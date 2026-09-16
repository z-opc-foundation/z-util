package com.zifang.util.core.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * GsonUtil 兼容层测试。
 * <p>
 * 验证 GsonUtil 委托给 z-util-parser-json 后行为保持一致。
 */
public class GsonUtilTest {

    @Test
    public void testObjectToJsonStr_simple() {
        String json = GsonUtil.objectToJsonStr("hello");
        assertEquals("\"hello\"", json);
    }

    @Test
    public void testObjectToJsonStr_map() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "alice");
        m.put("age", 30);
        String json = GsonUtil.objectToJsonStr(m);
        assertTrue("should contain name: " + json, json.contains("\"name\":\"alice\""));
        assertTrue("should contain age: " + json, json.contains("\"age\":30"));
    }

    @Test
    public void testJsonStrToObject_class() {
        String json = "{\"a\":1,\"b\":2}";
        Map result = GsonUtil.jsonStrToObject(json, Map.class);
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
    }

    @Test
    public void testToMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("k", "v");
        Map<String, Object> result = GsonUtil.toMap(m);
        assertEquals("v", result.get("k"));
    }

    @Test
    public void testChangeToSubClass() {
        Map<String, Object> m = new HashMap<>();
        m.put("a", 1);
        Integer i = GsonUtil.changeToSubClass(m, Integer.class);
        // 转换回 Integer 不一定有语义，但应能正常返回
        assertNotNull(i);
    }

    @Test
    public void testListToJson() {
        List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        String json = GsonUtil.objectToJsonStr(list);
        assertEquals("[\"a\",\"b\"]", json);
    }
}
