package com.zifang.util.expr.obj;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.zifang.util.expr.obj.Fixture.asLong;
import static com.zifang.util.expr.obj.Fixture.asMap;
import static com.zifang.util.expr.obj.Fixture.asRows;
import static com.zifang.util.expr.obj.Fixture.map;
import static com.zifang.util.expr.obj.Fixture.program;
import static com.zifang.util.expr.obj.Fixture.row;
import static com.zifang.util.expr.obj.Fixture.rows;
import static com.zifang.util.expr.obj.Fixture.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 整形算子：二维表 → 嵌套对象 / 键值映射 / 树 / 矩阵。
 *
 * @author zifang
 */
public class ObjShapeTest {

    private final List<Map<String, Object>> sales = rows(
            row("region", "华东", "city", "杭州", "amount", 120),
            row("region", "华东", "city", "上海", "amount", 80),
            row("region", "华南", "city", "深圳", "amount", 300),
            row("region", "华南", "city", "广州", "amount", 300),
            row("region", "华北", "city", "北京", "amount", 50));

    @Test
    @SuppressWarnings("unchecked")
    public void groupByBucketsRowsAndAggregates() {
        Object out = run(map("op", "group", "by", "region", "items", "lines",
                "agg", map("total", "SUM(amount)", "n", "COUNT(*)")), sales);
        List<Map<String, Object>> groups = (List<Map<String, Object>>) out;
        assertEquals(3, groups.size());
        assertEquals("华东", groups.get(0).get("region"));
        assertEquals(200L, asLong(groups.get(0).get("total")));
        assertEquals(2L, asLong(groups.get(0).get("n")));
        assertEquals(2, ((List<Object>) groups.get(0).get("lines")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void groupIntoTemplateBuildsNestedDocument() {
        Object out = run(map("op", "group", "by", "region", "items", "lines",
                "agg", map("total", "SUM(amount)", "cities", "COUNT(DISTINCT city)"),
                "into", map("region", "${region}", "total", "${total}", "cities", "${cities}",
                        "lines", map("op", "map", "of", "lines",
                                "into", map("city", "${city}", "amount", "${amount}")))), sales);
        List<Map<String, Object>> groups = (List<Map<String, Object>>) out;
        assertEquals(3, groups.size());
        Map<String, Object> east = groups.get(0);
        assertEquals("华东", east.get("region"));
        assertEquals(200L, asLong(east.get("total")));
        assertEquals(2L, asLong(east.get("cities")));
        // 模板里的 {"op": ...} 是嵌套子程序: 高维结构就靠这一条规则递归长出来
        List<Object> lines = (List<Object>) east.get("lines");
        assertEquals(2, lines.size());
        assertEquals("杭州", asMap(lines.get(0)).get("city"));
        assertFalse("模板没写的列不该出现在产出里", east.containsKey("amount"));
    }

    @Test
    public void groupAsMapKeysByTheFirstGroupColumn() {
        Object out = run(map("op", "group", "by", "region", "as", "map",
                "agg", map("total", "SUM(amount)")), sales);
        Map<String, Object> keyed = asMap(out);
        assertEquals(Arrays.asList("华东", "华南", "华北"), Arrays.asList(keyed.keySet().toArray()));
        assertEquals(600L, asLong(asMap(keyed.get("华南")).get("total")));
    }

    @Test
    public void foldCollapsesTheWholeTableIntoOneObject() {
        Object out = run(map("op", "fold", "agg", map("total", "SUM(amount)", "avg", "AVG(amount)",
                "biggest", "MAX(city)")), sales);
        Map<String, Object> doc = asMap(out);
        assertEquals(850L, asLong(doc.get("total")));
        assertEquals(170d, ((Number) doc.get("avg")).doubleValue(), 0.001d);
        assertEquals("深圳", doc.get("biggest"));
    }

    @Test
    public void foldOnEmptyTableStillGivesOneRowLikeSql() {
        Object out = run(map("op", "fold", "agg", map("total", "SUM(amount)", "n", "COUNT(*)")),
                rows());
        Map<String, Object> doc = asMap(out);
        assertEquals(0L, asLong(doc.get("total")));
        assertEquals(0L, asLong(doc.get("n")));
    }

    @Test
    public void sumOfAllIntegralValuesStaysALong() {
        Object out = run(map("op", "fold", "agg", map("whole", "SUM(qty)")),
                rows(row("qty", 2), row("qty", 3)));
        assertTrue(asMap(out).get("whole") instanceof Long);

        Object mixed = run(map("op", "fold", "agg", map("half", "SUM(qty)")),
                rows(row("qty", 2.5), row("qty", 3)));
        assertTrue(asMap(mixed).get("half") instanceof Double);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void collectAggKeepsDetailValuesAsArray() {
        Object out = run(map("op", "group", "by", "region",
                "agg", map("cities", "COLLECT(city)")), sales);
        List<Object> east = (List<Object>) asRows(out).get(0).get("cities");
        assertEquals(Arrays.asList("杭州", "上海"), east);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mapOverRowsProducesObjectArray() {
        Object out = run(map("op", "map", "into", map("label", "${city}", "link", "https://x/${city}")), sales);
        List<Map<String, Object>> list = (List<Map<String, Object>>) out;
        assertEquals("杭州", list.get(0).get("label"));
        assertEquals("https://x/杭州", list.get(0).get("link"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void keyByTurnsRowsIntoALookupObject() {
        Object out = run(map("op", "keyBy", "key", "city", "into", "${amount}"), sales);
        Map<String, Object> doc = asMap(out);
        assertEquals(120L, asLong(doc.get("杭州")));
        assertEquals(5, doc.size());
    }

    @Test
    public void keyByRefusesToDropRowsSilently() {
        try {
            run(map("op", "keyBy", "key", "region"), sales);
            fail("key 重复时不能静默覆盖");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("keyBy 的 key 重复"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void treeNestsFlatParentIdRows() {
        List<Map<String, Object>> menu = rows(
                row("id", 1, "parentId", null, "name", "报表"),
                row("id", 2, "parentId", 1, "name", "销售"),
                row("id", 3, "parentId", 2, "name", "明细"),
                row("id", 4, "parentId", 1, "name", "库存"),
                row("id", 5, "parentId", 99, "name", "孤儿"));
        Object out = run(map("op", "tree", "id", "id", "parent", "parentId"), menu);
        List<Object> roots = (List<Object>) out;
        assertEquals(2, roots.size());
        Map<String, Object> report = asMap(roots.get(0));
        assertEquals("报表", report.get("name"));
        List<Object> children = (List<Object>) report.get("children");
        assertEquals(2, children.size());
        Map<String, Object> branch = asMap(children.get(0));
        assertEquals("销售", branch.get("name"));
        assertEquals("明细", asMap(((List<Object>) branch.get("children")).get(0)).get("name"));
        // 叶子节点不带空 children 数组, 前端 tree 才不会渲染出可展开的空箭头
        assertFalse(asMap(children.get(1)).containsKey("children"));
        // 父不存在的行按根处理: 脏数据要能看出树, 而不是整棵树消失
        assertEquals("孤儿", asMap(roots.get(1)).get("name"));
    }

    @Test
    public void treeRejectsDuplicateIds() {
        try {
            run(map("op", "tree", "id", "region", "parent", "city"), sales);
            fail("id 重复无法建树");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("id 重复"));
        }
    }

    @Test
    public void pivotTurnsLongRowsIntoAMatrix() {
        List<Map<String, Object>> longRows = rows(
                row("city", "杭州", "quarter", "Q1", "amount", 10),
                row("city", "杭州", "quarter", "Q2", "amount", 20),
                row("city", "上海", "quarter", "Q1", "amount", 30));
        Object out = run(map("op", "pivot", "by", "city", "on", "quarter", "value", "amount"), longRows);
        List<Map<String, Object>> matrix = asRows(out);
        assertEquals(map("city", "杭州", "Q1", 10L, "Q2", 20L), matrix.get(0));
        // 缺格子补 null: 折线图上就是断点, 而不是误报 0
        assertNull(matrix.get(1).get("Q2"));
    }

    @Test
    public void pivotCountFillsMissingCellsWithZero() {
        List<Map<String, Object>> longRows = rows(
                row("city", "杭州", "quarter", "Q1", "amount", 10),
                row("city", "杭州", "quarter", "Q2", "amount", 30),
                row("city", "上海", "quarter", "Q1", "amount", 20));
        Object out = run(map("op", "pivot", "by", "city", "on", "quarter", "agg", "COUNT"), longRows);
        List<Map<String, Object>> matrix = asRows(out);
        assertEquals(1L, asLong(matrix.get(0).get("Q2")));
        // COUNT 的空格子是真的"一单都没有", 补 0 而不是 null
        assertEquals(0L, asLong(matrix.get(1).get("Q2")));
    }

    @Test
    public void mapOfMissingFieldFailsInsteadOfReturningEmptyArray() {
        try {
            run(map("op", "map", "of", "line", "into", map("city", "${city}")), map("region", "华东", "lines", sales));
            fail("of 字段名写错必须报错");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("取不到数组"));
            assertTrue(e.getMessage(), e.getMessage().contains("lines"));
        }
    }

    @Test
    public void unpivotIsTheInverseOfPivot() {
        List<Map<String, Object>> wide = rows(row("city", "杭州", "Q1", 10, "Q2", 20));
        Object out = run(map("op", "unpivot", "keep", "city",
                "as", map("name", "quarter", "value", "amount")), wide);
        assertEquals(map("city", "杭州", "quarter", "Q1", "amount", 10), asRows(out).get(0));
        assertEquals(2, asRows(out).size());
    }

    @Test
    public void setWritesAlongAPathWithoutTouchingInputRows() {
        Map<String, Object> nested = map("region", "华东", "metrics", map("n", 2));
        Object out = new ObjEngine().shape(program(
                map("op", "from", "rows", Arrays.asList(nested)),
                map("op", "set", "path", "metrics.avg", "expr", "100 / metrics.n")), null);
        assertEquals(50d, ((Number) asMap(asMap(asRows(out).get(0)).get("metrics")).get("avg")).doubleValue(), 0.001d);
        // 原行数据被复用给多条管道时, 就地写会串味
        assertFalse(nested.toString(), asMap(nested.get("metrics")).containsKey("avg"));
    }

    @Test
    public void getReadsAlongAPath() {
        Object out = new ObjEngine().shape(program(
                map("op", "from", "value", map("a", map("b", Arrays.asList(1, 2, 3)))),
                map("op", "get", "path", "a.b.1")), null);
        assertEquals(2, out);
    }

    @Test
    public void illegalAggExprFailsInsteadOfReturningNull() {
        try {
            run(map("op", "fold", "agg", map("total", "amount")), sales);
            fail("聚合槽位写成裸列名时必须报错");
        } catch (ObjException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("SUM(amount)"));
        }
    }
}
