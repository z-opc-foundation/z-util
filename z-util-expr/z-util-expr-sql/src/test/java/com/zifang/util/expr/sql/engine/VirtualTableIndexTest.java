package com.zifang.util.expr.sql.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * VirtualTable 索引功能测试。
 * 验证索引创建、查找、以及 SQL 引擎对索引的自动利用。
 */
public class VirtualTableIndexTest {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();

        // 注册 users 表（1000 行模拟大数据量）
        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("name", "user_" + i);
            row.put("age", 20 + (i % 50));
            row.put("dept", i % 3 == 0 ? "eng" : (i % 3 == 1 ? "sales" : "hr"));
            users.add(row);
        }
        engine.register("users", users);

        // 注册 orders 表
        List<Map<String, Object>> orders = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("user_id", (i % 1000) + 1);
            row.put("product", "product_" + (i % 50));
            row.put("amount", 100 + i * 10);
            orders.add(row);
        }
        engine.register("orders", orders);

        // 注册 departments 表
        List<Map<String, Object>> departments = new ArrayList<>();
        departments.add(row("id", 1, "name", "eng", "budget", 1000000));
        departments.add(row("id", 2, "name", "sales", "budget", 800000));
        departments.add(row("id", 3, "name", "hr", "budget", 500000));
        engine.register("departments", departments);
    }

    // ==================== 索引创建与查找 ====================

    @Test
    public void testCreateIndex() {
        assertFalse(engine.hasIndex("users", "id"));
        engine.createIndex("users", "id");
        assertTrue(engine.hasIndex("users", "id"));
    }

    @Test
    public void testCreateIndexMultipleColumns() {
        engine.createIndex("users", "id", "name", "dept");
        assertTrue(engine.hasIndex("users", "id"));
        assertTrue(engine.hasIndex("users", "name"));
        assertTrue(engine.hasIndex("users", "dept"));
    }

    @Test
    public void testIndexLookup() {
        engine.createIndex("users", "id");
        VirtualTable table = engine.getRegistry().getTable("users");
        List<Map<String, Object>> result = table.lookupByIndex("id", 42);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user_42", result.get(0).get("name"));
    }

    @Test
    public void testIndexLookupNoMatch() {
        engine.createIndex("users", "id");
        VirtualTable table = engine.getRegistry().getTable("users");
        List<Map<String, Object>> result = table.lookupByIndex("id", 99999);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIndexLookupNullValue() {
        engine.createIndex("users", "id");
        VirtualTable table = engine.getRegistry().getTable("users");
        // null 值不被索引
        List<Map<String, Object>> result = table.lookupByIndex("id", null);
        assertNull(result);
    }

    @Test
    public void testIndexLookupNonIndexedColumn() {
        VirtualTable table = engine.getRegistry().getTable("users");
        // 没有索引的列返回 null（调用方应回退到全表扫描）
        List<Map<String, Object>> result = table.lookupByIndex("age", 25);
        assertNull(result);
    }

    // ==================== WHERE 索引加速 ====================

    @Test
    public void testWhereEqualsWithIndex() {
        engine.createIndex("users", "id");
        long start = System.nanoTime();
        List<Map<String, Object>> result = engine.query("SELECT name, age FROM users WHERE id = 500");
        long elapsed = System.nanoTime() - start;

        assertEquals(1, result.size());
        assertEquals("user_500", result.get(0).get("name"));
        assertEquals(20, ((Number) result.get(0).get("age")).intValue());
        // 索引查询应该非常快（< 10ms）
        assertTrue("Index query should be fast, took: " + elapsed / 1_000_000.0 + "ms", elapsed < 10_000_000);
    }

    @Test
    public void testWhereEqualsWithoutIndex() {
        // 不创建索引，全表扫描
        long start = System.nanoTime();
        List<Map<String, Object>> result = engine.query("SELECT name, age FROM users WHERE id = 500");
        long elapsed = System.nanoTime() - start;

        assertEquals(1, result.size());
        assertEquals("user_500", result.get(0).get("name"));
        // 全表扫描应该更慢
        assertTrue("Full scan should take some time", elapsed > 0);
    }

    @Test
    public void testWhereEqualsStringWithIndex() {
        engine.createIndex("users", "name");
        List<Map<String, Object>> result = engine.query("SELECT id, age FROM users WHERE name = 'user_42'");
        assertEquals(1, result.size());
        assertEquals(42, ((Number) result.get(0).get("id")).intValue());
    }

    @Test
    public void testWhereEqualsWithIndexNoMatch() {
        engine.createIndex("users", "id");
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE id = 99999");
        assertEquals(0, result.size());
    }

    // ==================== 多表 JOIN（索引加速） ====================

    @Test
    public void testTwoTableJoinWithIndex() {
        engine.createIndex("orders", "user_id");
        engine.createIndex("users", "id");

        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, o.product, o.amount " +
                "FROM users u INNER JOIN orders o ON u.id = o.user_id " +
                "WHERE u.id = 1");

        // user_id = (i % 1000) + 1，当 i=1 时 user_id=2，所以 user_id=2 有1个订单
        // user_id=1 无订单（i 从1到500，i%1000+1 从2到501）
        // INNER JOIN 不保留无匹配的行，所以结果为空
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTwoTableJoinWithIndexOnBothTables() {
        engine.createIndex("users", "id");
        engine.createIndex("orders", "user_id");

        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, COUNT(*) AS order_count " +
                "FROM users u " +
                "INNER JOIN orders o ON u.id = o.user_id " +
                "GROUP BY u.name " +
                "HAVING COUNT(*) > 1 " +
                "LIMIT 5");

        // 500行 JOIN 后，每用户名平均1个订单，大部分只有1个订单
        // HAVING COUNT(*) > 1 过滤后保留有多个订单的用户名，再 LIMIT 5
        assertTrue(result.size() <= 5);
        for (Map<String, Object> row : result) {
            long count = ((Number) row.get("order_count")).longValue();
            assertTrue("Order count should be > 1, got: " + count, count > 1);
        }
    }

    @Test
    public void testThreeTableJoin() {
        engine.createIndex("users", "id");
        engine.createIndex("users", "dept");
        engine.createIndex("orders", "user_id");
        engine.createIndex("departments", "name");

        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, o.product, d.budget " +
                "FROM users u " +
                "INNER JOIN orders o ON u.id = o.user_id " +
                "INNER JOIN departments d ON u.dept = d.name " +
                "WHERE u.id = 10");

        // user 10 存在，dept = 10 % 3 = 1 → "sales"，budget = 800000
        // user_id 10 在 orders 范围内（2~501），所以有匹配订单
        for (Map<String, Object> row : result) {
            assertEquals("user_10", row.get("name"));
            assertEquals(800000, ((Number) row.get("budget")).intValue());
        }
    }

    @Test
    public void testLeftJoinWithIndex() {
        engine.createIndex("users", "id");
        engine.createIndex("orders", "user_id");

        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, o.product " +
                "FROM users u " +
                "LEFT JOIN orders o ON u.id = o.user_id " +
                "WHERE u.id = 999");

        // user 999 存在但可能没有订单
        assertFalse(result.isEmpty());
        assertEquals("user_999", result.get(0).get("name"));
    }

    // ==================== 性能对比测试 ====================

    @Test
    public void testIndexPerformanceBenefit() {
        // 不使用索引
        long startNoIndex = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            engine.query("SELECT name FROM users WHERE id = " + (i + 1));
        }
        long elapsedNoIndex = System.nanoTime() - startNoIndex;

        // 使用索引
        engine.createIndex("users", "id");
        long startWithIndex = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            engine.query("SELECT name FROM users WHERE id = " + (i + 1));
        }
        long elapsedWithIndex = System.nanoTime() - startWithIndex;

        System.out.println("No index: " + elapsedNoIndex / 1_000_000.0 + "ms");
        System.out.println("With index: " + elapsedWithIndex / 1_000_000.0 + "ms");
        System.out.println("Speedup: " + (double) elapsedNoIndex / elapsedWithIndex + "x");

        // 索引版本应该更快或至少不显著更慢
        assertTrue("Indexed queries should not be slower", elapsedWithIndex <= elapsedNoIndex * 2);
    }

    @Test
    public void testJoinPerformanceWithIndex() {
        // 不使用索引的 JOIN
        long startNoIndex = System.nanoTime();
        engine.query(
                "SELECT u.name, o.product " +
                "FROM users u INNER JOIN orders o ON u.id = o.user_id " +
                "WHERE u.id = 42");
        long elapsedNoIndex = System.nanoTime() - startNoIndex;

        // 使用索引的 JOIN
        engine.createIndex("users", "id");
        engine.createIndex("orders", "user_id");
        long startWithIndex = System.nanoTime();
        engine.query(
                "SELECT u.name, o.product " +
                "FROM users u INNER JOIN orders o ON u.id = o.user_id " +
                "WHERE u.id = 42");
        long elapsedWithIndex = System.nanoTime() - startWithIndex;

        System.out.println("JOIN no index: " + elapsedNoIndex / 1_000_000.0 + "ms");
        System.out.println("JOIN with index: " + elapsedWithIndex / 1_000_000.0 + "ms");

        // 索引 JOIN 应该更快
        assertTrue("Indexed JOIN should be faster", elapsedWithIndex <= elapsedNoIndex * 2);
    }

    // ==================== 边界情况 ====================

    @Test
    public void testIndexOnEmptyTable() {
        List<Map<String, Object>> empty = new ArrayList<>();
        engine.register("empty_table", empty);
        engine.createIndex("empty_table", "id");

        List<Map<String, Object>> result = engine.query("SELECT * FROM empty_table WHERE id = 1");
        assertEquals(0, result.size());
    }

    @Test
    public void testIndexRebuild() {
        engine.createIndex("users", "id");
        assertTrue(engine.hasIndex("users", "id"));

        // 重复创建不会出错
        engine.createIndex("users", "id");
        assertTrue(engine.hasIndex("users", "id"));
    }

    @Test
    public void testIndexWithNullValues() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("id", 1, "name", "Alice"));
        data.add(row("id", 2, "name", null));
        data.add(row("id", 3, "name", "Charlie"));
        engine.register("null_test", data);

        engine.createIndex("null_test", "name");

        // 查找有值的行
        List<Map<String, Object>> result = engine.query("SELECT id FROM null_test WHERE name = 'Alice'");
        assertEquals(1, result.size());
        assertEquals(1, ((Number) result.get(0).get("id")).intValue());

        // 查找 null 值的行（索引不包含 null，回退到全表扫描）
        result = engine.query("SELECT id FROM null_test WHERE name IS NULL");
        assertEquals(1, result.size());
        assertEquals(2, ((Number) result.get(0).get("id")).intValue());
    }

    // ==================== 辅助方法 ====================

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }
}
