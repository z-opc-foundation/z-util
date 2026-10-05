package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.expression.ExpressionEvaluator;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * 消费方（feature-team 低代码平台）反馈的回归测试，对应 1.0.18 修复面：
 * P0-1 内置函数自动注册 / P0-2 GROUP BY 标量函数分流 / P0-3 ROW_NUMBER 摘除 /
 * P1-1 FROM/JOIN 子查询 / P1-2 JSON_EXTRACT+JSON_CONTAINS /
 * P2 AVG 科学计数法 / P2 SUM 类型口径对齐。
 */
public class ConsumerFeedbackRegressionTest {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();

        List<Map<String, Object>> events = new ArrayList<>();
        events.add(row("id", 1, "user_id", 1, "create_time", "2026-10-01 10:00:00",
                "close_info", "{\"close_tags\":[\"ai_effective\",\"manual\"],\"operator\":{\"name\":\"alice\"}}"));
        events.add(row("id", 2, "user_id", 1, "create_time", "2026-10-01 15:30:00",
                "close_info", "{\"close_tags\":[\"manual\"],\"note\":\"mentioned ai_effective in text\"}"));
        events.add(row("id", 3, "user_id", 2, "create_time", "2026-10-02 09:00:00",
                "close_info", "{\"close_tags\":[\"ai_effective\"],\"operator\":{\"name\":\"bob\"}}"));
        events.add(row("id", 4, "user_id", 1, "create_time", "2026-10-02 11:00:00", "close_info", null));
        engine.register("events", events);

