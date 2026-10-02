package com.zifang.util.expr.sql.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * SqlEngine 执行引擎单元测试。
 * 验证 SQL 执行的正确性。
 */
public class SqlEngineTest {

    private VirtualTableEngine engine;

    @Before
    public void setUp() {
        engine = new VirtualTableEngine();

        List<Map<String, Object>> products = new ArrayList<>();
        products.add(row("id", 1, "name", "Phone", "price", 5999, "category", "electronics"));
        products.add(row("id", 2, "name", "Laptop", "price", 9999, "category", "electronics"));
        products.add(row("id", 3, "name", "Book", "price", 99, "category", "education"));
        products.add(row("id", 4, "name", "Pen", "price", 5, "category", "education"));
        products.add(row("id", 5, "name", "Tablet", "price", 3999, "category", "electronics"));
        engine.register("products", products);

        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(row("id", 1, "name", "electronics", "tax_rate", 0.13));
        categories.add(row("id", 2, "name", "education", "tax_rate", 0.0));
        engine.register("categories", categories);
    }

    // ==================== SELECT 投影 ====================

    @Test
    public void testSelectAll() {
        List<Map<String, Object>> result = engine.query("SELECT * FROM products");
        assertEquals(5, result.size());
    }

    @Test
    public void testSelectColumns() {
        List<Map<String, Object>> result = engine.query("SELECT name, price FROM products");
        assertEquals(5, result.size());
        assertEquals(2, result.get(0).size());
        assertTrue(result.get(0).containsKey("name"));
        assertTrue(result.get(0).containsKey("price"));
    }

