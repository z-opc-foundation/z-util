package com.zifang.util.db.query;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.db.context.DataSourceRegistry;
import com.zifang.util.db.dialect.H2Dialect;
import com.zifang.util.db.dialect.SqlType;
import com.zifang.util.db.meta.DataSourceDTO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 动态查询端到端：真实建池、建表、写入、条件取数、分页与元数据，跑在 H2 内存库上。
 * <p>
 * 建表时用小写带引号的标识符，与 {@link H2Dialect} 的引用方式保持一致。
 */
public class DynamicQueryH2Test {

    private static final String CODE = "dq-report";

    private static DataSourceRegistry registry;

    private DynamicQuery dq;

    @BeforeClass
    public static void initRegistry() {
        registry = new DataSourceRegistry();
        DataSourceDTO def = new DataSourceDTO();
        def.setDatasourceCode(CODE);
        def.setDatasourceType(H2Dialect.ID);
        def.setSchemaMark("dynamic_query");
        def.setUserName("sa");
        def.setPw("");
        registry.register(def);
    }

    @Before
    public void createTable() {
        dq = new DynamicQuery(registry.require(CODE), new H2Dialect());
        dq.executeUpdate(SqlSpec.of("DROP TABLE IF EXISTS \"t_order\""));
        dq.executeUpdate(SqlSpec.of("CREATE TABLE \"t_order\" ("
                + "\"id\" BIGINT PRIMARY KEY, \"amount\" DECIMAL(12,2), \"status\" VARCHAR(16),"
                + " \"channel\" VARCHAR(8), \"remark\" VARCHAR(64), \"biz_date\" DATE)"));
        insert(1L, "120.50", "PAID", "app", null, "2026-09-21");
        insert(2L, "80.00", "NEW", "web", "加急处理", "2026-09-22");
        insert(3L, "999.99", "PAID", "web", null, "2026-09-23");
        insert(4L, "12.00", "DONE", "app", "备注", "2026-09-23");
    }

    @AfterClass
    public static void closeRegistry() {
        if (registry != null) {
            registry.close();
        }
    }

    @After
    public void dropTable() {
        dq.executeUpdate(SqlSpec.of("DROP TABLE IF EXISTS \"t_order\""));
    }

