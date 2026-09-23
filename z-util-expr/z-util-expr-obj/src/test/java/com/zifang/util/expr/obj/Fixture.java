package com.zifang.util.expr.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试用的 spec 构造器：spec 的形状与 JSON 完全一致，这里只是省掉手写字符串。
 *
 * @author zifang
 */
final class Fixture {

    private Fixture() {
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }

    static Map<String, Object> row(Object... kv) {
        return map(kv);
    }

    @SafeVarargs
    static List<Map<String, Object>> rows(Map<String, Object>... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    static List<Object> program(Object... steps) {
        return new ArrayList<>(Arrays.asList(steps));
    }

    static Map<String, List<Map<String, Object>>> tables(String name, List<Map<String, Object>> items) {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        map.put(name, items);
        return map;
    }

    static Object run(Object program, Object value) {
        return new ObjEngine().shape(program, value);
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> asRows(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    static long asLong(Object value) {
        return ((Number) value).longValue();
    }

    static double asDouble(Object value) {
        return ((Number) value).doubleValue();
    }
}
