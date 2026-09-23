package com.zifang.util.db.query;

import com.zifang.util.db.dialect.Dialect;
import com.zifang.util.db.dialect.H2Dialect;
import com.zifang.util.db.dialect.MySqlDialect;
import com.zifang.util.db.dialect.PostgresDialect;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Query → 参数化 SQL 的编译规则，含条件树、分页与写操作的安全边界。
 */
public class QueryCompilerTest {

    private static final Dialect MYSQL = new MySqlDialect();

    private final QueryCompiler compiler = new QueryCompiler(MYSQL);

    @Test
    public void plainProjectionAndPredicate() {
        Query query = Query.select("id", "amount").from("t_order").where(Criteria.eq("status", "PAID"));
        SqlSpec spec = query.compile(MYSQL);
        assertEquals("SELECT `id`, `amount` FROM `t_order` WHERE `status` = ?", spec.sql());
        assertEquals(Collections.singletonList("PAID"), spec.params());
    }

    @Test
    public void starProjectionAndDistinct() {
        assertEquals("SELECT * FROM `t_order`", Query.select().from("t_order").compile(MYSQL).sql());
        assertEquals("SELECT DISTINCT `channel` FROM `t_order`",
                Query.select("channel").from("t_order").distinct().compile(MYSQL).sql());
    }

    @Test
    public void operatorsAndParamOrder() {
        Query query = Query.select().from("t_order")
                .where(Criteria.ge("amount", 100))
                .where(Criteria.between("biz_date", "2026-09-01", "2026-09-30"))
                .where(Criteria.in("channel", "app", "web"))
                .where(Criteria.isNull("deleted_at"))
                .where(Criteria.contains("remark", "加急"));
        SqlSpec spec = query.compile(MYSQL);
        assertEquals("SELECT * FROM `t_order` WHERE `amount` >= ? AND `biz_date` BETWEEN ? AND ?"
                + " AND `channel` IN (?, ?) AND `deleted_at` IS NULL AND `remark` LIKE ?", spec.sql());
        assertEquals(Arrays.<Object>asList(100, "2026-09-01", "2026-09-30", "app", "web", "%加急%"), spec.params());
    }

    @Test
    public void nestedOrGroup() {
        Query query = Query.select().from("t_order")
                .where(Criteria.eq("status", "PAID"))
                .where(Criteria.or(Criteria.eq("channel", "app"),
                        Criteria.and(Criteria.eq("channel", "web"), Criteria.gt("amount", 1000))));
        SqlSpec spec = query.compile(MYSQL);
        assertEquals("SELECT * FROM `t_order` WHERE `status` = ? AND (`channel` = ?"
                + " OR (`channel` = ? AND `amount` > ?))", spec.sql());
        assertEquals(Arrays.<Object>asList("PAID", "app", "web", 1000), spec.params());
    }

    @Test
    public void emptyCollectionBecomesConstPredicate() {
        assertEquals("SELECT * FROM `t_order` WHERE `channel` IN (?, ?)",
                Query.select().from("t_order").where(Criteria.in("channel", Arrays.asList("app", "web"))).compile(MYSQL).sql());
        assertEquals("SELECT * FROM `t_order` WHERE 1 = 0",
                Query.select().from("t_order").where(Criteria.in("channel", Collections.emptyList())).compile(MYSQL).sql());
        assertEquals("SELECT * FROM `t_order` WHERE 1 = 1",
                Query.select().from("t_order").where(Criteria.notIn("channel", Collections.emptyList())).compile(MYSQL).sql());
        // 空 AND 组语义上是恒真, 空 OR 组恒假
        assertEquals("SELECT * FROM `t_order` WHERE 1 = 1",
                Query.select().from("t_order").where(Criteria.and()).compile(MYSQL).sql());
        assertEquals("SELECT * FROM `t_order` WHERE 1 = 0",
                Query.select().from("t_order").where(Criteria.or()).compile(MYSQL).sql());
    }

