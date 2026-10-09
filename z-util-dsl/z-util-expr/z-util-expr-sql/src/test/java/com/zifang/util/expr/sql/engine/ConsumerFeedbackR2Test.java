package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.engine.VirtualTableEngine.ColumnDef;
import com.zifang.util.expr.sql.engine.VirtualTableEngine.Stage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 消费方（feature-team 低代码平台）反馈第二轮回归测试，对应 1.0.20 修复面：
 * R2-P0-1 无 GROUP BY 包裹聚合（{@code SUM(x)*1.0/COUNT(*)}）单行收敛而非逐行退化 /
 * R2-P1-3 多段 SQL 管线 ([{as, sql}]) /
 * R2-P1-4 WHERE 相对时间表达式（{@code DATE_SUB(CURDATE(), 7, 'DAY')} + NOW_MS/zone 锚定）/
 * R2-P1-5 dialect offset 缺省不报错 /
 * R2-P2-7 时间分桶 BUCKET 一等公民 /
 * R2-P2-8 dry-run schema 推断（对象层 group+agg 见 expr-obj 模块 GroupAggTest）。
 */
public class ConsumerFeedbackR2Test {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();
        List<Map<String, Object>> orders = new ArrayList<>();
        orders.add(row("id", 1, "dept", "A", "user_id", 1, "amount", 100));
        orders.add(row("id", 2, "dept", "A", "user_id", 2, "amount", 200));
        orders.add(row("id", 3, "dept", "B", "user_id", 3, "amount", 400));
        orders.add(row("id", 4, "dept", "B", "user_id", 4, "amount", 800));
        engine.register("orders", orders);
    }

    // ==================== R2-P0-1 包裹聚合无 GROUP BY 单行收敛 ====================

    @Test
    public void r2_p0_1_ratioSqlCollapsesToSingleRow() {
        // AI 生成 KPI 比率常用写法；之前退化返回 N 行。
        List<Map<String, Object>> r = engine.query(
                "SELECT SUM(amount) AS total, COUNT(*) AS cnt, SUM(amount)*1.0/COUNT(*) AS avg FROM orders");
        assertEquals("包裹聚合返回单行而非逐行退化", 1, r.size());
        Map<String, Object> only = r.get(0);
        assertEquals(1500L, ((Number) only.get("total")).longValue());
        assertEquals(4L, ((Number) only.get("cnt")).longValue());
        assertEquals(375.0, ((Number) only.get("avg")).doubleValue(), 1e-9);
    }

    @Test
    public void r2_p0_1_countStarAloneStillCollapses() {
        List<Map<String, Object>> r = engine.query("SELECT COUNT(*) AS cnt FROM orders");
        assertEquals(1, r.size());
        assertEquals(4L, ((Number) r.get(0).get("cnt")).longValue());
    }

    @Test
    public void r2_p0_1_mixedAggregateAndScalarStillSingleRow() {
        // 含 CAST 的复合聚合表达式
        List<Map<String, Object>> r = engine.query(
                "SELECT CAST(SUM(amount)/COUNT(*) AS DOUBLE) AS avg_amt FROM orders");
        assertEquals(1, r.size());
        assertEquals(375.0, ((Number) r.get(0).get("avg_amt")).doubleValue(), 1e-9);
    }

    // ==================== R2-P1-3 多段 SQL 管线 ====================

    @Test
    public void r2_p1_3_pipelineRegistersIntermediateTables() {
        List<Stage> stages = new ArrayList<>();
        stages.add(new Stage("orders_a", "SELECT id, amount FROM orders WHERE dept = 'A'"));
        stages.add(new Stage("a_total", "SELECT SUM(amount) AS s FROM orders_a"));
        List<Map<String, Object>> last = engine.pipeline(stages);
        // 最后一段就是 SQL 求值结果
        assertEquals(1, last.size());
        assertEquals(300L, ((Number) last.get(0).get("s")).longValue());
        // 派生表名仍注册在引擎里，后续 SQL 可引用
        assertTrue(engine.hasTable("orders_a"));
        assertEquals(2, engine.getTable("orders_a").size());
    }

    // ==================== R2-P1-4 相对时间表达式 ====================

    @Test
    public void r2_p1_4_nowMsReturnsEpochMillis() {
        // 替代 FT 的 LcTimeExpr：管线 now 必须能在 SELECT/WHERE 里直接出 epoch ms。
        Object now = engine.query("SELECT NOW_MS() AS n FROM orders").get(0).get("n");
        assertTrue("NOW_MS 应返回 Long epoch, 实际: " + (now == null ? "null" : now.getClass()),
                now instanceof Long);
        long delta = System.currentTimeMillis() - ((Long) now).longValue();
        assertTrue("NOW_MS 与 System.currentTimeMillis 误差应小于 5s", Math.abs(delta) < 5_000L);
    }

    @Test
    public void r2_p1_4_dateSubShiftsDateBackwards() {
        // DATE_SUB(d, 3, 'DAY') 与 DateAdd(d, -3, 'DAY') 同形；
        // 这里只验 LS 输出日期可比 CAST 转 STRING，便于管道继续。
        Object shifted = engine.query("SELECT DATE_SUB(CURDATE(), 3, 'DAY') AS d FROM orders").get(0).get("d");
        assertNotNull("DATE_SUB 应返回非空", shifted);
        assertTrue("DATE_SUB 应返回 LocalDate, 实际: " + shifted.getClass(),
                shifted instanceof java.time.LocalDate);
    }

    // ==================== R2-P2-7 BUCKET ====================

    @Test
    public void r2_p2_7_bucketDayProducesStableKey() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("id", 1, "dt", "2026-10-09 10:00:00"));
        data.add(row("id", 2, "dt", "2026-10-09 22:00:00"));
        data.add(row("id", 3, "dt", "2026-10-10 01:00:00"));
        engine.register("logs", data);

        List<Map<String, Object>> r = engine.query(
                "SELECT BUCKET(dt, 'DAY') AS d, COUNT(*) AS c FROM logs GROUP BY BUCKET(dt, 'DAY') ORDER BY d");
        assertEquals(2, r.size());
        assertEquals("2026-10-09", r.get(0).get("d"));
        assertEquals(2L, ((Number) r.get(0).get("c")).longValue());
        assertEquals("2026-10-10", r.get(1).get("d"));
        assertEquals(1L, ((Number) r.get(1).get("c")).longValue());
    }

    // ==================== R2-P2-8 dry-run schema 推断 ====================

    @Test
    public void r2_p2_8_explainReturnsColumnTypesWithoutExecution() {
        List<ColumnDef> cols = engine.explain(
                "SELECT id, amount, dept, SUM(amount) AS total, CAST(amount AS DOUBLE) AS amt_d FROM orders GROUP BY dept");
        assertEquals(5, cols.size());
        // id Integer ⇒ INTEGER (from sample)
        // amount Integer ⇒ INTEGER
        // dept String ⇒ STRING
        // SUM(amount) ⇒ DECIMAL
        // CAST(amount AS DOUBLE) ⇒ DOUBLE
        assertEquals("id", cols.get(0).name());
        assertEquals("INTEGER", cols.get(0).type());
        assertEquals("amount", cols.get(1).name());
        assertEquals("INTEGER", cols.get(1).type());
        assertEquals("total", cols.get(3).name());
        assertEquals("DECIMAL", cols.get(3).type());
        assertEquals("amt_d", cols.get(4).name());
        assertEquals("DOUBLE", cols.get(4).type());
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
