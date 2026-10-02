package com.zifang.util.parser.toml;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * TOML 解析器测试
 */

/**
 * TomlParserTest类。
 */
public class TomlParserTest {

    private TomlParser parser;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        parser = new TomlParser();
    }

    // ==================== 基本类型测试 ====================

    @Test
    /**
     * testBasicString方法。
     */
    public void testBasicString() {
        String toml = "name = \"test\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("test", doc.getTables().get("").get("name"));
    }

    @Test
    /**
     * testBasicInteger方法。
     */
    public void testBasicInteger() {
        String toml = "count = 42\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(42L, doc.getTables().get("").get("count"));
    }

    @Test
    /**
     * testBasicFloat方法。
     */
    public void testBasicFloat() {
        String toml = "price = 3.14\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(3.14, doc.getTables().get("").get("price"));
    }

    @Test
    /**
     * testBasicBoolean方法。
     */
    public void testBasicBoolean() {
        String toml = "enabled = true\n" +
                "disabled = false\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(true, doc.getTables().get("").get("enabled"));
        assertEquals(false, doc.getTables().get("").get("disabled"));
    }

    @Test
    /**
     * testNegativeNumbers方法。
     */
    public void testNegativeNumbers() {
        String toml = "negative = -10\n" +
                "positive = +10\n" +
                "float_negative = -3.14\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(-10L, doc.getTables().get("").get("negative"));
        assertEquals(10L, doc.getTables().get("").get("positive"));
        assertEquals(-3.14, doc.getTables().get("").get("float_negative"));
    }

    @Test
    /**
     * testStringWithSpecialChars方法。
     */
    public void testStringWithSpecialChars() {
        String toml = "text = \"hello\\nworld\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("hello\nworld", doc.getTables().get("").get("text"));
    }

    @Test
    /**
     * testEmptyString方法。
     */
    public void testEmptyString() {
        String toml = "empty = \"\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("", doc.getTables().get("").get("empty"));
    }

    // ==================== Table 测试 ====================

    @Test
    /**
     * testBasicTable方法。
     */
    public void testBasicTable() {
        String toml = "[server]\n" +
                "host = \"localhost\"\n" +
                "port = 8080\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable server = doc.getTable("server");
        assertNotNull(server);
        assertEquals("localhost", server.get("host"));
        assertEquals(8080L, server.get("port"));
    }

    @Test
    /**
     * testNestedTable方法。
     */
    public void testNestedTable() {
        String toml = "[parent.child]\n" +
                "value = 123\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable child = doc.getTable("parent.child");
        assertNotNull(child);
        assertEquals(123L, child.get("value"));
    }

    @Test
    /**
     * testDeepNestedTable方法。
     */
    public void testDeepNestedTable() {
        String toml = "[a.b.c.d]\n" +
                "value = 456\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable deep = doc.getTable("a.b.c.d");
        assertNotNull(deep);
        assertEquals(456L, deep.get("value"));
    }

    @Test
    /**
     * testMultipleTables方法。
     */
    public void testMultipleTables() {
        String toml = "[database]\n" +
                "name = \"mydb\"\n" +
                "\n" +
                "[server]\n" +
                "host = \"localhost\"\n";
        TomlDocument doc = parser.parse(toml);

        assertNotNull(doc.getTable("database"));
        assertNotNull(doc.getTable("server"));
        assertEquals("mydb", doc.getTable("database").get("name"));
        assertEquals("localhost", doc.getTable("server").get("host"));
    }

    @Test
    /**
     * testTableWithMultipleKeys方法。
     */
    public void testTableWithMultipleKeys() {
        String toml = "[config]\n" +
                "key1 = \"value1\"\n" +
                "key2 = \"value2\"\n" +
                "key3 = 123\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable config = doc.getTable("config");
        assertEquals("value1", config.get("key1"));
        assertEquals("value2", config.get("key2"));
        assertEquals(123L, config.get("key3"));
    }

    // ==================== 数组测试 ====================

    @Test
    /**
     * testIntegerArray方法。
     */
    public void testIntegerArray() {
        String toml = "numbers = [1, 2, 3]\n";
        TomlDocument doc = parser.parse(toml);

        List<?> numbers = (List<?>) doc.getTables().get("").get("numbers");
        assertEquals(3, numbers.size());
        assertEquals(1L, numbers.get(0));
        assertEquals(2L, numbers.get(1));
        assertEquals(3L, numbers.get(2));
    }

    @Test
    /**
     * testStringArray方法。
     */
    public void testStringArray() {
        String toml = "names = [\"alice\", \"bob\", \"charlie\"]\n";
        TomlDocument doc = parser.parse(toml);

        List<?> names = (List<?>) doc.getTables().get("").get("names");
        assertEquals(3, names.size());
        assertEquals("alice", names.get(0));
        assertEquals("bob", names.get(1));
        assertEquals("charlie", names.get(2));
    }

    @Test
    /**
     * testMixedArray方法。
     */
    public void testMixedArray() {
        String toml = "mixed = [1, \"two\", 3.0]\n";
        TomlDocument doc = parser.parse(toml);

        List<?> mixed = (List<?>) doc.getTables().get("").get("mixed");
        assertEquals(3, mixed.size());
        assertEquals(1L, mixed.get(0));
        assertEquals("two", mixed.get(1));
        assertEquals(3.0, mixed.get(2));
    }

    @Test
    /**
     * testEmptyArray方法。
     */
    public void testEmptyArray() {
        String toml = "empty = []\n";
        TomlDocument doc = parser.parse(toml);

        List<?> empty = (List<?>) doc.getTables().get("").get("empty");
        assertTrue(empty.isEmpty());
    }

    @Test
    /**
     * testNestedArray方法。
     */
    public void testNestedArray() {
        String toml = "matrix = [[1, 2], [3, 4]]\n";
        TomlDocument doc = parser.parse(toml);

        List<?> matrix = (List<?>) doc.getTables().get("").get("matrix");
        assertEquals(2, matrix.size());
    }

    // ==================== Inline Table 测试 ====================

    @Test
    /**
     * testInlineTable方法。
     */
    public void testInlineTable() {
        String toml = "point = { x = 1, y = 2 }\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable point = (TomlDocument.TomlTable) doc.getTables().get("").get("point");
        assertNotNull(point);
        assertEquals(1L, point.get("x"));
        assertEquals(2L, point.get("y"));
    }

    @Test
    /**
     * testInlineTableWithString方法。
     */
    public void testInlineTableWithString() {
        String toml = "person = { name = \"John\", age = 30 }\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable person = (TomlDocument.TomlTable) doc.getTables().get("").get("person");
        assertNotNull(person);
        assertEquals("John", person.get("name"));
        assertEquals(30L, person.get("age"));
    }

    @Test
    /**
     * testInlineTableNested方法。
     */
    public void testInlineTableNested() {
        String toml = "config = { server = { host = \"localhost\", port = 8080 } }\n";
        TomlDocument doc = parser.parse(toml);

        TomlDocument.TomlTable config = (TomlDocument.TomlTable) doc.getTables().get("").get("config");
        assertNotNull(config);
        TomlDocument.TomlTable server = (TomlDocument.TomlTable) config.get("server");
        assertEquals("localhost", server.get("host"));
        assertEquals(8080L, server.get("port"));
    }

    // ==================== 数组 of Tables 测试 ====================

    @Test
    /**
     * testArrayOfTables方法。
     */
    public void testArrayOfTables() {
        String toml = "[[products]]\n" +
                "name = \"widget\"\n" +
                "\n" +
                "[[products]]\n" +
                "name = \"gadget\"\n";
        TomlDocument doc = parser.parse(toml);

        List<TomlDocument.TomlTable> products = doc.getArrayOfTables();
        assertEquals(2, products.size());
        assertEquals("widget", products.get(0).get("name"));
        assertEquals("gadget", products.get(1).get("name"));
    }

    @Test
    /**
     * testArrayOfTablesWithNestedTable方法。
     */
    public void testArrayOfTablesWithNestedTable() {
        String toml = "[[servers]]\n" +
                "[servers.other]\n" +
                "ip = \"192.168.1.1\"\n" +
                "\n" +
                "[[servers]]\n" +
                "[servers.other]\n" +
                "ip = \"192.168.1.2\"\n";
        TomlDocument doc = parser.parse(toml);

        List<TomlDocument.TomlTable> servers = doc.getArrayOfTables();
        assertEquals(2, servers.size());
    }

    @Test
    /**
     * testNestedArrayOfTables方法。
     */
    public void testNestedArrayOfTables() {
        String toml = "[[fruit]]\n" +
                "name = \"apple\"\n" +
                "\n" +
                "  [[fruit.physical]]\n" +
                "  color = \"red\"\n" +
                "  shape = \"round\"\n";
        TomlDocument doc = parser.parse(toml);

        // fruit is in arrayOfTables since it's defined via [[fruit]]
        assertEquals(1, doc.getArrayOfTables().size());
        TomlDocument.TomlTable fruit = doc.getArrayOfTables().get(0);
        assertNotNull(fruit);
        assertEquals("apple", fruit.get("name"));

        // fruit.physical should be a sub-table-array of fruit
        assertEquals(1, fruit.getSubTableArrays().size());
        TomlDocument.TomlTable physical = fruit.getSubTableArrays().get(0);
        assertEquals("red", physical.get("color"));
        assertEquals("round", physical.get("shape"));
    }

    // ==================== 注释测试 ====================

    @Test
    /**
     * testCommentLine方法。
     */
    public void testCommentLine() {
        String toml = "# 这是一个注释\n" +
                "name = \"test\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("test", doc.getTables().get("").get("name"));
    }

    @Test
    /**
     * testCommentAfterKeyValue方法。
     */
    public void testCommentAfterKeyValue() {
        String toml = "name = \"test\" # 这是注释\n" +
                "value = 123\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("test", doc.getTables().get("").get("name"));
        assertEquals(123L, doc.getTables().get("").get("value"));
    }

    @Test
    /**
     * testMultipleCommentLines方法。
     */
    public void testMultipleCommentLines() {
        String toml = "# 注释1\n" +
                "# 注释2\n" +
                "[server]\n" +
                "# server注释\n" +
                "host = \"localhost\"\n";
        TomlDocument doc = parser.parse(toml);

        assertNotNull(doc.getTable("server"));
        assertEquals("localhost", doc.getTable("server").get("host"));
    }

    @Test
    /**
     * testEmptyLinesIgnored方法。
     */
    public void testEmptyLinesIgnored() {
        String toml = "name = \"test\"\n\n\n\nvalue = 123\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("test", doc.getTables().get("").get("name"));
        assertEquals(123L, doc.getTables().get("").get("value"));
    }

    // ==================== Round-trip 测试 ====================

    @Test
    /**
     * testRoundTripBasic方法。
     */
    public void testRoundTripBasic() {
        String toml = "name = \"test\"\n" +
                "value = 42\n";
        TomlDocument doc = parser.parse(toml);
        String stored = parser.store(doc);
        TomlDocument parsedAgain = parser.parse(stored);

        assertEquals("test", parsedAgain.getTables().get("").get("name"));
        assertEquals(42L, parsedAgain.getTables().get("").get("value"));
    }

    @Test
    /**
     * testRoundTripWithTable方法。
     */
    public void testRoundTripWithTable() {
        String toml = "[server]\n" +
                "host = \"localhost\"\n" +
                "port = 8080\n";
        TomlDocument doc = parser.parse(toml);
        String stored = parser.store(doc);
        TomlDocument parsedAgain = parser.parse(stored);

        assertEquals("localhost", parsedAgain.getTable("server").get("host"));
        assertEquals(8080L, parsedAgain.getTable("server").get("port"));
    }

    @Test
    /**
     * testRoundTripWithArray方法。
     */
    public void testRoundTripWithArray() {
        String toml = "numbers = [1, 2, 3]\n";
        TomlDocument doc = parser.parse(toml);
        String stored = parser.store(doc);
        TomlDocument parsedAgain = parser.parse(stored);

        List<?> numbers = (List<?>) parsedAgain.getTables().get("").get("numbers");
        assertEquals(3, numbers.size());
        assertEquals(1L, numbers.get(0));
    }

    @Test
    /**
     * testRoundTripWithInlineTable方法。
     */
    public void testRoundTripWithInlineTable() {
        String toml = "point = { x = 1, y = 2 }\n";
        TomlDocument doc = parser.parse(toml);
        String stored = parser.store(doc);
        TomlDocument parsedAgain = parser.parse(stored);

        TomlDocument.TomlTable point = (TomlDocument.TomlTable) parsedAgain.getTables().get("").get("point");
        assertNotNull(point);
        assertEquals(1L, point.get("x"));
        assertEquals(2L, point.get("y"));
    }

    @Test
    /**
     * testRoundTripComplex方法。
     */
    public void testRoundTripComplex() {
        String toml = "# 配置文件示例\n" +
                "title = \"TOML Example\"\n" +
                "\n" +
                "[owner]\n" +
                "name = \"John Doe\"\n" +
                "enabled = true\n" +
                "\n" +
                "[database]\n" +
                "server = \"192.168.1.1\"\n" +
                "ports = [8001, 8002, 8003]\n" +
                "connection_max = 5000\n" +
                "enabled = true\n" +
                "\n" +
                "[servers]\n" +
                "\n" +
                "  [servers.alpha]\n" +
                "  ip = \"10.0.0.1\"\n" +
                "  dc = \"eqdc10\"\n" +
                "\n" +
                "  [servers.beta]\n" +
                "  ip = \"10.0.0.2\"\n" +
                "  dc = \"eqdc10\"\n";
        TomlDocument doc = parser.parse(toml);
        String stored = parser.store(doc);
        TomlDocument parsedAgain = parser.parse(stored);

        assertEquals("TOML Example", parsedAgain.getTables().get("").get("title"));
        assertEquals("John Doe", parsedAgain.getTable("owner").get("name"));
        assertEquals(true, parsedAgain.getTable("owner").get("enabled"));
        assertEquals("192.168.1.1", parsedAgain.getTable("database").get("server"));
        assertEquals("10.0.0.1", parsedAgain.getTable("servers.alpha").get("ip"));
    }

    // ==================== 边界测试 ====================

    @Test
    /**
     * testUnderscoreInNumbers方法。
     */
    public void testUnderscoreInNumbers() {
        String toml = "big = 1_000_000\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(1000000L, doc.getTables().get("").get("big"));
    }

    @Test
    /**
     * testWhitespaceAroundKey方法。
     */
    public void testWhitespaceAroundKey() {
        String toml = "key   = \"value\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("value", doc.getTables().get("").get("key"));
    }

    @Test
    /**
     * testLiteralString方法。
     */
    public void testLiteralString() {
        String toml = "path = 'C:\\Users\\test'\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals("C:\\Users\\test", doc.getTables().get("").get("path"));
    }

    @Test
    /**
     * testMultiLineString方法。
     */
    public void testMultiLineString() {
        String toml = "text = \"\"\"\n" +
                "Line 1\n" +
                "Line 2\n" +
                "\"\"\"\n";
        TomlDocument doc = parser.parse(toml);

        assertTrue(doc.getTables().get("").get("text").toString().contains("Line 1"));
    }

    @Test(expected = TomlException.class)
    /**
     * testInvalidTableSyntax方法。
     */
    public void testInvalidTableSyntax() {
        String toml = "[invalid\n";
        parser.parse(toml);
    }

    @Test(expected = TomlException.class)
    /**
     * testUnclosedString方法。
     */
    public void testUnclosedString() {
        String toml = "str = \"unclosed\n";
        parser.parse(toml);
    }

    @Test
    /**
     * testZeroValues方法。
     */
    public void testZeroValues() {
        String toml = "zero_int = 0\n" +
                "zero_float = 0.0\n" +
                "empty_string = \"\"\n";
        TomlDocument doc = parser.parse(toml);

        assertEquals(0L, doc.getTables().get("").get("zero_int"));
        assertEquals(0.0, doc.getTables().get("").get("zero_float"));
        assertEquals("", doc.getTables().get("").get("empty_string"));
    }
}
