package com.zifang.util.expr.sql.engine;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Row 类测试。
 */
public class RowTest {

    // ==================== 构造 ====================

    @Test
    public void testEmptyRow() {
        Row row = new Row();
        assertTrue(row.isEmpty());
        assertEquals(0, row.size());
    }

    @Test
    public void testVarargsConstructor() {
        Row row = Row.of("id", 1, "name", "Alice", "score", 95.5);
        assertEquals(3, row.size());
        assertEquals(1, row.getInt("id"));
        assertEquals("Alice", row.getString("name"));
        assertEquals(95.5, row.getDouble("score"), 0.001);
    }

    @Test
    public void testMapConstructor() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 10);
        map.put("label", "test");
        Row row = new Row(map);
        assertEquals(10, row.getInt("id"));
        assertEquals("test", row.getString("label"));
        // 共享底层 Map
        map.put("extra", "val");
        assertTrue(row.has("extra"));
    }

    @Test
    public void testFromMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", 42);
        Row row = Row.fromMap(map);
        assertEquals(42, row.getInt("x"));
    }

    // ==================== 类型化访问器 ====================

    @Test
    public void testGetString() {
        Row row = Row.of("name", "Bob", "num", 42);
        assertEquals("Bob", row.getString("name"));
        assertEquals("42", row.getString("num"));
        assertNull(row.getString("missing"));
    }

    @Test
    public void testGetStringWithDefault() {
        Row row = Row.of("name", "Bob");
        assertEquals("Bob", row.getString("name", "default"));
        assertEquals("default", row.getString("missing", "default"));
    }

    @Test
    public void testGetInt() {
        Row row = Row.of("a", 10, "b", 3.7, "c", "99");
        assertEquals(10, row.getInt("a"));
        assertEquals(3, row.getInt("b"));  // double -> int 截断
        assertEquals(99, row.getInt("c"));
    }

    @Test
    public void testGetIntWithDefault() {
        Row row = Row.of("a", 10);
        assertEquals(10, row.getInt("a", 0));
        assertEquals(-1, row.getInt("missing", -1));
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetIntThrowsOnMissing() {
        Row row = Row.empty();
        row.getInt("missing");
    }

    @Test
    public void testGetLong() {
        Row row = Row.of("big", 9999999999L, "small", 5);
        assertEquals(9999999999L, row.getLong("big"));
        assertEquals(5L, row.getLong("small"));
    }

    @Test
    public void testGetLongWithDefault() {
        Row row = Row.empty();
        assertEquals(42L, row.getLong("missing", 42L));
    }

    @Test
    public void testGetDouble() {
        Row row = Row.of("pi", 3.14, "int", 7);
        assertEquals(3.14, row.getDouble("pi"), 0.001);
        assertEquals(7.0, row.getDouble("int"), 0.001);
    }

    @Test
    public void testGetDoubleWithDefault() {
        Row row = Row.empty();
        assertEquals(1.5, row.getDouble("missing", 1.5), 0.001);
    }

    @Test
    public void testGetFloat() {
        Row row = Row.of("f", 2.5f);
        assertEquals(2.5f, row.getFloat("f"), 0.001f);
    }

    @Test
    public void testGetFloatWithDefault() {
        Row row = Row.empty();
        assertEquals(9.9f, row.getFloat("missing", 9.9f), 0.001f);
    }

    @Test
    public void testGetBoolean() {
        Row row = Row.of("flag", true, "zero", 0, "one", 1, "text", "true");
        assertTrue(row.getBoolean("flag"));
        assertFalse(row.getBoolean("zero"));
        assertTrue(row.getBoolean("one"));
        assertTrue(row.getBoolean("text"));
    }

    @Test
    public void testGetBooleanWithDefault() {
        Row row = Row.empty();
        assertTrue(row.getBoolean("missing", true));
        assertFalse(row.getBoolean("missing", false));
    }

    @Test
    public void testGetAs() {
        Row row = Row.of("id", 42, "name", "test", "flag", true);
        assertEquals(Integer.valueOf(42), row.getAs("id", Integer.class));
        assertEquals("test", row.getAs("name", String.class));
        assertEquals(Long.valueOf(42), row.getAs("id", Long.class));
        assertEquals(Double.valueOf(42.0), row.getAs("id", Double.class));
        assertTrue(row.getAs("flag", Boolean.class));
        assertNull(row.getAs("missing", String.class));
    }

    // ==================== 判空/存在 ====================

    @Test
    public void testIsNull() {
        Row row = Row.of("a", 1, "b", null);
        assertTrue(row.isNull("b"));
        assertTrue(row.isNull("missing"));
        assertFalse(row.isNull("a"));
    }

    @Test
    public void testIsNotNull() {
        Row row = Row.of("a", 1, "b", null);
        assertTrue(row.isNotNull("a"));
        assertFalse(row.isNotNull("b"));
        assertFalse(row.isNotNull("missing"));
    }

    @Test
    public void testHas() {
        Row row = Row.of("x", 1);
        assertTrue(row.has("x"));
        assertFalse(row.has("y"));
    }

    // ==================== 链式写入 ====================

    @Test
    public void testChainedPut() {
        Row row = new Row();
        Row result = row.set("a", 1).set("b", 2).set("c", 3);
        assertSame(row, result);
        assertEquals(3, row.size());
        assertEquals(1, row.getInt("a"));
    }

    @Test
    public void testPutAll() {
        Row row = Row.of("a", 1);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("b", 2);
        extra.put("c", 3);
        row.putAllRows(extra);
        assertEquals(3, row.size());
    }

    // ==================== 列操作 ====================

    @Test
    public void testKeep() {
        Row row = Row.of("a", 1, "b", 2, "c", 3);
        row.keep("a", "c");
        assertEquals(2, row.size());
        assertTrue(row.has("a"));
        assertFalse(row.has("b"));
        assertTrue(row.has("c"));
    }

    @Test
    public void testRename() {
        Row row = Row.of("old_name", "Alice");
        row.rename("old_name", "new_name");
        assertFalse(row.has("old_name"));
        assertEquals("Alice", row.getString("new_name"));
    }

    // ==================== 转换 ====================

    @Test
    public void testToMap() {
        Row row = Row.of("id", 1, "name", "test");
        Map<String, Object> map = row.toMap();
        assertSame(row.toMap(), map);  // 同一引用
        assertEquals(1, map.get("id"));
    }

    @Test
    public void testToMapCopy() {
        Row row = Row.of("id", 1);
        Map<String, Object> copy = row.toMapCopy();
        copy.put("id", 999);
        assertEquals(1, row.getInt("id"));  // 原 Row 不受影响
    }

    @Test
    public void testColumnNames() {
        Row row = Row.of("a", 1, "b", 2, "c", 3);
        assertEquals(3, row.columnNames().size());
        assertTrue(row.columnNames().contains("a"));
        assertTrue(row.columnNames().contains("b"));
        assertTrue(row.columnNames().contains("c"));
    }

    // ==================== 静态工厂 ====================

    @Test
    public void testMerge() {
        Row left = Row.of("id", 1, "name", "Alice");
        Row right = Row.of("id", 99, "age", 30);
        Row merged = Row.merge(left, right);
        assertEquals(99, merged.getInt("id"));   // right 覆盖
        assertEquals("Alice", merged.getString("name"));  // left 保留
        assertEquals(30, merged.getInt("age"));  // right 新增
    }

    @Test
    public void testEmpty() {
        Row row = Row.empty();
        assertTrue(row.isEmpty());
    }

    // ==================== Map 接口兼容 ====================

    @Test
    public void testMapInterfaceCompatibility() {
        Row row = Row.of("id", 1, "name", "test");
        // 作为 Map 使用
        Map<String, Object> map = row;
        map.put("extra", "val");
        assertEquals("val", row.getString("extra"));
        assertEquals(3, row.size());
        assertTrue(row.containsKey("id"));
        assertTrue(row.containsValue("test"));
        row.remove("extra");
        assertFalse(row.containsKey("extra"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Row a = Row.of("id", 1, "name", "test");
        Row b = Row.of("id", 1, "name", "test");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString() {
        Row row = Row.of("id", 1);
        String s = row.toString();
        assertTrue(s.contains("id=1"));
    }

    // ==================== 辅助 ====================

    private static Row empty() { return new Row(); }
}