    private void insert(long id, String amount, String status, String channel, String remark, String day) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", id);
        values.put("amount", new BigDecimal(amount));
        values.put("status", status);
        values.put("channel", channel);
        values.put("remark", remark);
        values.put("biz_date", Date.valueOf(day));
        assertEquals(1, dq.insert("t_order", values));
    }

    @Test
    public void listKeepsProjectionAndRowOrder() {
        List<Map<String, Object>> rows = dq.list(Query.select("id", "amount").from("t_order")
                .orderBy("id", false));
        assertEquals(4, rows.size());
        assertEquals(Arrays.asList("id", "amount"), Arrays.asList(rows.get(0).keySet().toArray(new String[0])));
        assertEquals(4L, ((Number) rows.get(0).get("id")).longValue());
        assertEquals(new BigDecimal("999.99"), rows.get(1).get("amount"));
    }

    @Test
    public void conditionsAndPagination() {
        List<Map<String, Object>> paid = dq.list(Query.select().from("t_order")
                .where(Criteria.eq("status", "PAID"))
                .where(Criteria.ge("amount", 100)));
        assertEquals(2, paid.size());

        List<Map<String, Object>> webOrBig = dq.list(Query.select("id").from("t_order")
                .where(Criteria.or(Criteria.eq("channel", "web"), Criteria.gt("amount", 500)))
                .orderBy("id"));
        assertEquals(2, webOrBig.size());
        assertEquals(2L, ((Number) webOrBig.get(0).get("id")).longValue());

        assertEquals(1, dq.list(Query.select().from("t_order").where(Criteria.contains("remark", "加急"))).size());
        assertEquals(2, dq.list(Query.select().from("t_order").where(Criteria.isNull("remark"))).size());
        // PAID: id1/id3, DONE: id4
        assertEquals(3, dq.list(Query.select().from("t_order").where(Criteria.in("status", "PAID", "DONE"))).size());
        assertEquals(0, dq.list(Query.select().from("t_order").where(Criteria.in("status", "CANCELLED"))).size());
        assertEquals(2, dq.list(Query.select().from("t_order")
                .where(Criteria.between("biz_date", Date.valueOf("2026-09-21"), Date.valueOf("2026-09-22")))).size());
        assertEquals(2, dq.list(Query.select("channel").from("t_order").distinct()).size());
    }

    @Test
    public void countOneAndScalar() {
        assertEquals(4L, dq.count(Query.select().from("t_order")));
        assertEquals(2L, dq.count(Query.select().from("t_order").where(Criteria.eq("status", "PAID"))));
        assertEquals(2L, dq.count(Query.select("channel").from("t_order").distinct()));
        // 分页窗口不影响总数
        assertEquals(4L, dq.count(Query.select().from("t_order").page(2, 2)));

        Map<String, Object> one = dq.one(Query.select().from("t_order").where(Criteria.eq("id", 3L)));
        assertNotNull(one);
        assertEquals("PAID", one.get("status"));
        assertNull(dq.one(Query.select().from("t_order").where(Criteria.eq("id", 404L))));

        Object max = dq.scalar(SqlSpec.of("SELECT MAX(\"amount\") FROM \"t_order\""));
        assertEquals(0, new BigDecimal("999.99").compareTo((BigDecimal) max));
        assertNull(dq.scalar(SqlSpec.of("SELECT MAX(\"amount\") FROM \"t_order\" WHERE \"id\" = ?", -1L)));
    }

    @Test
    public void pageReturnsRowsAndTotal() {
        Page<Map<String, Object>> second = dq.page(Query.select().from("t_order").orderBy("id").page(2, 2));
        assertEquals(4L, second.total());
        assertEquals(2, second.rows().size());
        assertEquals(3L, ((Number) second.rows().get(0).get("id")).longValue());
        assertEquals(4L, ((Number) second.rows().get(1).get("id")).longValue());
        assertEquals(2, second.pageNo());
        assertEquals(2L, second.pageCount());
        assertFalse(second.isEmpty());

        Page<Map<String, Object>> empty = dq.page(Query.select().from("t_order")
                .where(Criteria.eq("status", "NONE")).page(1, 20));
        assertEquals(0L, empty.total());
        assertTrue(empty.rows().isEmpty());
        assertTrue(empty.isEmpty());
        assertEquals(0L, empty.pageCount());
    }

    @Test
    public void templateWithNamedParams() {
        SqlTemplate template = SqlTemplate.of("SELECT \"id\", \"status\" FROM \"t_order\""
                + " WHERE \"biz_date\" = ${day} AND \"amount\" >= ${min} ORDER BY \"id\"");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("day", Date.valueOf("2026-09-23"));
        params.put("min", new BigDecimal("100"));
        List<Map<String, Object>> rows = dq.list(template.bind(params));
        assertEquals(1, rows.size());
        assertEquals(3L, ((Number) rows.get(0).get("id")).longValue());
        assertEquals("PAID", rows.get(0).get("status"));
    }

    @Test
    public void updateAndDeleteAffectExpectedRows() {
        Map<String, Object> set = new LinkedHashMap<>();
        set.put("status", "DONE");
        set.put("remark", null);
        assertEquals(2, dq.update("t_order", set, Criteria.eq("status", "PAID")));
        assertEquals(3, dq.list(Query.select().from("t_order").where(Criteria.eq("status", "DONE"))).size());
        assertEquals(2, dq.list(Query.select().from("t_order").where(Criteria.isNull("remark"))).size());

        assertEquals(1, dq.delete("t_order", Criteria.eq("id", 4L)));
        assertEquals(3L, dq.count(Query.select().from("t_order")));
        try {
            dq.delete("t_order", null);
            fail("无条件 delete 应在执行前被拒");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("delete"));
        }
    }

    @Test
    public void metadataReadout() {
        assertTrue(dq.tables().contains("t_order"));
        Map<String, SqlType> columns = dq.columns("t_order");
        assertEquals(SqlType.LONG, columns.get("id"));
        assertEquals(SqlType.DECIMAL, columns.get("amount"));
        assertEquals(SqlType.STRING, columns.get("status"));
        assertEquals(SqlType.DATE, columns.get("biz_date"));
        assertEquals(6, columns.size());
        assertEquals(Arrays.asList("id", "amount", "status", "channel", "remark", "biz_date"),
                Arrays.asList(columns.keySet().toArray(new String[0])));
    }

    @Test
    public void viewsAreDataUnitsAndQualifiedNamesResolve() {
        dq.executeUpdate(SqlSpec.of("CREATE VIEW \"v_paid\" AS SELECT \"id\", \"amount\" FROM \"t_order\""));
        try {
            assertTrue(dq.tables().contains("v_paid"));
            // 限定名前缀既可能是 catalog 也可能是 schema, 两侧都要能定位到
            Map<String, SqlType> viaSchema = dq.columns("PUBLIC.v_paid");
            assertEquals(2, viaSchema.size());
            assertEquals(SqlType.DECIMAL, viaSchema.get("amount"));
            Map<String, SqlType> viaCatalog = dq.columns("DYNAMIC_QUERY.v_paid");
            assertEquals(viaSchema, viaCatalog);
        } finally {
            dq.executeUpdate(SqlSpec.of("DROP VIEW \"v_paid\""));
        }
        assertFalse(dq.tables().contains("v_paid"));
    }

    @Test
    public void maxRowsCapsRead() {
        assertEquals(4, new DynamicQuery(registry.require(CODE), new H2Dialect())
                .list(Query.select().from("t_order")).size());
        DynamicQuery capped = new DynamicQuery(registry.require(CODE), new H2Dialect()).maxRows(2);
        assertEquals(2, capped.maxRows());
        assertEquals(2, capped.list(Query.select().from("t_order")).size());
        try {
            capped.maxRows(-1);
            fail("maxRows 不能为负");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("maxRows"));
        }
    }

    @Test
    public void dialectAutoDetectedAndErrorsWrapped() {
        DynamicQuery auto = new DynamicQuery(registry.require(CODE));
        assertEquals(H2Dialect.ID, auto.dialect().id());
        assertEquals(H2Dialect.ID, auto.compiler().dialect().id());
        assertEquals(registry.get(CODE), auto.dataSource());
        try {
            auto.list(SqlSpec.of("SELECT * FROM \"no_such_table\""));
            fail("SQL 错误应包装成 BusinessException");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("数据库查询失败"));
        }
        try {
            auto.executeUpdate(SqlSpec.of("INSERT INTO \"no_such_table\" (id) VALUES (1)"));
            fail("写入错误应包装成 BusinessException");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("数据库写入失败"));
        }
    }
}
