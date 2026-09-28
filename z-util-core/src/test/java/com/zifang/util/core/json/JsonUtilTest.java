package com.zifang.util.core.json;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JsonUtil工具类的单元测试。
 */
public class JsonUtilTest {

    @Test
    public void testSortKeysIsDeterministic() {
        String left = "{\"b\":1,\"a\":{\"y\":2,\"x\":[3,2,1]}}";
        String right = "{\"a\":{\"x\":[3,2,1],\"y\":2},\"b\":1}";
        assertEquals(JsonUtil.sortKeys(left), JsonUtil.sortKeys(right));
        assertEquals("{\"a\":{\"x\":[3,2,1],\"y\":2},\"b\":1}", JsonUtil.sortKeys(left));
    }

    @Test
    public void testSortKeysRecursesIntoArrays() {
        String json = "[{\"b\":1,\"a\":2},{\"d\":3,\"c\":4}]";
        assertEquals("[{\"a\":2,\"b\":1},{\"c\":4,\"d\":3}]", JsonUtil.sortKeys(json));
    }

    @Test
    public void testCanonicalFromObject() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("z", 1);
        inner.put("a", "2");
        Map<String, Object> outer = new LinkedHashMap<>();
        outer.put("name", "z-util");
        outer.put("detail", inner);
        assertEquals("{\"detail\":{\"a\":\"2\",\"z\":1},\"name\":\"z-util\"}", JsonUtil.canonicalValue(outer));
        assertEquals("null", JsonUtil.canonicalValue(null));
    }

    @Test
    public void testCanonicalValueMatchesSortKeys() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("k2", Arrays.asList(1, 2));
        value.put("k1", "v");
        assertEquals(JsonUtil.sortKeys("{\"k2\":[1,2],\"k1\":\"v\"}"), JsonUtil.canonicalValue(value));
    }

    @Test
    public void testDeepEqualsIgnoresKeyOrder() {
        assertTrue(JsonUtil.deepEquals("{\"b\":1,\"a\":2}", "{\"a\":2,\"b\":1}"));
        assertTrue(JsonUtil.deepEquals("{\"a\":{\"x\":1,\"y\":2}}", "{\"a\":{\"y\":2,\"x\":1}}"));
        assertFalse(JsonUtil.deepEquals("{\"a\":1}", "{\"a\":2}"));
        assertFalse(JsonUtil.deepEquals("{\"a\":1}", "{\"a\":1,\"b\":2}"));
    }

    @Test
    public void testDeepEqualsComparesNumbersByValue() {
        assertTrue(JsonUtil.deepEquals("{\"n\":1}", "{\"n\":1.00}"));
        assertTrue(JsonUtil.deepEquals("[1e2]", "[100]"));
        assertFalse(JsonUtil.deepEquals("{\"n\":\"1\"}", "{\"n\":1}"));
    }

    @Test
    public void testDeepEqualsArrayOrderMatters() {
        assertTrue(JsonUtil.deepEquals("[1,2,3]", "[1,2,3]"));
        assertFalse(JsonUtil.deepEquals("[1,2]", "[2,1]"));
        assertFalse(JsonUtil.deepEquals("[1,2]", "[1,2,3]"));
    }

    @Test
    public void testDeepEqualsNullHandling() {
        assertTrue(JsonUtil.deepEquals(null, null));
        assertFalse(JsonUtil.deepEquals(null, "{}"));
        assertTrue(JsonUtil.deepEquals("null", "null"));
        assertFalse(JsonUtil.deepEquals("null", "\"null\""));
    }

    @Test
    public void testUpperFirstKeys() {
        assertEquals("{\"Ab\":1,\"Bc\":{\"Cd\":2}}", JsonUtil.upperFirstKeys("{\"ab\":1,\"bc\":{\"cd\":2}}"));
    }

    @Test
    public void testInvalidJsonThrows() {
        String[] bad = {"{", "not json", "   ", "{\"a\":}\""};
        for (String value : bad) {
            try {
                JsonUtil.sortKeys(value);
                fail("expected IllegalArgumentException for " + value);
            } catch (IllegalArgumentException ignored) {
                // 预期
            }
        }
    }
}
