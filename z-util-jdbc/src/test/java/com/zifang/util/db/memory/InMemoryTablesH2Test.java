package com.zifang.util.db.memory;

import com.zifang.util.db.context.DataSourceRegistry;
import com.zifang.util.db.dialect.H2Dialect;
import com.zifang.util.db.meta.DataSourceDTO;
import com.zifang.util.db.query.Criteria;
import com.zifang.util.db.query.DynamicQuery;
import com.zifang.util.db.query.Query;
import com.zifang.util.db.query.SqlSpec;
import com.zifang.util.expr.sql.engine.Table;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JDBC 取数 → 内存 SQL 引擎端到端：真实建库取行，在内存里做 join、分组聚合、
 * 链式算子与索引加速，验证复杂处理不必再把 SQL 打回数据库。
 */
public class InMemoryTablesH2Test {

    private static final String CODE = "dq-memory";

    private static DataSourceRegistry registry;

    private DynamicQuery dq;

    @BeforeClass
    public static void initRegistry() {
        registry = new DataSourceRegistry();
        DataSourceDTO def = new DataSourceDTO();
        def.setDatasourceCode(CODE);
        def.setDatasourceType(H2Dialect.ID);
        def.setSchemaMark("in_memory_tables");
        def.setUserName("sa");
        def.setPw("");
        registry.register(def);
    }

    @Before
    public void createTables() {
        dq = new DynamicQuery(registry.require(CODE), new H2Dialect());
        dq.executeUpdate(SqlSpec.of("DROP TABLE IF EXISTS \"t_order\""));
        dq.executeUpdate(SqlSpec.of("DROP TABLE IF EXISTS \"t_user\""));
        dq.executeUpdate(SqlSpec.of("CREATE TABLE \"t_user\" ("
                + "\"id\" BIGINT PRIMARY KEY, \"name\" VARCHAR(32), \"dept\" VARCHAR(16))"));
        dq.executeUpdate(SqlSpec.of("CREATE TABLE \"t_order\" ("
                + "\"id\" BIGINT PRIMARY KEY, \"user_id\" BIGINT,"
                + " \"amount\" DECIMAL(12,2), \"status\" VARCHAR(8))"));
        user(1L, "老张", "A组");
        user(2L, "小李", "A组");
        user(3L, "老王", "B组");
        order(1L, 1L, "100.50", "PAID");
        order(2L, 1L, "200.00", "PAID");
        order(3L, 2L, "50.25", "NEW");
        order(4L, 3L, "999.99", "PAID");
        // user_id=9 不存在: INNER JOIN 应在内存里把它丢掉
        order(5L, 9L, "10.00", "PAID");
    }

    @AfterClass
    public static void closeRegistry() {
        if (registry != null) {
            registry.close();
        }
    }