    @Test
    public void orderAndPaging() {
        Query query = Query.select("id").from("t_order").orderBy("created_at", false).orderBy("id").page(3, 20);
        SqlSpec spec = query.compile(MYSQL);
        assertEquals("SELECT `id` FROM `t_order` ORDER BY `created_at` DESC, `id` ASC LIMIT 20 OFFSET 40", spec.sql());
        assertTrue(query.offset() == 40 && query.limit() == 20);
    }

    @Test
    public void countWrapsSelectAndKeepsDistinct() {
        Query query = Query.select("channel").from("t_order").distinct()
                .where(Criteria.eq("status", "PAID")).limit(10);
        SqlSpec count = query.compileCount(MYSQL);
        assertEquals("SELECT COUNT(*) FROM (SELECT DISTINCT `channel` FROM `t_order` WHERE `status` = ?) z_cnt",
                count.sql());
        assertEquals(Collections.singletonList("PAID"), count.params());
    }

    @Test
    public void dialectDifferences() {
        Query query = Query.select("id").from("t_order").where(Criteria.eq("status", "PAID")).offset(5).limit(5);
        assertEquals("SELECT \"id\" FROM \"t_order\" WHERE \"status\" = ? LIMIT 5 OFFSET 5",
                query.compile(new PostgresDialect()).sql());
        assertEquals("SELECT \"id\" FROM \"t_order\" WHERE \"status\" = ? LIMIT 5 OFFSET 5",
                query.compile(new H2Dialect()).sql());
    }

    @Test
    public void writeStatements() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", 1L);
        values.put("status", "NEW");
        values.put("remark", null);
        assertEquals("INSERT INTO `t_order` (`id`, `status`, `remark`) VALUES (?, ?, ?)",
                compiler.compileInsert("t_order", values).sql());
        assertEquals(Arrays.<Object>asList(1L, "NEW", null), compiler.compileInsert("t_order", values).params());

        SqlSpec update = compiler.compileUpdate("t_order", values, Criteria.eq("id", 1L));
        assertEquals("UPDATE `t_order` SET `id` = ?, `status` = ?, `remark` = ? WHERE `id` = ?", update.sql());
        assertEquals(Arrays.<Object>asList(1L, "NEW", null, 1L), update.params());

        SqlSpec delete = compiler.compileDelete("t_order", Criteria.isNull("remark"));
        assertEquals("DELETE FROM `t_order` WHERE `remark` IS NULL", delete.sql());
    }

    @Test
    public void fullTableWriteNeedsExplicitCondition() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("status", "NEW");
        try {
            compiler.compileUpdate("t_order", values, null);
            fail("无条件的 update 应被拒绝");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("update"));
        }
        try {
            compiler.compileDelete("t_order", null);
            fail("无条件的 delete 应被拒绝");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("delete"));
        }
        assertTrue(compiler.compileDelete("t_order", Criteria.alwaysTrue()).sql().endsWith("WHERE 1 = 1"));
    }

    @Test
    public void illegalIdentifiersRejectedAtConstruction() {
        String[] evil = {"id; DROP TABLE t", "1 = 1", "a b", "count(*)"};
        for (String column : evil) {
            try {
                Criteria.eq(column, "x");
                fail("应拒绝列名: " + column);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("标识符"));
            }
        }
        try {
            Query.select().from("t_order; --");
            fail("应拒绝表名");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("标识符"));
        }
    }

    @Test
    public void arityAndMiscGuards() {
        try {
            new QueryCompiler(null);
            fail("方言不能为空");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("dialect"));
        }
        try {
            new Criterion("a", Op.BETWEEN, Collections.singletonList(1));
            fail("between 需要两个值");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("BETWEEN"));
        }
        try {
            new Criterion("a", Op.EQ, Arrays.asList(1, 2));
            fail("eq 只接受一个值");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("EQ"));
        }
        try {
            Query.select("id").compile(MYSQL);
            fail("未指定表名");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("表名"));
        }
        try {
            Query.select().from("t").page(0, 10);
            fail("页码必须从 1 开始");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("pageNo"));
        }
        List<Predicate> conditions = Query.select().from("t").where(Criteria.eq("a", 1)).conditions();
        assertEquals(1, conditions.size());
        assertEquals("a EQ [1]", conditions.get(0).toString());
    }
}