    @Test
    public void testSelectWithAlias() {
        List<Map<String, Object>> result = engine.query("SELECT name AS product_name FROM products LIMIT 1");
        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("product_name"));
        assertEquals("Phone", result.get(0).get("product_name"));
    }

    // ==================== WHERE 过滤 ====================

    @Test
    public void testWhereEquals() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE id = 1");
        assertEquals(1, result.size());
        assertEquals("Phone", result.get(0).get("name"));
    }

    @Test
    public void testWhereGreaterThan() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE price > 5000");
        assertEquals(2, result.size()); // Phone(5999), Laptop(9999)
    }

    @Test
    public void testWhereLessThan() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE price < 100");
        assertEquals(2, result.size()); // Book(99), Pen(5)
    }

    @Test
    public void testWhereAnd() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name FROM products WHERE price > 100 AND category = 'electronics'");
        assertEquals(3, result.size()); // Phone(5999), Laptop(9999), Tablet(3999)
    }

    @Test
    public void testWhereOr() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name FROM products WHERE name = 'Phone' OR name = 'Book'");
        assertEquals(2, result.size());
    }

    @Test
    public void testWhereNotEqual() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE category <> 'electronics'");
        assertEquals(2, result.size()); // Book, Pen
    }

    @Test
    public void testWhereBetween() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE price BETWEEN 100 AND 6000");
        assertEquals(2, result.size()); // Phone(5999), Tablet(3999)
    }

    @Test
    public void testWhereIn() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name FROM products WHERE category IN ('electronics', 'education')");
        assertEquals(5, result.size());
    }

    @Test
    public void testWhereLike() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE name LIKE 'P%'");
        assertEquals(2, result.size()); // Phone, Pen
    }

    @Test
    public void testWhereIsNull() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE category IS NULL");
        assertEquals(0, result.size());
    }

    @Test
    public void testWhereIsNotNull() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE category IS NOT NULL");
        assertEquals(5, result.size());
    }

    @Test
    public void testWhereParentheses() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name FROM products WHERE (price > 5000 OR price < 10) AND category = 'electronics'");
        assertEquals(2, result.size()); // Phone(5999), Laptop(9999)
    }

    // ==================== 表达式列 ====================

    @Test
    public void testSelectExpression() {
        List<Map<String, Object>> result = engine.query("SELECT name, price * 2 AS doubled FROM products WHERE id = 1");
        assertEquals(1, result.size());
        assertEquals("Phone", result.get(0).get("name"));
        assertEquals(11998.0, result.get(0).get("doubled"));
    }

    @Test
    public void testSelectArithmetic() {
        List<Map<String, Object>> result = engine.query("SELECT price + 100 AS adjusted FROM products WHERE id = 4");
        assertEquals(1, result.size());
        assertEquals(105.0, result.get(0).get("adjusted"));
    }

    // ==================== ORDER BY ====================

    @Test
    public void testOrderByAsc() {
        List<Map<String, Object>> result = engine.query("SELECT name, price FROM products ORDER BY price ASC");
        assertEquals("Pen", result.get(0).get("name")); // 5
        assertEquals("Laptop", result.get(result.size() - 1).get("name")); // 9999
    }

    @Test
    public void testOrderByDesc() {
        List<Map<String, Object>> result = engine.query("SELECT name, price FROM products ORDER BY price DESC");
        assertEquals("Laptop", result.get(0).get("name")); // 9999
        assertEquals("Pen", result.get(result.size() - 1).get("name")); // 5
    }

    @Test
    public void testOrderByMultiple() {
        List<Map<String, Object>> result = engine.query(
                "SELECT name, category, price FROM products ORDER BY category ASC, price DESC");
        // education: Book(99), Pen(5)  electronics: Laptop(9999), Phone(5999), Tablet(3999)
        assertEquals("Book", result.get(0).get("name"));
        assertEquals("Pen", result.get(1).get("name"));
    }

    // ==================== LIMIT / OFFSET ====================

    @Test
    public void testLimit() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products ORDER BY id LIMIT 2");
        assertEquals(2, result.size());
        assertEquals("Phone", result.get(0).get("name"));
        assertEquals("Laptop", result.get(1).get("name"));
    }

    @Test
    public void testLimitOffset() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products ORDER BY id LIMIT 2 OFFSET 2");
        assertEquals(2, result.size());
        assertEquals("Book", result.get(0).get("name"));
        assertEquals("Pen", result.get(1).get("name"));
    }

    // ==================== DISTINCT ====================

    @Test
    public void testDistinct() {
        List<Map<String, Object>> result = engine.query("SELECT DISTINCT category FROM products");
        assertEquals(2, result.size()); // electronics, education
    }

    // ==================== GROUP BY ====================

    @Test
    public void testGroupByCount() {
        List<Map<String, Object>> result = engine.query(
                "SELECT category, COUNT(*) AS cnt FROM products GROUP BY category");
        assertEquals(2, result.size());
        for (Map<String, Object> r : result) {
            if ("electronics".equals(r.get("category"))) {
                assertEquals(3L, r.get("cnt"));
            } else {
                assertEquals(2L, r.get("cnt"));
            }
        }
    }

    @Test
    public void testGroupBySum() {
        List<Map<String, Object>> result = engine.query(
                "SELECT category, SUM(price) AS total FROM products GROUP BY category");
        assertEquals(2, result.size());
    }

    @Test
    public void testGroupByMaxMin() {
        List<Map<String, Object>> result = engine.query(
                "SELECT category, MAX(price) AS max_p, MIN(price) AS min_p FROM products GROUP BY category");
        assertEquals(2, result.size());
    }

    @Test
    public void testAggregateWithoutGroupBy() {
        List<Map<String, Object>> result = engine.query("SELECT COUNT(*) AS total FROM products");
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).get("total"));
    }

    // ==================== JOIN ====================

    @Test
    public void testInnerJoin() {
        List<Map<String, Object>> result = engine.query(
                "SELECT p.name, c.tax_rate FROM products p INNER JOIN categories c ON p.category = c.name");
        // 所有5个产品都有匹配的category
        assertEquals(5, result.size());
    }

    @Test
    public void testLeftJoin() {
        // 添加一个没有对应category的产品
        List<Map<String, Object>> extra = new ArrayList<>();
        extra.add(row("id", 1, "name", "Phone", "price", 5999, "category", "electronics"));
        extra.add(row("id", 2, "name", "Laptop", "price", 9999, "category", "electronics"));
        extra.add(row("id", 3, "name", "Book", "price", 99, "category", "education"));
        extra.add(row("id", 4, "name", "Pen", "price", 5, "category", "education"));
        extra.add(row("id", 5, "name", "Tablet", "price", 3999, "category", "electronics"));
        engine.register("p2", extra);

        List<Map<String, Object>> cats = new ArrayList<>();
        cats.add(row("id", 1, "name", "electronics"));
        engine.register("c2", cats);

        List<Map<String, Object>> result = engine.query(
                "SELECT p.name, c.name AS cat FROM p2 p LEFT JOIN c2 c ON p.category = c.name");
        // 电子产品匹配，教育类不匹配（LEFT JOIN 保留）
        assertEquals(5, result.size());
    }

    // ==================== 内置函数 ====================

    @Test
    public void testCountFunction() {
        List<Map<String, Object>> result = engine.query("SELECT COUNT(*) AS total FROM products");
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).get("total"));
    }

    @Test
    public void testSumFunction() {
        List<Map<String, Object>> result = engine.query("SELECT SUM(price) AS total FROM products");
        assertEquals(1, result.size());
    }

    @Test
    public void testMaxFunction() {
        List<Map<String, Object>> result = engine.query("SELECT MAX(price) AS max_price FROM products");
        assertEquals(1, result.size());
        assertEquals(9999, ((Number) result.get(0).get("max_price")).intValue());
    }

    @Test
    public void testMinFunction() {
        List<Map<String, Object>> result = engine.query("SELECT MIN(price) AS min_price FROM products");
        assertEquals(1, result.size());
        assertEquals(5, ((Number) result.get(0).get("min_price")).intValue());
    }

    // ==================== Bean 转换 ====================

    @Test
    public void testQueryAs() {
        List<ProductDTO> result = engine.queryAs(
                "SELECT name, price FROM products WHERE price > 1000 ORDER BY price", ProductDTO.class);
        assertEquals(3, result.size());
        assertEquals("Tablet", result.get(0).name);
        assertEquals(3999, result.get(0).price);
    }

    // ==================== 表管理 ====================

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

    @Test
    public void testTableNames() {
        Set<String> names = engine.getTableNames();
        assertTrue(names.contains("products"));
        assertTrue(names.contains("categories"));
    }

    @Test
    public void testClear() {
        engine.clear();
        assertFalse(engine.hasTable("products"));
    }

    // ==================== 边界情况 ====================

    @Test
    public void testEmptyResult() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM products WHERE price > 100000");
        assertEquals(0, result.size());
    }

    @Test
    public void testTableCaseInsensitive() {
        List<Map<String, Object>> result = engine.query("SELECT name FROM PRODUCTS");
        assertEquals(5, result.size());
    }

    // ==================== 辅助方法 ====================

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }

    public static class ProductDTO {
        public String name;
        public int price;
    }
}
