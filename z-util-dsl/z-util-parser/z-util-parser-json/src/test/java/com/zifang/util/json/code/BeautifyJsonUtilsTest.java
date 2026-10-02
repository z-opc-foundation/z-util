package com.zifang.util.json.code;

import com.zifang.util.json.BeautifyJsonUtils;
import com.zifang.util.json.JSONParser;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * BeautifyJsonUtils 美化输出测试
 */

/**
 * BeautifyJsonUtilsTest类。
 */
public class BeautifyJsonUtilsTest {

    private JSONParser jsonParser = new JSONParser();

    private JsonObject parseObject(String json) throws Exception {
        return (JsonObject) jsonParser.fromJSON(json);
    }

    private JsonArray parseArray(String json) throws Exception {
        return (JsonArray) jsonParser.fromJSON(json);
    }

    // ===== 正常美化 =====

    @Test
    /**
     * testBeautifyEmptyObject方法。
     */
    public void testBeautifyEmptyObject() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseObject("{}"));
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
        assertFalse(result.contains(","));
    }

    @Test
    /**
     * testBeautifyEmptyArray方法。
     */
    public void testBeautifyEmptyArray() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseArray("[]"));
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
        assertFalse(result.contains(","));
    }

    @Test
    /**
     * testBeautifySimpleObject方法。
     */
    public void testBeautifySimpleObject() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseObject("{\"name\": \"zifang\", \"age\": 30}"));
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"zifang\""));
        assertTrue(result.contains("\"age\""));
        assertTrue(result.contains("30"));
    }

    @Test
    /**
     * testBeautifyNestedObject方法。
     */
    public void testBeautifyNestedObject() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseObject("{\"person\": {\"name\": \"alice\"}}"));
        assertTrue(result.contains("\"person\""));
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"alice\""));
    }

    @Test
    /**
     * testBeautifyArrayOfPrimitives方法。
     */
    public void testBeautifyArrayOfPrimitives() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseArray("[\"a\", \"b\", \"c\"]"));
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
        assertTrue(result.contains("\"c\""));
    }

    @Test
    /**
     * testBeautifyArrayOfObjects方法。
     */
    public void testBeautifyArrayOfObjects() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseArray("[{\"name\": \"a\"}, {\"name\": \"b\"}]"));
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
    }

    // ===== null 值处理 =====

    @Test
    /**
     * testBeautifyNullValue方法。
     */
    public void testBeautifyNullValue() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseObject("{\"a\": null}"));
        assertTrue(result.contains("null"));
        assertFalse(result.contains("\"null\"")); // null 不带引号
    }

    @Test
    /**
     * testBeautifyNullInArray方法。
     */
    public void testBeautifyNullInArray() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseArray("[null, \"a\"]"));
        assertTrue(result.contains("null"));
    }

    // ===== 多次调用 callDepth 不累积 =====

    @Test
    /**
     * testBeautifyMultipleCallsNoDepthLeak方法。
     */
    public void testBeautifyMultipleCallsNoDepthLeak() throws Exception {
        // 连续调用不应因 static callDepth 累积而缩进错误
        String r1 = BeautifyJsonUtils.beautify(parseObject("{\"a\": 1}"));
        String r2 = BeautifyJsonUtils.beautify(parseObject("{\"b\": 2}"));
        // 两者顶层结构都是 "{\n  ...\n}"
        assertTrue(r1.startsWith("{\n"));
        assertTrue(r1.endsWith("\n}"));
        assertTrue(r2.startsWith("{\n"));
        assertTrue(r2.endsWith("\n}"));
    }

    @Test
    /**
     * testBeautifyThreeNestedObjects方法。
     */
    public void testBeautifyThreeNestedObjects() throws Exception {
        String result = BeautifyJsonUtils.beautify(parseObject("{\"a\": {\"b\": {\"c\": 1}}}"));
        // 缩进深度逐层增加
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
        assertTrue(result.contains("\"c\""));
    }
}
