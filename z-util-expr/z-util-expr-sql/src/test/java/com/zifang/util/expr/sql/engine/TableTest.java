package com.zifang.util.expr.sql.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Table 类测试。
 */
public class TableTest {

    private Table users;

    @Before
    public void setUp() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("id", 1, "name", "Alice", "age", 30, "dept", "eng"));
        data.add(row("id", 2, "name", "Bob", "age", 25, "dept", "sales"));
        data.add(row("id", 3, "name", "Charlie", "age", 35, "dept", "eng"));
        data.add(row("id", 4, "name", "Diana", "age", 28, "dept", "sales"));
        data.add(row("id", 5, "name", "Eve", "age", 32, "dept", "eng"));
        users = Table.fromMaps("users", data);
    }

    // ==================== 基础 ====================

    @Test
    public void testBasicInfo() {
        assertEquals("users", users.getName());
        assertEquals(5, users.size());
        assertFalse(users.isEmpty());
        assertEquals(4, users.getColumnNames().size());
    }

    @Test
    public void testRowAccess() {
        assertEquals("Alice", users.getRow(0).getString("name"));
        assertEquals("Eve", users.last().getString("name"));
        assertEquals("Alice", users.first().getString("name"));
    }

    // ==================== 行追加/删除 ====================

    @Test
    public void testAddRow() {
        users.addRow(Row.of("id", 6, "name", "Frank", "age", 40, "dept", "hr"));
        assertEquals(6, users.size());
    }

    @Test
    public void testAddRowVarargs() {
        users.addRow("id", 6, "name", "Frank", "age", 40, "dept", "hr");
        assertEquals(6, users.size());
        assertEquals("Frank", users.last().getString("name"));
    }

    @Test
    public void testRemoveRow() {
        users.removeRow(0);
        assertEquals(4, users.size());
        assertEquals("Bob", users.first().getString("name"));
    }

    // ==================== 列操作 ====================

    @Test
    public void testAddColumn() {
        users.addColumn("salary", 50000);
        assertTrue(users.getColumnNames().contains("salary"));
        assertEquals(50000, users.getRow(0).getInt("salary"));
    }

    @Test
    public void testAddColumnComputed() {
        users.addColumn("age_doubled", row -> row.getInt("age") * 2);
        assertEquals(60, users.getRow(0).getInt("age_doubled"));
        assertEquals(50, users.getRow(1).getInt("age_doubled"));
    }

    @Test
    public void testDropColumn() {
        users.dropColumn("dept");
        assertFalse(users.getColumnNames().contains("dept"));
    }

    @Test
    public void testRenameColumn() {
        users.renameColumn("name", "username");
        assertTrue(users.getColumnNames().contains("username"));
        assertFalse(users.getColumnNames().contains("name"));
        assertEquals("Alice", users.getRow(0).getString("username"));
    }

    // ==================== 数据操作 ====================

    @Test
    public void testWherePredicate() {
        Table result = users.where(row -> row.getInt("age") > 30);
        assertEquals(2, result.size());
    }

    @Test
    public void testWhereEquals() {
        Table result = users.where("dept", "eng");
        assertEquals(3, result.size());
    }

    @Test
    public void testSort() {
        Table sorted = users.sort("age", true);
        assertEquals("Bob", sorted.first().getString("name"));   // 25
        assertEquals("Charlie", sorted.last().getString("name")); // 35
    }

    @Test
    public void testSortDesc() {
        Table sorted = users.sort("age", false);
        assertEquals("Charlie", sorted.first().getString("name")); // 35
    }

    @Test
    public void testLimit() {
        Table limited = users.limit(2);
        assertEquals(2, limited.size());
    }

    @Test
    public void testLimitOffset() {
        Table limited = users.limit(2, 1);
        assertEquals(2, limited.size());
        assertEquals("Bob", limited.first().getString("name"));
    }

    @Test
    public void testDistinct() {
        users.addRow(Row.of("id", 6, "name", "Alice2", "age", 30, "dept", "eng"));
        Table distinct = users.distinct("dept");
        assertEquals(2, distinct.size());  // eng, sales
    }

    // ==================== 列投影 ====================

    @Test
    public void testSelect() {
        Table projected = users.select("name", "age");
        assertEquals(2, projected.getColumnNames().size());
        assertTrue(projected.getColumnNames().contains("name"));
        assertTrue(projected.getColumnNames().contains("age"));
        assertEquals("Alice", projected.getRow(0).getString("name"));
    }

    // ==================== 聚合 ====================

    @Test
    public void testCount() {
        assertEquals(5, users.count());
    }

    @Test
    public void testMax() {
        assertEquals(35, ((Number) users.max("age")).intValue());
    }

    @Test
    public void testMin() {
        assertEquals(25, ((Number) users.min("age")).intValue());
    }

    @Test
    public void testSum() {
        assertEquals(150.0, ((Number) users.sum("age")).doubleValue(), 0.001);
    }

    @Test
    public void testAvg() {
        assertEquals(30.0, ((Number) users.avg("age")).doubleValue(), 0.001);
    }

    @Test
    public void testAggregate() {
        assertEquals(5L, users.aggregate("age", "COUNT"));
        assertEquals(150.0, ((Number) users.aggregate("age", "SUM")).doubleValue(), 0.001);
    }

    // ==================== 分组 ====================

    @Test
    public void testGroupBy() {
        Map<String, Table> groups = users.groupBy("dept");
        assertEquals(2, groups.size());
        assertEquals(3, groups.get("eng").size());
        assertEquals(2, groups.get("sales").size());
    }

    // ==================== 索引 ====================

    @Test
    public void testCreateAndLookupIndex() {
        users.createIndex("id");
        assertTrue(users.hasIndex("id"));
        List<Row> result = users.lookupByIndex("id", 3);
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getString("name"));
    }

    @Test
    public void testIndexNoMatch() {
        users.createIndex("id");
        List<Row> result = users.lookupByIndex("id", 999);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testBatchCreateIndex() {
        users.createIndex("id", "name", "dept");
        assertTrue(users.hasIndex("id"));
        assertTrue(users.hasIndex("name"));
        assertTrue(users.hasIndex("dept"));
    }

    // ==================== SQL 查询 ====================

    @Test
    public void testQuery() {
        Table result = users.query("SELECT * WHERE age > 30");
        assertEquals(2, result.size());
    }

    // ==================== 转换 ====================

    @Test
    public void testToMapList() {
        List<Map<String, Object>> list = users.toMapList();
        assertEquals(5, list.size());
        assertEquals("Alice", list.get(0).get("name"));
    }

    @Test
    public void testMapFunction() {
        List<String> names = users.map(row -> row.getString("name"));
        assertEquals(5, names.size());
        assertEquals("Alice", names.get(0));
    }

    // ==================== 格式化 ====================

    @Test
    public void testHeadTail() {
        Table head = users.head(2);
        assertEquals(2, head.size());
        assertEquals("Alice", head.first().getString("name"));

        Table tail = users.tail(2);
        assertEquals(2, tail.size());
        assertEquals("Eve", tail.last().getString("name"));
    }

    @Test
    public void testToPrettyString() {
        String s = users.toPrettyString();
        assertTrue(s.contains("Table: users"));
        assertTrue(s.contains("5 rows"));
        assertTrue(s.contains("Alice"));
    }

    @Test
    public void testToString() {
        String s = users.toString();
        assertTrue(s.contains("users"));
        assertTrue(s.contains("rows=5"));
    }

    // ==================== 互操作 ====================

    @Test
    public void testToVirtualTable() {
        VirtualTable vt = users.toVirtualTable();
        assertEquals(5, vt.getRowCount());
    }

    @Test
    public void testFromVirtualTable() {
        VirtualTable vt = users.toVirtualTable();
        Table restored = Table.fromVirtualTable(vt, "restored");
        assertEquals(5, restored.size());
        assertEquals("Alice", restored.first().getString("name"));
    }

    @Test
    public void testFromMaps() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("x", 1));
        Table t = Table.fromMaps("test", data);
        assertEquals("test", t.getName());
        assertEquals(1, t.size());
    }

    // ==================== 迭代 ====================

    @Test
    public void testIterable() {
        int count = 0;
        for (Row row : users) {
            assertNotNull(row.getString("name"));
            count++;
        }
        assertEquals(5, count);
    }

    // ==================== 链式调用 ====================

    @Test
    public void testChainedOperations() {
        List<String> names = users
                .where("dept", "eng")
                .sort("age", false)
                .limit(2)
                .select("name")
                .map(row -> row.getString("name"));
        assertEquals(2, names.size());
        assertEquals("Charlie", names.get(0)); // 35
        assertEquals("Eve", names.get(1));      // 32
    }

    // ==================== 辅助 ====================

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }
}
