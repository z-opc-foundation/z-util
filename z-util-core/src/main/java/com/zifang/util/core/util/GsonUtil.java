package com.zifang.util.core.util;

import com.zifang.util.json.JsonUtil;
import com.zifang.util.json.model.JsonObject;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @deprecated 请直接使用 {@link JsonUtil}（z-util-parser-json 提供）。
 * <p>
 * 本类保留仅为兼容旧代码，内部完全委托给自研的 z-util-parser-json。
 *
 * @author zifang
 */
@Deprecated
public class GsonUtil {

    public static <T> String objectToJsonStr(T object) {
        return JsonUtil.toJson(object);
    }

    public static <T> T jsonStrToObject(String jsonStr, Class<T> classOfT) {
        // 优先处理 Map / List / ArrayList 等 raw 类型
        if (classOfT == java.util.Map.class || classOfT == java.util.HashMap.class) {
            return (T) jsonToMap(JsonUtil.parseObject(jsonStr));
        }
        if (classOfT == java.util.List.class || classOfT == java.util.ArrayList.class) {
            return (T) JsonUtil.parseArray(jsonStr);
        }
        return JsonUtil.fromJson(jsonStr, classOfT);
    }

    /** 把 JsonObject 递归转成 java.util.Map（包含其内部嵌套对象）。 */
    private static Map<String, Object> jsonToMap(JsonObject obj) {
        Map<String, Object> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Object> e : obj.getAllKeyValue()) {
            Object v = e.getValue();
            if (v instanceof JsonObject) v = jsonToMap((JsonObject) v);
            else if (v instanceof com.zifang.util.json.model.JsonArray) v = v.toString();
            result.put(e.getKey(), v);
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T jsonStrToObject(String jsonStr, Type typeReference) {
        // 简化版：仅支持 Class 类型。ParameterizedType 等复杂类型请直接使用 JsonUtil.fromJson(String, TypeReference)。
        if (typeReference instanceof Class) {
            Class cls = (Class) typeReference;
            // Map / List 等 raw 类型走专门的解析路径
            if (cls == java.util.Map.class || cls == java.util.HashMap.class) {
                return (T) JsonUtil.parseObject(jsonStr);
            }
            if (cls == java.util.List.class || cls == java.util.ArrayList.class) {
                return (T) JsonUtil.parseArray(jsonStr);
            }
            return (T) JsonUtil.fromJson(jsonStr, cls);
        }
        // 非 Class 类型，尝试先解析为 Map 后再转换（粗略实现）
        return (T) JsonUtil.parseObject(jsonStr);
    }

    public static <T> T changeToSubClass(Object o, Class<T> t) {
        return jsonStrToObject(objectToJsonStr(o), t);
    }

    public static Map<String, Object> toMap(Object o) {
        // JsonUtil.parseObject 返回 JsonObject（不是 Map），需要手工转换
        JsonObject obj = JsonUtil.parseObject(objectToJsonStr(o));
        Map<String, Object> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Object> e : obj.getAllKeyValue()) {
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }
}