        List<Map<String, Object>> daily = new ArrayList<>();
        daily.add(row("dt", 1, "cnt", 1));
        daily.add(row("dt", 2, "cnt", 2));
        daily.add(row("dt", 3, "cnt", 3));
        engine.register("daily", daily);
    }

    // ==================== P0-1 内置函数自动注册 ====================

    @Test
    public void p0_1_builtinFunctionsWorkWithoutManualRegistration() {
        // 引擎从零构建，全程不调 registerBuiltin()
        VirtualTableEngine fresh = new VirtualTableEngine();
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("v", 1));
        fresh.register("t", data);

        List<Map<String, Object>> r = fresh.query(
                "SELECT IFNULL(NULL, 'x') AS a, UPPER('ab') AS b, DATE('2026-10-04 12:00:00') AS d FROM t");
        assertEquals(1, r.size());
        assertEquals("x", r.get(0).get("a"));
        assertEquals("AB", r.get(0).get("b"));
        assertNotNull(r.get(0).get("d"));
    }

    @Test
    public void p0_1_expressionEvaluatorAutoRegistered() {
        ExpressionEvaluator eval = new ExpressionEvaluator();
        assertEquals("x", eval.evaluate("IFNULL(NULL, 'x')", Collections.emptyMap()));
        assertEquals(3L, ((Number) eval.evaluate("INSTR('abcdef', 'cd')", Collections.emptyMap())).longValue());
    }

    // ==================== P0-2 GROUP BY 标量函数分流 ====================

    @Test
    public void p0_2_scalarFunctionInGroupBySelectWithAlias() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE(create_time) AS dt, COUNT(*) AS cnt FROM events GROUP BY DATE(create_time)");
        assertEquals(2, r.size());

        Map<String, Object> day1 = findRowByDt(r);
        assertNotNull("别名 dt 列必须存在", day1);
        assertEquals(2L, ((Number) day1.get("cnt")).longValue());
        for (Map<String, Object> row : r) {
            assertEquals(2L, ((Number) row.get("cnt")).longValue());
        }
    }

    private Map<String, Object> findRowByDt(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            if (row.containsKey("dt") && row.get("dt") != null) {
                return row;
            }
        }
        return null;
    }

    @Test
    public void p0_2_scalarFunctionMixedWithAggregateNoGroupBy() {
        // 隐式聚合里混标量函数：MAX 走聚合，DATE 走首行标量
        List<Map<String, Object>> r = engine.query(
                "SELECT MAX(id) AS max_id, DATE(create_time) AS first_day FROM events");
        assertEquals(1, r.size());
        assertEquals(4, ((Number) r.get(0).get("max_id")).intValue());
        assertEquals("2026-10-01", r.get(0).get("first_day").toString());
    }

    // ==================== P0-3 ROW_NUMBER 不再静默给错值 ====================

    @Test
    public void p0_3_rowNumberIsNotSilentlyOne() {
        try {
            engine.query("SELECT ROW_NUMBER() FROM daily");
            fail("ROW_NUMBER 未实现窗口语义，必须显式报错而不是每行返回 1");
        } catch (SqlException expected) {
            assertTrue(expected.getMessage().contains("ROW_NUMBER"));
        }
    }

    // ==================== P1-1 FROM / JOIN 子查询 ====================

    @Test
    public void p1_1_fromSubqueryOnEvents() {
        List<Map<String, Object>> r = engine.query(
                "SELECT t.dt, t.cnt FROM (SELECT DATE(create_time) AS dt, COUNT(*) AS cnt "
                        + "FROM events GROUP BY DATE(create_time)) t");
        assertEquals(2, r.size());
        for (Map<String, Object> row : r) {
            assertTrue(row.containsKey("dt"));
            assertTrue(row.containsKey("cnt"));
            assertEquals(2L, ((Number) row.get("cnt")).longValue());
        }
    }

    @Test
    public void p1_1_runningTotalViaSubquerySelfJoin() {
        List<Map<String, Object>> r = engine.query(
                "SELECT a.dt, SUM(b.cnt) AS rt FROM (SELECT dt, cnt FROM daily) a "
                        + "INNER JOIN (SELECT dt, cnt FROM daily) b ON a.dt >= b.dt GROUP BY a.dt");
        assertEquals(3, r.size());

        Map<Object, Object> byDt = new HashMap<>();
        for (Map<String, Object> row : r) {
            byDt.put(row.get("dt"), row.get("rt"));
        }
        assertEquals(1L, ((Number) byDt.get(1)).longValue());
        assertEquals(3L, ((Number) byDt.get(2)).longValue());
        assertEquals(6L, ((Number) byDt.get(3)).longValue());
    }

    @Test
    public void p1_1_joinSubqueryWithEquiJoin() {
        List<Map<String, Object>> users = new ArrayList<>();
        users.add(row("id", 1, "name", "Alice"));
        users.add(row("id", 2, "name", "Bob"));
        engine.register("u2", users);

        List<Map<String, Object>> r = engine.query(
                "SELECT u.name, s.c FROM u2 u INNER JOIN "
                        + "(SELECT user_id, COUNT(*) AS c FROM events GROUP BY user_id) s "
                        + "ON u.id = s.user_id WHERE u.id = 1");
        assertEquals(1, r.size());
        assertEquals("Alice", r.get(0).get("name"));
        assertEquals(3L, ((Number) r.get(0).get("c")).longValue());
    }

    @Test
    public void p1_1_fromSubqueryRequiresAlias() {
        try {
            engine.query("SELECT dt FROM (SELECT dt FROM daily)");
            fail("FROM 子查询缺别名必须报错");
        } catch (SqlException expected) {
            assertTrue(expected.getMessage().contains("别名"));
        }
    }

    // ==================== P1-2 JSON 函数 ====================

    @Test
    public void p1_2_jsonExtractScalarAndPath() {
        List<Map<String, Object>> r = engine.query(
                "SELECT JSON_EXTRACT(close_info, '$.operator.name') AS op, "
                        + "JSON_EXTRACT(close_info, '$.close_tags[0]') AS first_tag "
                        + "FROM events WHERE id = 1");
        assertEquals(1, r.size());
        assertEquals("alice", r.get(0).get("op"));
        assertEquals("ai_effective", r.get(0).get("first_tag"));
    }

    @Test
    public void p1_2_jsonExtractArrayReturnsJsonText() {
        List<Map<String, Object>> r = engine.query(
                "SELECT JSON_EXTRACT(close_info, '$.close_tags') AS tags FROM events WHERE id = 3");
        assertEquals("[\"ai_effective\"]", r.get(0).get("tags"));
    }

    @Test
    public void p1_2_jsonExtractMissingPathIsNull() {
        List<Map<String, Object>> r = engine.query(
                "SELECT JSON_EXTRACT(close_info, '$.not_exist') AS v FROM events WHERE id = 1");
        assertNull(r.get(0).get("v"));
    }

    @Test
    public void p1_2_jsonContainsArrayMembership() {
        // 与消费方原口径一致：JSON_CONTAINS(JSON_EXTRACT(...), '"ai_effective"')
        List<Map<String, Object>> r = engine.query(
                "SELECT id FROM events WHERE JSON_CONTAINS(JSON_EXTRACT(close_info, '$.close_tags'), '\"ai_effective\"') = 1");
        assertEquals(2, r.size());
        Set<Object> ids = new HashSet<>();
        for (Map<String, Object> row : r) {
            ids.add(row.get("id"));
        }
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(3));
    }

    @Test
    public void p1_2_jsonContainsDoesNotSubstringMatch() {
        // id=2 的 note 文本里含 ai_effective，但 close_tags 数组没有 —— 子串近似会误判，JSON 语义不能
        List<Map<String, Object>> r = engine.query(
                "SELECT id FROM events WHERE JSON_CONTAINS(JSON_EXTRACT(close_info, '$.close_tags'), '\"ai_effective\"') = 1 AND id = 2");
        assertEquals(0, r.size());
    }

    @Test
    public void p1_2_jsonContainsWithOptionalPath() {
        List<Map<String, Object>> r = engine.query(
                "SELECT JSON_CONTAINS(close_info, '\"alice\"', '$.operator.name') AS hit FROM events WHERE id = 1");
        assertEquals(1L, r.get(0).get("hit"));
    }

    // ==================== P2 AVG 科学计数法 ====================

    @Test
    public void p2_avgNeverScientificNotation() {
        List<Map<String, Object>> single = new ArrayList<>();
        single.add(row("x", 100));
        engine.register("one", single);

        Object avg = engine.query("SELECT AVG(x) AS a FROM one").get(0).get("a");
        assertTrue(avg instanceof BigDecimal);
        BigDecimal bd = (BigDecimal) avg;
        assertEquals(0, new BigDecimal("100").compareTo(bd));
        assertTrue("AVG(100) 不应序列化成 1E+2，实际: " + bd, bd.scale() >= 0);
        assertEquals("100", bd.toString());
    }

    // ==================== P2 SUM 类型口径对齐 ObjEngine ====================

    @Test
    public void p2_sumAllIntegralReturnsLong() {
        List<Map<String, Object>> r = engine.query("SELECT SUM(cnt) AS s FROM daily");
        Object sum = r.get(0).get("s");
        assertTrue("全整型列 SUM 应为 Long，实际: " + sum.getClass(), sum instanceof Long);
        assertEquals(6L, sum);
    }

    @Test
    public void p2_sumWithFractionReturnsDouble() {
        List<Map<String, Object>> mixed = new ArrayList<>();
        mixed.add(row("v", 1));
        mixed.add(row("v", 2.5));
        engine.register("mix", mixed);

        Object sum = engine.query("SELECT SUM(v) AS s FROM mix").get(0).get("s");
        assertTrue("掺浮点 SUM 应为 Double，实际: " + sum.getClass(), sum instanceof Double);
        assertEquals(3.5, (Double) sum, 1e-9);
    }

    // ==================== 辅助 ====================

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