    private void user(long id, String name, String dept) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", id);
        values.put("name", name);
        values.put("dept", dept);
        dq.insert("t_user", values);
    }

    private void order(long id, long userId, String amount, String status) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", id);
        values.put("user_id", userId);
        values.put("amount", new BigDecimal(amount));
        values.put("status", status);
        dq.insert("t_order", values);
    }

    private InMemoryTables loaded() {
        return new InMemoryTables(dq)
                .load("t_user", Query.select().from("t_user"))
                .load("t_order", Query.select().from("t_order"));
    }

    @Test
    public void joinAndGroupByRunInMemory() {
        List<Map<String, Object>> groups = loaded().sql(
                "SELECT u.dept, COUNT(*) AS cnt, SUM(o.amount) AS total"
                        + " FROM t_order o INNER JOIN t_user u ON o.user_id = u.id GROUP BY u.dept");
        assertEquals(2, groups.size());
        Map<String, Object> groupA = byKey(groups, "dept", "A组");
        assertEquals(3L, groupA.get("cnt"));
        assertEquals(350.75d, ((Number) groupA.get("total")).doubleValue(), 0.001d);
        Map<String, Object> groupB = byKey(groups, "dept", "B组");
        assertEquals(1L, groupB.get("cnt"));
        assertEquals(999.99d, ((Number) groupB.get("total")).doubleValue(), 0.001d);
    }

    @Test
    public void filterAndOrderAfterJoin() {
        List<Map<String, Object>> rows = loaded().sql(
                "SELECT u.name, o.amount FROM t_order o INNER JOIN t_user u ON o.user_id = u.id"
                        + " WHERE o.status = 'PAID' AND o.amount > 150 ORDER BY o.amount DESC");
        assertEquals(Arrays.asList("老王", "老张"), column(rows, "name"));
    }

    @Test
    public void chainedTableOpsOnJdbcRows() {
        Table orders = loaded().table("t_order");
        assertEquals(5, orders.size());

        Table paid = orders.where("status", "PAID").sort("amount", false);
        assertEquals(4, paid.size());
        assertEquals(new BigDecimal("999.99"), paid.first().get("amount"));

        paid.addColumn("after_tax", row -> ((BigDecimal) row.get("amount")).multiply(new BigDecimal("1.06")));
        assertEquals(0, new BigDecimal("106.53").compareTo(
                (BigDecimal) byKey(paid.toMapList(), "id", 1L).get("after_tax")));

        Map<String, Table> byStatus = orders.groupBy("status");
        assertEquals(2, byStatus.size());
        assertEquals(4, byStatus.get("PAID").size());
        assertEquals(1, byStatus.get("NEW").size());
        assertEquals(1360.74d, ((Number) orders.aggregate("amount", "SUM")).doubleValue(), 0.001d);
    }

    @Test
    public void indexSpeedsEqualityLookupWithoutChangingResult() {
        List<Map<String, Object>> plain = new InMemoryTables(dq)
                .load("t_order", Query.select().from("t_order"))
                .sql("SELECT id FROM t_order WHERE user_id = 1 ORDER BY id");
        List<Map<String, Object>> indexed = loaded().index("t_order", "user_id")
                .sql("SELECT id FROM t_order WHERE user_id = 1 ORDER BY id");
        assertEquals(Arrays.asList(1L, 2L), column(indexed, "id"));
        assertEquals(column(plain, "id"), column(indexed, "id"));
    }

    @Test
    public void queryAndTemplateBothFeedMemoryTables() {
        InMemoryTables mem = new InMemoryTables(dq)
                .load("paid", Query.select("id", "amount").from("t_order").where(Criteria.eq("status", "NONE")))
                .load("all", SqlSpec.of("SELECT \"id\", \"amount\" FROM \"t_order\""));
        assertEquals(0, mem.table("paid").size());
        assertEquals(5, mem.table("all").size());
        assertTrue(mem.names().contains("paid"));
        assertFalse(mem.names().contains("t_order"));
    }

    @Test
    public void registrationIsCaseInsensitiveAndManualRowsAllowed() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1L);
        row.put("dept", "C组");
        InMemoryTables mem = new InMemoryTables().load("T_Order", Arrays.asList(row, row));
        assertTrue(mem.names().contains("t_order"));
        assertEquals(2L, mem.sql("SELECT COUNT(*) AS c FROM t_order").get(0).get("c"));
    }

    @Test
    public void loadWithoutDataSourceNeedsRows() {
        try {
            new InMemoryTables().load("t", Query.select().from("t_user"));
            fail("没有 DynamicQuery 不能声称从库里取数");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("DynamicQuery"));
        }
    }

    @Test
    public void maxRowsBoundsWhatEntersMemory() {
        InMemoryTables mem = new InMemoryTables(dq.maxRows(2))
                .load("t_order", Query.select().from("t_order"));
        assertEquals(2, mem.table("t_order").size());
    }

    private static Map<String, Object> byKey(List<Map<String, Object>> rows, String key, Object value) {
        for (Map<String, Object> row : rows) {
            if (value.equals(row.get(key))) {
                return row;
            }
        }
        throw new AssertionError("结果里找不到 " + key + "=" + value + " on " + rows);
    }

    private static List<Object> column(List<Map<String, Object>> rows, String key) {
        List<Object> values = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            values.add(row.get(key));
        }
        return values;
    }
}
