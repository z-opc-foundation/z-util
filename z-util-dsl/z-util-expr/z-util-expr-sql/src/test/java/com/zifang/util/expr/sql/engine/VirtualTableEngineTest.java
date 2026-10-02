package com.zifang.util.expr.sql.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * VirtualTableEngine 端到端测试。
 */
public class VirtualTableEngineTest {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();

        // 注册 users 表
        List<Map<String, Object>> users = new ArrayList<>();
        users.add(row("id", 1, "name", "Alice", "age", 30, "dept", "eng"));
        users.add(row("id", 2, "name", "Bob", "age", 25, "dept", "sales"));
        users.add(row("id", 3, "name", "Charlie", "age", 35, "dept", "eng"));
        users.add(row("id", 4, "name", "Diana", "age", 28, "dept", "sales"));
        users.add(row("id", 5, "name", "Eve", "age", 32, "dept", "eng"));
        engine.register("users", users);

        // 注册 orders 表
        List<Map<String, Object>> orders = new ArrayList<>();
        orders.add(row("id", 101, "user_id", 1, "product", "phone", "price", 5999, "qty", 2));
        orders.add(row("id", 102, "user_id", 1, "product", "laptop", "price", 9999, "qty", 1));
        orders.add(row("id", 103, "user_id", 2, "product", "tablet", "price", 3999, "qty", 3));
        orders.add(row("id", 104, "user_id", 3, "product", "phone", "price", 5999, "qty", 1));
        orders.add(row("id", 105, "user_id", null, "product", "watch", "price", 2999, "qty", 1));
        engine.register("orders", orders);
    }

    // ==================== 基础查询 ====================

    @Test
    public void testSelectAll() {
        List<Map<String, Object>> result = engine.query("SELECT * FROM users");
        assertEquals(5, result.size());
    }

    @Test
    public void testSelectColumns() {
        List<Map<String, Object>> result = engine.query("SELECT name, age FROM users");
        assertEquals(5, result.size());
        assertEquals("Alice", result.get(0).get("name"));
        assertEquals(30, ((Number) result.get(0).get("age")).intValue());
    }

    @Test
    public void testSelectWithAlias() {
        List<Map<String, Object>> result = engine.query("SELECT name AS username, age AS user_age FROM users");
        assertEquals(5, result.size());
        assertTrue(result.get(0).containsKey("username"));
        assertTrue(result.get(0).containsKey("user_age"));
        assertEquals("Alice", result.get(0).get("username"));
    }

    // ==================== WHERE 过滤 ====================

    @Test
    public void testWhereEquals() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age = 30");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).get("name"));
    }

    @Test
    public void testWhereGreaterThan() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age > 30");
        assertEquals(2, result.size());
    }

    @Test
    public void testWhereAndOr() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age > 25 AND dept = 'eng'");
        assertEquals(3, result.size()); // Alice(30), Charlie(35), Eve(32)
    }

    @Test
    public void testWhereOr() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE dept = 'eng' OR dept = 'sales'");
        assertEquals(5, result.size());
    }

    @Test
    public void testWhereNotEqual() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE dept <> 'eng'");
        assertEquals(2, result.size());
    }

    @Test
    public void testWhereLessEqual() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age <= 28");
        assertEquals(2, result.size());
    }

    @Test
    public void testWhereBetween() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age BETWEEN 28 AND 32");
        assertEquals(3, result.size()); // Alice(30), Diana(28), Eve(32)
    }

    @Test
    public void testWhereIn() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE dept IN ('eng', 'sales')");
        assertEquals(5, result.size());
    }

    @Test
    public void testWhereLike() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE name LIKE 'A%'");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).get("name"));
    }

    @Test
    public void testWhereIsNull() {
        List<Map<String, Object>> result = engine.query("SELECT product FROM orders WHERE user_id IS NULL");
        assertEquals(1, result.size());
        assertEquals("watch", result.get(0).get("product"));
    }

    @Test
    public void testWhereIsNotNull() {
        List<Map<String, Object>> result = engine.query("SELECT product FROM orders WHERE user_id IS NOT NULL");
        assertEquals(4, result.size());
    }

    @Test
    public void testWhereNot() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE NOT (age > 30)");
        assertEquals(3, result.size());
    }

    @Test
    public void testWhereParentheses() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name FROM users WHERE (age > 25 AND dept = 'eng') OR name = 'Diana'");
        assertEquals(4, result.size()); // Alice, Charlie, Eve, Diana
    }

    // ==================== 表达式列 ====================

    @Test
    public void testSelectExpression() {
        List<Map<String, Object>> result = engine.query("SELECT product, price * qty AS total FROM orders WHERE id = 101");
        assertEquals(1, result.size());
        assertEquals("phone", result.get(0).get("product"));
        assertEquals(11998.0, result.get(0).get("total"));
    }

    @Test
    public void testSelectArithmetic() {
        List<Map<String, Object>> result = engine.query("SELECT price + 100 AS adjusted FROM orders WHERE id = 101");
        assertEquals(1, result.size());
        assertEquals(6099.0, result.get(0).get("adjusted"));
    }

    // ==================== ORDER BY ====================

    @Test
    public void testOrderByAsc() {
        List<Map<String, Object>> result = engine.query("SELECT name, age FROM users ORDER BY age ASC");
        assertEquals("Bob", result.get(0).get("name")); // 25
        assertEquals("Charlie", result.get(result.size() - 1).get("name")); // 35
    }

    @Test
    public void testOrderByDesc() {
        List<Map<String, Object>> result = engine.query("SELECT name, age FROM users ORDER BY age DESC");
        assertEquals("Charlie", result.get(0).get("name")); // 35
    }

    @Test
    public void testOrderByMultiple() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name, dept, age FROM users ORDER BY dept ASC, age DESC");
        // eng: Charlie(35), Eve(32), Alice(30)  sales: Diana(28), Bob(25)
        assertEquals("Charlie", result.get(0).get("name"));
    }

    // ==================== LIMIT / OFFSET ====================

    @Test
    public void testLimit() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users LIMIT 2");
        assertEquals(2, result.size());
    }

    @Test
    public void testLimitOffset() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users ORDER BY id LIMIT 2 OFFSET 1");
        assertEquals(2, result.size());
        assertEquals("Bob", result.get(0).get("name"));
        assertEquals("Charlie", result.get(1).get("name"));
    }

    @Test
    public void testOffsetOnly() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users ORDER BY id OFFSET 3");
        assertEquals(2, result.size());
    }

    // ==================== DISTINCT ====================

    @Test
    public void testDistinct() {
        List<Map<String, Object>> result = engine.query("SELECT DISTINCT dept FROM users");
        assertEquals(2, result.size());
    }

    // ==================== GROUP BY ====================

    @Test
    public void testGroupByCount() {
        List<Map<String, Object>> result = engine.query("SELECT dept, COUNT(*) AS cnt FROM users GROUP BY dept");
        assertEquals(2, result.size());
        // 验证每个部门的人数
        for (Map<String, Object> r : result) {
            if ("eng".equals(r.get("dept"))) {
                assertEquals(3L, r.get("cnt"));
            } else {
                assertEquals(2L, r.get("cnt"));
            }
        }
    }

    @Test
    public void testGroupBySum() {
        List<Map<String, Object>> result = engine.query(
                "SELECT dept, SUM(age) AS total_age FROM users GROUP BY dept");
        assertEquals(2, result.size());
    }

    @Test
    public void testGroupByAvg() {
        List<Map<String, Object>> result = engine.query(
                "SELECT dept, AVG(age) AS avg_age FROM users GROUP BY dept");
        assertEquals(2, result.size());
    }

    @Test
    public void testGroupByMaxMin() {
        List<Map<String, Object>> result = engine.query(
                "SELECT dept, MAX(age) AS max_age, MIN(age) AS min_age FROM users GROUP BY dept");
        assertEquals(2, result.size());
    }

    // ==================== JOIN ====================

    @Test
    public void testInnerJoin() {
        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, o.product FROM users u INNER JOIN orders o ON u.id = o.user_id");
        // user_id=1(2条), user_id=2(1条), user_id=3(1条), user_id=null(1条不匹配)
        assertEquals(4, result.size());
    }

    @Test
    public void testLeftJoin() {
        List<Map<String, Object>> result = engine.query(
                "SELECT u.name, o.product FROM users u LEFT JOIN orders o ON u.id = o.user_id");
        // 所有5个用户都应出现（user1有2个订单，所以总行数=6）
        assertEquals(6, result.size());
    }

    // ==================== 内置函数 ====================

    @Test
    public void testCountFunction() {
        List<Map<String, Object>> result = engine.query("SELECT COUNT(*) AS total FROM users");
        assertEquals(1, result.size());
        assertEquals(5, ((Number) result.get(0).get("total")).intValue());
    }

    @Test
    public void testSumFunction() {
        List<Map<String, Object>> result = engine.query("SELECT SUM(age) AS total_age FROM users");
        assertEquals(1, result.size());
    }

    @Test
    public void testMaxFunction() {
        List<Map<String, Object>> result = engine.query("SELECT MAX(age) AS max_age FROM users");
        assertEquals(1, result.size());
        assertEquals(35, ((Number) result.get(0).get("max_age")).intValue());
    }

    @Test
    public void testMinFunction() {
        List<Map<String, Object>> result = engine.query("SELECT MIN(age) AS min_age FROM users");
        assertEquals(1, result.size());
        assertEquals(25, ((Number) result.get(0).get("min_age")).intValue());
    }

    // ==================== Bean 转换 ====================

    @Test
    public void testQueryAs() {
        List<UserDTO> result = engine.queryAs(
                "SELECT name, age FROM users WHERE age >= 30 ORDER BY age", UserDTO.class);
        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).name);
        assertEquals(30, result.get(0).age);
        assertEquals("Charlie", result.get(2).name);
        assertEquals(35, result.get(2).age);
    }

    // ==================== 边界情况 ====================

    @Test
    public void testEmptyResult() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM users WHERE age > 100");
        assertEquals(0, result.size());
    }

    @Test
    public void testTableCaseInsensitive() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM USERS");
        assertEquals(5, result.size());
    }

    @Test
    public void testRegisterAndQuery() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("key", "hello", "value", 42));
        engine.register("kv", data);
        assertTrue(engine.hasTable("kv"));
        List<Map<String, Object>> result = engine.query("SELECT key, value FROM kv");
        assertEquals(1, result.size());
        assertEquals("hello", result.get(0).get("key"));
        assertEquals(42, ((Number) result.get(0).get("value")).intValue());
    }

    @Test
    public void testUnregister() {
        engine.register("temp", new ArrayList<>());
        assertTrue(engine.hasTable("temp"));
        engine.unregister("temp");
        assertFalse(engine.hasTable("temp"));
    }

    // ==================== 辅助方法 ====================

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }

    /**
     * 用于 Bean 转换测试的 DTO。
     */
    public static class UserDTO {
        public String name;
        public int age;
    }
}
