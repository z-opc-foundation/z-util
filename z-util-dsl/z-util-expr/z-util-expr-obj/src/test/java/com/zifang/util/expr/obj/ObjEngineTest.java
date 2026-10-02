package com.zifang.util.expr.obj;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.zifang.util.expr.obj.Fixture.asDouble;
import static com.zifang.util.expr.obj.Fixture.asLong;
import static com.zifang.util.expr.obj.Fixture.asMap;
import static com.zifang.util.expr.obj.Fixture.asRows;
import static com.zifang.util.expr.obj.Fixture.map;
import static com.zifang.util.expr.obj.Fixture.program;
import static com.zifang.util.expr.obj.Fixture.row;
import static com.zifang.util.expr.obj.Fixture.rows;
import static com.zifang.util.expr.obj.Fixture.run;
import static com.zifang.util.expr.obj.Fixture.tables;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 行级算子：取数、投影、过滤、排序、截断。
 *
 * @author zifang
 */
public class ObjEngineTest {

    private final List<Map<String, Object>> sales = rows(
            row("region", "华东", "city", "杭州", "amount", 120, "qty", 3),
            row("region", "华东", "city", "上海", "amount", 80, "qty", 2),
            row("region", "华南", "city", "深圳", "amount", 300, "qty", 5),
            row("region", "华北", "city", "北京", "amount", 50, "qty", 1));

    @Test
    public void fromRowsStartsThePipeline() {
        Object out = new ObjEngine().shape(map("op", "from", "rows", sales));
        assertEquals(4, asRows(out).size());
    }

    @Test
    public void fromTableReadsThroughTheTableSource() {
        ObjEngine engine = new ObjEngine(TableSource.of(tables("sales", sales)));
        assertEquals(4, asRows(engine.shape(map("op", "from", "table", "sales"))).size());
        // 表名不存在时按空表处理: 报表里查不到数据应该是空页, 不是 500
        assertTrue(asRows(engine.shape(map("op", "from", "table", "nope"))).isEmpty());
    }

    @Test
    public void fromWithoutSourceFailsLoud() {
        try {
            new ObjEngine().shape(map("op", "from", "sql", "select 1"));
            fail("未绑定 TableSource 时 from.sql 必须报错");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("from.sql"));
        }
    }

    @Test
    public void selectKeepsOnlyListedColumns() {
        Object out = run(map("op", "select", "fields", Arrays.asList("city", "amount")), sales);
        assertEquals(map("city", "杭州", "amount", 120), asRows(out).get(0));
    }

    @Test
    public void selectMapFormEvaluatesElPerColumn() {
        Object out = run(map("op", "select", "fields", map("城市", "city", "单价", "amount / qty")), sales);
        assertEquals("杭州", asRows(out).get(0).get("城市"));
        assertEquals(40d, asDouble(asRows(out).get(0).get("单价")), 0.001d);
    }

    @Test
    public void whereFiltersWithRowAsElRoot() {
        Object out = run(map("op", "where", "expr", "amount > 100 && region == '华南'"), sales);
        assertEquals(1, asRows(out).size());
        assertEquals("深圳", asRows(out).get(0).get("city"));
    }

    @Test
    public void orderByStringSuffixControlsDirection() {
        Object out = run(map("op", "order", "by", Arrays.asList("region", "amount desc")), sales);
        assertEquals("杭州", asRows(out).get(0).get("city"));
        assertEquals("上海", asRows(out).get(1).get("city"));
    }

    @Test
    public void orderByExpressionAndExplicitDir() {
        Object out = run(map("op", "order", "by", map("expr", "amount / qty", "dir", "desc")), sales);
        // 单价: 深圳 60 > 北京 50 > 杭州 40 = 上海 40
        assertEquals("深圳", asRows(out).get(0).get("city"));
        assertEquals("北京", asRows(out).get(1).get("city"));
        // 同键保持输入顺序: 稳定排序不许把并列的行打乱
        assertEquals("杭州", asRows(out).get(2).get("city"));
        assertEquals("上海", asRows(out).get(3).get("city"));
    }

    @Test
    public void orderPutsNullsLast() {
        List<Map<String, Object>> withNull = rows(row("n", 2), row("n", null), row("n", 1));
        Object out = run(map("op", "order", "by", "n"), withNull);
        assertEquals(3, asRows(out).size());
        assertNull(asRows(out).get(2).get("n"));

        // desc 不许把空桶翻到最前面: "取最高的那一档" 拿回一个 null 是最难查的错
        Object descOut = run(map("op", "order", "by", "n desc"), withNull);
        assertEquals(2L, asLong(asRows(descOut).get(0).get("n")));
        assertNull(asRows(descOut).get(2).get("n"));
    }

    @Test
    public void limitSlidesOverRows() {
        Object out = run(program(map("op", "order", "by", "amount desc"),
                map("op", "limit", "n", 2, "offset", 1)), sales);
        assertEquals("杭州", asRows(out).get(0).get("city"));
        assertEquals("上海", asRows(out).get(1).get("city"));
    }

    @Test
    public void oneTakesTheHeadRow() {
        Object out = run(program(map("op", "order", "by", "amount desc"), map("op", "one")), sales);
        assertEquals("深圳", asMap(out).get("city"));
    }

    @Test
    public void bareTemplateAtProgramPositionMapsEveryRow() {
        Object out = run(map("city", "${city}"), sales);
        assertEquals("杭州", asMap(asRows(out).get(0)).get("city"));
        assertEquals(4, asRows(out).size());
    }

    @Test
    public void unknownOpListsWhatExists() {
        try {
            run(map("op", "sort"), sales);
            fail("未知 op 必须报错");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("pivot"));
        }
    }

    @Test
    public void rowOpOnNonTableExplainsHowToFixIt() {
        try {
            run(map("op", "group", "by", "region"), map("scalar", 1));
            fail("对象输入要走别的算子");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("二维行数组"));
        }
    }

    @Test
    public void fromValueAllowsNonTableRowInput() {
        Object out = new ObjEngine().shape(program(
                map("op", "from", "value", map("a", 1, "b", 2)),
                map("op", "set", "path", "sum", "expr", "a + b")));
        assertEquals(3L, asLong(asMap(out).get("sum")));
    }

}
