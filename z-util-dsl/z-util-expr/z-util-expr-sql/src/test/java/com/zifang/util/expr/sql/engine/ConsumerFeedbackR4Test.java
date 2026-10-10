package com.zifang.util.expr.sql.engine;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 消费方（feature-team）第三轮 expr-sql 探针反馈回归测试，20 项实测：
 * P0-1 MONTH()/DAY() 返回年份（HOUR/MINUTE/SECOND 同源错位）/
 * P0-2 DATE_FORMAT MySQL % 词法不识别 /
 * P1-1 CASE WHEN / P1-2 DATE_ADD INTERVAL / P1-3 IN (SELECT …) / P1-4 OVER 窗口函数 /
 * P2-1 TIMESTAMPDIFF / P2-2 GROUP_CONCAT。
 * 喂值口径与 FT 生产实测一致：'2026-08-21 11:00:26'。
 */
public class ConsumerFeedbackR4Test {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(row("id", 1, "name", "alice", "dept", "A", "amount", 100,
                "ts", "2026-08-21 11:00:26", "created", LocalDateTime.of(2026, 8, 21, 11, 0, 26)));
        events.add(row("id", 2, "name", "bob", "dept", "B", "amount", 800,
                "ts", "2026-08-22 12:30:00", "created", LocalDateTime.of(2026, 8, 22, 12, 30, 0)));
        events.add(row("id", 3, "name", "carol", "dept", "A", "amount", 400,
                "ts", "2026-09-01 08:15:30", "created", LocalDateTime.of(2026, 9, 1, 8, 15, 30)));
        engine.register("events", events);
    }

    // ==================== P0-1 日期取值函数错位 ====================

    @Test
    public void r4_p0_1_monthReturnsMonthNotYear_stringInput() {
        List<Map<String, Object>> r = engine.query(
                "SELECT MONTH(ts) AS m FROM events WHERE id = 1");
        assertEquals(8, ((Number) r.get(0).get("m")).intValue());
    }

    @Test
    public void r4_p0_1_dayReturnsDayNotYear_stringInput() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DAY(ts) AS d FROM events WHERE id = 1");
        assertEquals(21, ((Number) r.get(0).get("d")).intValue());
    }

    @Test
    public void r4_p0_1_monthDayOnLocalDateTimeColumn() {
        List<Map<String, Object>> r = engine.query(
                "SELECT MONTH(created) AS m, DAY(created) AS d FROM events WHERE id = 1");
        assertEquals(8, ((Number) r.get(0).get("m")).intValue());
        assertEquals(21, ((Number) r.get(0).get("d")).intValue());
    }

    @Test
    public void r4_p0_1_yearStillCorrect() {
        List<Map<String, Object>> r = engine.query(
                "SELECT YEAR(ts) AS y FROM events WHERE id = 1");
        assertEquals(2026, ((Number) r.get(0).get("y")).intValue());
    }

    @Test
    public void r4_p0_1_hourMinuteSecondNotAllHour() {
        List<Map<String, Object>> r = engine.query(
                "SELECT HOUR(ts) AS h, MINUTE(ts) AS mi, SECOND(ts) AS s FROM events WHERE id = 1");
        assertEquals(11, ((Number) r.get(0).get("h")).intValue());
        assertEquals(0, ((Number) r.get(0).get("mi")).intValue());
        assertEquals(26, ((Number) r.get(0).get("s")).intValue());
    }

    // ==================== P0-2 DATE_FORMAT MySQL % 词法 ====================

    @Test
    public void r4_p0_2_dateFormatYmd() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_FORMAT(ts, '%Y-%m-%d') AS f FROM events WHERE id = 1");
        assertEquals("2026-08-21", r.get(0).get("f"));
    }

    @Test
    public void r4_p0_2_dateFormatHis() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_FORMAT(ts, '%H:%i:%s') AS f FROM events WHERE id = 1");
        assertEquals("11:00:26", r.get(0).get("f"));
    }

    @Test
    public void r4_p0_2_dateFormatFullAndLiteralText() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_FORMAT(ts, '%Y年%m月%d日') AS f FROM events WHERE id = 1");
        assertEquals("2026年08月21日", r.get(0).get("f"));
    }

    @Test
    public void r4_p0_2_dateFormatMonthNameAndEscapedPercent() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_FORMAT(ts, '%M %c/%e %p') AS f FROM events WHERE id = 1");
        assertEquals("August 8/21 AM", r.get(0).get("f"));
    }

    // ==================== P1-1 CASE WHEN ====================

    @Test
    public void r4_p1_1_searchedCase() {
        List<Map<String, Object>> r = engine.query(
                "SELECT name, CASE WHEN amount >= 400 THEN 'BIG' WHEN amount >= 100 THEN 'MID' ELSE 'SMALL' END AS tier "
                        + "FROM events ORDER BY id");
        assertEquals(3, r.size());
        assertEquals("MID", r.get(0).get("tier"));
        assertEquals("BIG", r.get(1).get("tier"));
        assertEquals("BIG", r.get(2).get("tier"));
    }

    @Test
    public void r4_p1_1_simpleCase() {
        List<Map<String, Object>> r = engine.query(
                "SELECT name, CASE dept WHEN 'A' THEN 'alpha' WHEN 'B' THEN 'beta' END AS dname "
                        + "FROM events ORDER BY id");
        assertEquals("alpha", r.get(0).get("dname"));
        assertEquals("beta", r.get(1).get("dname"));
        assertEquals("alpha", r.get(2).get("dname"));
    }

    @Test
    public void r4_p1_1_caseInWhereAndNoElse() {
        List<Map<String, Object>> r = engine.query(
                "SELECT name FROM events WHERE CASE WHEN amount >= 400 THEN 1 ELSE 0 END = 1 ORDER BY id");
        assertEquals(Arrays.asList("bob", "carol"), Arrays.asList(r.get(0).get("name"), r.get(1).get("name")));
    }

    // ==================== P1-2 DATE_ADD INTERVAL 语法 ====================

    @Test
    public void r4_p1_2_dateAddNegativeInterval() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_ADD(ts, INTERVAL -1 DAY) AS d FROM events WHERE id = 1");
        assertEquals("2026-08-20T11:00:26", r.get(0).get("d").toString());
    }

    @Test
    public void r4_p1_2_dateAddMonthAndDateSub() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATE_ADD(ts, INTERVAL 1 MONTH) AS m, DATE_SUB(ts, INTERVAL 1 HOUR) AS h FROM events WHERE id = 1");
        assertEquals("2026-09-21T11:00:26", r.get(0).get("m").toString());
        assertEquals("2026-08-21T10:00:26", r.get(0).get("h").toString());
    }

    // ==================== P1-3 IN (SELECT …) 子查询 ====================

    @Test
    public void r4_p1_3_inSubquery() {
        engine.register("big", engine.query("SELECT id FROM events WHERE amount >= 400"));
        List<Map<String, Object>> r = engine.query(
                "SELECT name FROM events WHERE id IN (SELECT id FROM big) ORDER BY id");
        assertEquals(Arrays.asList("bob", "carol"), Arrays.asList(r.get(0).get("name"), r.get(1).get("name")));
    }

    @Test
    public void r4_p1_3_inSubqueryInlineSameTable() {
        List<Map<String, Object>> r = engine.query(
                "SELECT name FROM events WHERE id IN (SELECT id FROM events WHERE amount >= 400) ORDER BY id");
        assertEquals(Arrays.asList("bob", "carol"), Arrays.asList(r.get(0).get("name"), r.get(1).get("name")));
    }

    @Test
    public void r4_p1_3_notInSubquery() {
        List<Map<String, Object>> r = engine.query(
                "SELECT name FROM events WHERE id NOT IN (SELECT id FROM events WHERE amount >= 400) ORDER BY id");
        assertEquals(1, r.size());
        assertEquals("alice", r.get(0).get("name"));
    }

    // ==================== P1-4 窗口函数 ====================

    @Test
    public void r4_p1_4_rowNumberOverOrderBy() {
        List<Map<String, Object>> r = engine.query(
                "SELECT ROW_NUMBER() OVER (ORDER BY amount DESC) AS rn, name FROM events");
        // OVER 内的 ORDER BY 只决定编号分配，输出行序保持原表行序（id 1,2,3）
        assertEquals(3, r.size());
        assertEquals("alice", r.get(0).get("name"));
        assertEquals(3L, ((Number) r.get(0).get("rn")).longValue());
        assertEquals("bob", r.get(1).get("name"));
        assertEquals(1L, ((Number) r.get(1).get("rn")).longValue());
        assertEquals("carol", r.get(2).get("name"));
        assertEquals(2L, ((Number) r.get(2).get("rn")).longValue());
    }

    @Test
    public void r4_p1_4_rankDenseRankAndPartition() {
        engine.register("dup", Arrays.asList(
                row("name", "a", "dept", "A", "amount", 10),
                row("name", "b", "dept", "A", "amount", 10),
                row("name", "c", "dept", "A", "amount", 5),
                row("name", "d", "dept", "B", "amount", 1)));
        List<Map<String, Object>> r = engine.query(
                "SELECT name, RANK() OVER (PARTITION BY dept ORDER BY amount DESC) AS rk, "
                        + "DENSE_RANK() OVER (ORDER BY amount DESC) AS drk FROM dup ORDER BY name");
        Map<String, Object> a = r.get(0), b = r.get(1), c = r.get(2), d = r.get(3);
        assertEquals(1L, ((Number) a.get("rk")).longValue());
        assertEquals(1L, ((Number) b.get("rk")).longValue());
        assertEquals(3L, ((Number) c.get("rk")).longValue());
        assertEquals(1L, ((Number) d.get("rk")).longValue());
        assertEquals(1L, ((Number) a.get("drk")).longValue());
        assertEquals(1L, ((Number) b.get("drk")).longValue());
        assertEquals(2L, ((Number) c.get("drk")).longValue());
        assertEquals(3L, ((Number) d.get("drk")).longValue());
    }

    // ==================== P2-1 TIMESTAMPDIFF ====================

    @Test
    public void r4_p2_1_timestampDiffUnits() {
        List<Map<String, Object>> r = engine.query(
                "SELECT TIMESTAMPDIFF('SECOND', ts, created) AS same, "
                        + "TIMESTAMPDIFF('MINUTE', ts, '2026-08-21 12:30:26') AS mins, "
                        + "TIMESTAMPDIFF('DAY', '2026-08-01 00:00:00', ts) AS days, "
                        + "TIMESTAMPDIFF('MONTH', '2026-01-21 00:00:00', ts) AS months "
                        + "FROM events WHERE id = 1");
        Map<String, Object> row0 = r.get(0);
        assertEquals(0L, ((Number) row0.get("same")).longValue());
        assertEquals(90L, ((Number) row0.get("mins")).longValue());
        assertEquals(20L, ((Number) row0.get("days")).longValue());
        assertEquals(7L, ((Number) row0.get("months")).longValue());
    }

    // ==================== P2-2 GROUP_CONCAT ====================

    @Test
    public void r4_p2_2_groupConcatDefaultSeparator() {
        List<Map<String, Object>> r = engine.query(
                "SELECT dept, GROUP_CONCAT(name) AS names FROM events GROUP BY dept ORDER BY dept");
        assertEquals(2, r.size());
        assertEquals("alice,carol", r.get(0).get("names"));
        assertEquals("bob", r.get(1).get("names"));
    }

    @Test
    public void r4_p2_2_groupConcatSeparatorAndDistinct() {
        engine.register("tags", Arrays.asList(
                row("id", 1, "tag", "red"), row("id", 2, "tag", "blue"),
                row("id", 3, "tag", "red")));
        List<Map<String, Object>> r = engine.query(
                "SELECT GROUP_CONCAT(tag SEPARATOR ' | ') AS joined, GROUP_CONCAT(DISTINCT tag) AS uniq FROM tags");
        assertEquals("red | blue | red", r.get(0).get("joined"));
        assertEquals("red,blue", r.get(0).get("uniq"));
    }

    // ==================== 支持面回归（FT 14 项抽样） ====================

    @Test
    public void r4_matrix_datediffNullifCastStillWork() {
        List<Map<String, Object>> r = engine.query(
                "SELECT DATEDIFF(ts, '2026-08-01 00:00:00') AS dd, NULLIF(dept, 'A') AS nd, "
                        + "CAST(amount AS DOUBLE) AS amt FROM events WHERE id = 3");
        assertEquals(31L, ((Number) r.get(0).get("dd")).longValue());
        assertNull(r.get(0).get("nd"));
        assertEquals(400.0, ((Number) r.get(0).get("amt")).doubleValue(), 1e-9);
    }

    @Test
    public void r4_matrix_ifCoalesceScalarFuncs() {
        List<Map<String, Object>> r = engine.query(
                "SELECT IF(amount > 500, 'hi', 'lo') AS tag, COALESCE(NULL, dept) AS d, "
                        + "UPPER(name) AS un, ROUND(amount / 3, 1) AS r3 FROM events WHERE id = 2");
        assertEquals("hi", r.get(0).get("tag"));
        assertEquals("B", r.get(0).get("d"));
        assertEquals("BOB", r.get(0).get("un"));
        assertEquals(266.7, ((Number) r.get(0).get("r3")).doubleValue(), 1e-9);
    }

    @Test
    public void r4_matrix_havingGroupAggregates() {
        List<Map<String, Object>> r = engine.query(
                "SELECT dept, SUM(amount) AS total FROM events GROUP BY dept HAVING SUM(amount) >= 500 ORDER BY dept");
        assertEquals(2, r.size());
        assertEquals(500L, ((Number) r.get(0).get("total")).longValue());
        assertEquals(800L, ((Number) r.get(1).get("total")).longValue());
    }

    private static Map<String, Object> row(Object... kv) {
        java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
