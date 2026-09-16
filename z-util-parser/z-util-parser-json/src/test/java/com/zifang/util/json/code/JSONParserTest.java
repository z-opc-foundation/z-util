package com.zifang.util.json.code;

import com.zifang.util.json.JSONParser;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * JSONParser (tokenizer + recursive descent parser) 核心功能测试
 */

/**
 * JSONParserTest类。
 */
public class JSONParserTest {

    private JSONParser jsonParser = new JSONParser();

    // ===== 基础类型 =====

    @Test
    /**
     * testParseNull方法。
     */
    public void testParseNull() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": null}");
        assertNull(obj.get("k"));
    }

    @Test
    /**
     * testParseTrue方法。
     */
    public void testParseTrue() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": true}");
        assertEquals(Boolean.TRUE, obj.get("k"));
    }

    @Test
    /**
     * testParseFalse方法。
     */
    public void testParseFalse() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": false}");
        assertEquals(Boolean.FALSE, obj.get("k"));
    }

    @Test
    /**
     * testParsePositiveInt方法。
     */
    public void testParsePositiveInt() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": 123}");
        assertEquals(123, obj.get("k"));
    }

    @Test
    /**
     * testParseNegativeInt方法。
     */
    public void testParseNegativeInt() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": -456}");
        assertEquals(-456, obj.get("k"));
    }

    @Test
    /**
     * testParsePositiveDouble方法。
     */
    public void testParsePositiveDouble() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": 3.14}");
        assertEquals(3.14, obj.get("k"));
    }

    @Test
    /**
     * testParseNegativeDouble方法。
     */
    public void testParseNegativeDouble() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": -2.718}");
        assertEquals(-2.718, obj.get("k"));
    }

    @Test
    /**
     * testParseScientificNotation方法。
     */
    public void testParseScientificNotation() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": 1.23e10}");
        assertEquals(1.23e10, obj.get("k"));

        obj = (JsonObject) jsonParser.fromJSON("{\"k\": 1e10}");
        assertEquals(1.0e10, obj.get("k"));

        obj = (JsonObject) jsonParser.fromJSON("{\"k\": 1.23E-5}");
        assertEquals(1.23e-5, obj.get("k"));

        obj = (JsonObject) jsonParser.fromJSON("{\"k\": -1.5e3}");
        assertEquals(-1.5e3, obj.get("k"));
    }

    @Test
    /**
     * testParseString方法。
     */
    public void testParseString() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"k\": \"hello\"}");
        assertEquals("hello", obj.get("k"));
    }

    // ===== 对象 =====

    @Test
    /**
     * testParseEmptyObject方法。
     */
    public void testParseEmptyObject() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{}");
        assertEquals(0, obj.getAllKeyValue().size());
    }

    @Test
    /**
     * testParseSimpleObject方法。
     */
    public void testParseSimpleObject() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"name\": \"zifang\"}");
        assertEquals("zifang", obj.get("name"));
    }

    @Test
    /**
     * testParseNestedObject方法。
     */
    public void testParseNestedObject() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"outer\": {\"inner\": \"value\"}}");
        JsonObject inner = obj.getJsonObject("outer");
        assertEquals("value", inner.get("inner"));
    }

    // ===== 数组 =====

    @Test
    /**
     * testParseEmptyArray方法。
     */
    public void testParseEmptyArray() throws Exception {
        JsonArray arr = (JsonArray) jsonParser.fromJSON("[]");
        assertEquals(0, arr.size());
    }

    @Test
    /**
     * testParseSimpleArray方法。
     */
    public void testParseSimpleArray() throws Exception {
        JsonArray arr = (JsonArray) jsonParser.fromJSON("[1, 2, 3]");
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0));
        assertEquals(2, arr.get(1));
        assertEquals(3, arr.get(2));
    }

    @Test
    /**
     * testParseArrayMixedTypes方法。
     */
    public void testParseArrayMixedTypes() throws Exception {
        JsonArray arr = (JsonArray) jsonParser.fromJSON("[0.1, \"a\", 123, 999, true, false, null]");
        assertEquals(0.1, arr.get(0));
        assertEquals("a", arr.get(1));
        assertEquals(123, arr.get(2));
        assertEquals(999, arr.get(3));
        assertEquals(Boolean.TRUE, arr.get(4));
        assertEquals(Boolean.FALSE, arr.get(5));
        assertNull(arr.get(6));
    }

    // ===== 复杂结构 =====

    @Test
    /**
     * testParseComplexObject方法。
     */
    public void testParseComplexObject() throws Exception {
        String json = "{\"a\": 1, \"b\": \"b\", \"c\": {\"a\": 1, \"b\": null, \"d\": [0.1, \"a\", 1, 2, 123, 999.0, true, false, null]}}";
        JsonObject obj = (JsonObject) jsonParser.fromJSON(json);

        assertEquals(1, obj.get("a"));
        assertEquals("b", obj.get("b"));

        JsonObject c = obj.getJsonObject("c");
        assertEquals(1, c.get("a"));
        assertNull(c.get("b"));

        JsonArray d = c.getJsonArray("d");
        assertEquals(0.1, d.get(0));
        assertEquals("a", d.get(1));
        assertEquals(123, d.get(4));
        assertEquals(999.0, d.get(5));
        assertEquals(Boolean.TRUE, d.get(6));
        assertEquals(Boolean.FALSE, d.get(7));
        assertNull(d.get(8));
    }

    @Test
    /**
     * testParseNestedArray方法。
     */
    public void testParseNestedArray() throws Exception {
        JsonArray arr = (JsonArray) jsonParser.fromJSON("[[1, 2, 3, \"中\"]]");
        JsonArray inner = arr.getJsonArray(0);
        assertEquals(1, inner.get(0));
        assertEquals(2, inner.get(1));
        assertEquals(3, inner.get(2));
        assertEquals("中", inner.get(3));
    }

    // ===== Unicode =====

    @Test
    /**
     * testParseChineseString方法。
     */
    public void testParseChineseString() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"name\": \"狄仁杰\", \"type\": \"射手\"}");
        assertEquals("狄仁杰", obj.get("name"));
        assertEquals("射手", obj.get("type"));
    }

    // ===== 字符串中的转义序列 (regression for g4 [.] vs . bug) =====

    @Test
    public void testParseEscapedNewline() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": \"line1\\nline2\"}");
        assertEquals("line1\nline2", obj.get("a"));
    }

    @Test
    public void testParseEscapedTab() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": \"col1\\tcol2\"}");
        assertEquals("col1\tcol2", obj.get("a"));
    }

    @Test
    public void testParseEscapedQuote() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": \"say \\\"hi\\\"\"}");
        assertEquals("say \"hi\"", obj.get("a"));
    }

    @Test
    public void testParseEscapedBackslash() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": \"C:\\\\Users\\\\test\"}");
        assertEquals("C:\\Users\\test", obj.get("a"));
    }

    @Test
    public void testParseEscapedUnicode() throws Exception {
        // 构造 JSON 字面量：用 char 拼装避免 Java 编译器在源码层把 unicode 转义吃掉
        // 目标 JSON: {"a": "\u4e2d\u6587 text"}
        StringBuilder sb = new StringBuilder("{\"a\": \"");
        sb.append("\\u4e2d\\u6587 text");
        sb.append("\"}");
        JsonObject obj = (JsonObject) jsonParser.fromJSON(sb.toString());
        assertEquals("中文 text", obj.get("a"));
    }

    @Test
    public void testParseMixedEscapesInNestedStructure() throws Exception {
        // 模拟 z-team chat-test 场景的 configJson 形态
        String json = "{\"agents\": [{\"role\": \"developer\", \"refId\": \"agent_assistant\","
                + "\"default\": true, \"position\": \"A\","
                + "\"description\": \"line1\\nline2\\twith tab\"}],"
                + "\"welcomeMessage\": \"hello {{name}}\\n\"}";
        JsonObject obj = (JsonObject) jsonParser.fromJSON(json);
        JsonArray agents = obj.getJsonArray("agents");
        assertEquals(1, agents.size());
        JsonObject agent = agents.getJsonObject(0);
        assertEquals("developer", agent.get("role"));
        assertEquals("line1\nline2\twith tab", agent.get("description"));
        assertEquals("hello {{name}}\n", obj.get("welcomeMessage"));
    }

    // ===== getJsonObject / getJsonArray 异常 =====

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetJsonObjectMissingKey方法。
     */
    public void testGetJsonObjectMissingKey() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": 1}");
        obj.getJsonObject("notexist");
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetJsonArrayMissingKey方法。
     */
    public void testGetJsonArrayMissingKey() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": 1}");
        obj.getJsonArray("notexist");
    }

    @Test(expected = com.zifang.util.json.exception.JsonTypeException.class)
    /**
     * testGetJsonObjectWrongType方法。
     */
    public void testGetJsonObjectWrongType() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": 1}");
        obj.getJsonObject("a");
    }

    @Test(expected = com.zifang.util.json.exception.JsonTypeException.class)
    /**
     * testGetJsonArrayWrongType方法。
     */
    public void testGetJsonArrayWrongType() throws Exception {
        JsonObject obj = (JsonObject) jsonParser.fromJSON("{\"a\": \"str\"}");
        obj.getJsonArray("a");
    }
}
