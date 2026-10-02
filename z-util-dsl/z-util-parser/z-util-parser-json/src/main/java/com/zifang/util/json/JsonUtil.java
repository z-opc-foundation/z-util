package com.zifang.util.json;

import com.zifang.util.json.annotation.JsonDeserialize;
import com.zifang.util.json.annotation.JsonSerialize;
import com.zifang.util.json.define.TypeReference;
import com.zifang.util.json.exception.JsonTypeException;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import com.zifang.util.json.serializer.SerializerRegistry;
import com.zifang.util.json.serializer.ValueDeserializer;
import com.zifang.util.json.serializer.ValueSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON 工具类，提供 JSON 序列化与反序列化、JSONPath 查询等功能。
 * <p>
 * 支持注解驱动的序列化控制：
 * <ul>
 *   <li>{@code @JsonSerialize(using=KeepLongSerializer.class)} — Long 序列化为字符串，避免 JS 精度丢失</li>
 *   <li>{@code @JsonSerialize(format="yyyy-MM-dd")} — Date 字段格式化</li>
 *   <li>{@code @JsonSerialize(ignore=true)} — 序列化时忽略字段</li>
 *   <li>{@code @JsonSerialize(name="alias")} — 序列化时使用别名</li>
 * </ul>
 *
 * @author zifang
 */
public class JsonUtil {

    private static final JSONParser PARSER = new JSONParser();
    private static final SerializerRegistry REGISTRY = SerializerRegistry.getInstance();

    // ==================== Field Metadata Cache ====================

    /**
     * POJO 序列化时的字段元数据（注解信息、序列化器实例）。
     */
    private static final Map<Class<?>, List<FieldMeta>> serializeMetaCache = new ConcurrentHashMap<>();

    /**
     * POJO 反序列化时的字段元数据（注解信息、反序列化器实例）。
     */
    private static final Map<Class<?>, List<FieldMeta>> deserializeMetaCache = new ConcurrentHashMap<>();

    /**
     * 序列化时检测循环引用的 visited 集合（FEATURE008 P1 修复 2026-06-25）。
     * <p>用 {@link IdentityHashMap} 而非普通 Set，避免 POJO 自身 {@code equals/hashCode} 干扰；
     * 用 {@link ThreadLocal} 隔离并发调用，并在 {@link #toJson} 顶层清空。</p>
     * <p>命中循环引用时该字段序列化为 {@code "null"}，避免 {@link StackOverflowError}。
     * 该行为与 FastJSON 默认行为一致，比 Jackson 的"直接抛异常"更友好（不破坏业务流）。</p>
     */
    private static final ThreadLocal<Set<Object>> SERIALIZE_VISITED =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /**
     * 全局 Long 转字符串序列化开关，仅对未显式指定序列化器的字段生效。
     * <p>用 {@link ThreadLocal} 隔离并发调用，{@link #toJsonWithLongAsString} 用完即复位。</p>
     */
    private static final ThreadLocal<Boolean> LONG_AS_STRING_MODE =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static class FieldMeta {
        final Field field;
        final String jsonName;           // 序列化后的 JSON 属性名
        final boolean ignore;
        final ValueSerializer serializer;
        final ValueDeserializer deserializer;
        final String dateFormat;         // 注解中的 format 值

        FieldMeta(Field field,
                  String jsonName,
                  boolean ignore,
                  ValueSerializer serializer,
                  ValueDeserializer deserializer,
                  String dateFormat) {
            this.field = field;
            this.jsonName = jsonName;
            this.ignore = ignore;
            this.serializer = serializer;
            this.deserializer = deserializer;
            this.dateFormat = dateFormat;
        }
    }

    // ==================== 解析入口 ====================

    public static JsonObject parseObject(String json) {
        if (json == null || json.trim().isEmpty()) return new JsonObject();
        Object parsed = PARSER.fromJSON(json.trim());
        if (parsed instanceof JsonObject) return (JsonObject) parsed;
        throw new JsonTypeException("JSON is not an object: " + json);
    }

    public static JsonArray parseArray(String json) {
        if (json == null || json.trim().isEmpty()) return new JsonArray();
        Object parsed = PARSER.fromJSON(json.trim());
        if (parsed instanceof JsonArray) return (JsonArray) parsed;
        throw new JsonTypeException("JSON is not an array: " + json);
    }

    /**
     * 将JSON对象字符串安全解析为Map。
     * <p>
     * json为null、空白、解析失败、非对象结构或字面量null时返回空Map，不抛出异常，
     * 适用于扩展字段、配置串等来源不可信的场景。
     *
     * @param json JSON字符串
     * @return 键值映射；解析失败时返回空Map
     */
    public static Map<String, Object> parseToMap(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        try {
            JsonObject jsonObject = parseObject(json);
            for (Map.Entry<String, Object> entry : jsonObject.getAllKeyValue()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            // 解析失败保持空Map
        }
        return result;
    }

    // ==================== 序列化 ====================

    public static <T> String toJson(T t) {
        if (t == null) return "null";
        // 仅在顶层调用时清空 visited；嵌套调用要保留祖先栈以正确检测循环引用。
        // (FEATURE008 P1 修复 2026-06-25 v2：原版每次都 clear() 会让 solvePojo 内部 toJson(value) 把栈清空，
        //  失去循环引用检测能力，StackOverflow 仍然会发生)
        Set<Object> visited = SERIALIZE_VISITED.get();
        boolean isTopCall = visited.isEmpty();
        if (isTopCall) {
            visited.clear();  // 防御性清空：上层调用者可能没在 finally 里清
        }
        try {
            if (t instanceof JsonObject || t instanceof JsonArray) return t.toString();
            if (t instanceof String) return "\"" + escapeString((String) t) + "\"";
            if (t instanceof Number || t instanceof Boolean) {
                if (t instanceof Long && LONG_AS_STRING_MODE.get()) {
                    return "\"" + t + "\"";
                }
                return String.valueOf(t);
            }
            if (t instanceof Date) return String.valueOf(((Date) t).getTime());
            if (t instanceof Collection) return solveList((Collection<?>) t);
            if (t instanceof Map) return solveMap((Map<?, ?>) t);
            if (t.getClass().isArray()) return solveArray(t);
            return solvePojo(t);
        } finally {
            if (isTopCall) {
                visited.clear();  // 顶层结束清理，避免 ThreadLocal 内存泄漏 / 下次调用误判
            }
        }
    }

    public static <T> String toJsonPretty(T t) {
        return prettyPrint(toJson(t), 0);
    }

    /**
     * 全局 Long 转字符串序列化：对象中所有 Long（含嵌套集合/Map/POJO字段）序列化为字符串。
     * <p>
     * 避免 JavaScript 处理超过 2^53-1 的整数时丢失精度。
     * 与字段级注解 {@code @JsonSerialize(using=KeepLongSerializer.class)} 的区别：
     * 注解需要逐字段标注，本方法对整个对象的所有 Long 生效；
     * 已显式指定序列化器的字段仍以注解为准。
     *
     * @param value 待序列化对象
     * @return JSON字符串；value为null时返回"null"
     */
    public static String toJsonWithLongAsString(Object value) {
        LONG_AS_STRING_MODE.set(Boolean.TRUE);
        try {
            return toJson(value);
        } finally {
            LONG_AS_STRING_MODE.set(Boolean.FALSE);
        }
    }

    // ==================== 反序列化 ====================

    public static <T> T fromJson(String jsonStr, TypeReference<T> typeRef) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return null;
        Object parsed = PARSER.fromJSON(jsonStr.trim());
        return convertValue(parsed, typeRef.getType());
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return null;
        Object parsed = PARSER.fromJSON(jsonStr.trim());
        return convertValue(parsed, clazz);
    }

    // ==================== 字符串级便捷操作 ====================

    /**
     * 从JSON字符串中获取指定键的字符串值
     * <p>
     * 值为字符串时原样返回；值为数字、布尔、对象或数组时返回其JSON表示。
     *
     * @param json JSON字符串，为null或空白时视为空对象
     * @param key  键名
     * @return 字符串值；键不存在时返回null
     */
    public static String getString(String json, String key) {
        Object value = parseLenient(json).get(key);
        if (value == null) {
            return null;
        }
        return value instanceof String ? (String) value : toJson(value);
    }

    /**
     * 从JSON字符串中获取指定键的String值，键不存在时返回默认值
     *
     * @param json         JSON字符串，为null或空白时视为空对象
     * @param key          键名
     * @param defaultValue 键不存在时的默认返回值
     * @return 字符串值；键不存在时返回defaultValue
     */
    public static String getString(String json, String key, String defaultValue) {
        String value = getString(json, key);
        return value == null ? defaultValue : value;
    }

    /**
     * 从JSON字符串中获取指定键的Integer值
     *
     * @param json JSON字符串，为null或空白时视为空对象
     * @param key  键名
     * @return Integer值；值不是数字或数字字符串时返回null
     */
    public static Integer getInteger(String json, String key) {
        return toIntegerOrNull(parseLenient(json).get(key));
    }

    /**
     * 从JSON字符串中获取指定键的Integer值，键不存在或无法解析时返回默认值
     *
     * @param json         JSON字符串，为null或空白时视为空对象
     * @param key          键名
     * @param defaultValue 键不存在或值无法解析时的默认返回值
     * @return Integer值；解析失败时返回defaultValue
     */
    public static Integer getInteger(String json, String key, Integer defaultValue) {
        Integer value = toIntegerOrNull(parseLenient(json).get(key));
        return value == null ? defaultValue : value;
    }

    /**
     * 从JSON字符串中获取指定键的Long值
     *
     * @param json JSON字符串，为null或空白时视为空对象
     * @param key  键名
     * @return Long值；值不是数字或数字字符串时返回null
     */
    public static Long getLong(String json, String key) {
        return toLongOrNull(parseLenient(json).get(key));
    }

    /**
     * 从JSON字符串中获取指定键的Double值
     *
     * @param json JSON字符串，为null或空白时视为空对象
     * @param key  键名
     * @return Double值；值不是数字或数字字符串时返回null
     */
    public static Double getDouble(String json, String key) {
        return toDoubleOrNull(parseLenient(json).get(key));
    }

    /**
     * 从JSON字符串中获取指定键的Boolean值
     *
     * @param json JSON字符串，为null或空白时视为空对象
     * @param key  键名
     * @return Boolean值；值不是布尔或布尔字符串（true/false，忽略大小写）时返回null
     */
    public static Boolean getBoolean(String json, String key) {
        return toBooleanOrNull(parseLenient(json).get(key));
    }

    /**
     * 从JSON字符串中获取指定键的Boolean值，键不存在或无法解析时返回默认值
     *
     * @param json         JSON字符串，为null或空白时视为空对象
     * @param key          键名
     * @param defaultValue 键不存在或值无法解析时的默认返回值
     * @return Boolean值；解析失败时返回defaultValue
     */
    public static Boolean getBoolean(String json, String key, Boolean defaultValue) {
        Boolean value = toBooleanOrNull(parseLenient(json).get(key));
        return value == null ? defaultValue : value;
    }

    /**
     * 从JSON字符串中获取指定键的嵌套值并转换为指定类型
     *
     * @param json  JSON字符串，为null或空白时视为空对象
     * @param key   键名
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 转换结果；键不存在或无法转换时返回null
     */
    public static <T> T getObject(String json, String key, Class<T> clazz) {
        Object value = parseLenient(json).get(key);
        if (value == null) {
            return null;
        }
        return convertValue(value, clazz);
    }

    /**
     * 从JSON字符串中获取指定键的数组值并逐项转换为指定类型
     *
     * @param json  JSON字符串，为null或空白时视为空对象
     * @param key   键名
     * @param clazz 数组元素目标类型
     * @param <T>   数组元素目标类型
     * @return 转换后的列表；值不是数组时返回null
     */
    public static <T> List<T> getList(String json, String key, Class<T> clazz) {
        Object value = parseLenient(json).get(key);
        if (!(value instanceof JsonArray)) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (Object item : (JsonArray) value) {
            result.add(convertValue(item, clazz));
        }
        return result;
    }

    /**
     * 更新JSON字符串中指定键的值并返回新的JSON字符串
     * <p>
     * 原JSON为null或空白时视为空对象；键不存在时新增，存在时覆盖。
     *
     * @param json  原JSON字符串
     * @param key   键名
     * @param value 新值，支持基本类型、String、Map、Collection、POJO等
     * @return 更新后的紧凑JSON字符串
     */
    public static String updateJson(String json, String key, Object value) {
        JsonObject jsonObject = parseLenient(json);
        jsonObject.put(key, normalizeValue(value));
        return compactJson(jsonObject);
    }

    /**
     * 合并两个JSON字符串
     * <p>
     * 后者的同名键覆盖前者；任一为null或空白时直接返回另一个。
     *
     * @param jsonA 第一个JSON字符串
     * @param jsonB 第二个JSON字符串，其键值覆盖前者
     * @return 合并后的紧凑JSON字符串
     */
    public static String combineJsonString(String jsonA, String jsonB) {
        if (jsonA == null || jsonA.trim().isEmpty()) {
            return jsonB;
        }
        if (jsonB == null || jsonB.trim().isEmpty()) {
            return jsonA;
        }
        JsonObject merged = parseObject(jsonA);
        for (Map.Entry<String, Object> entry : parseObject(jsonB).getAllKeyValue()) {
            merged.put(entry.getKey(), entry.getValue());
        }
        return compactJson(merged);
    }

    /**
     * 移除JSON字符串中指定键后返回新JSON字符串
     *
     * @param json 原JSON字符串，为null或空白时视为空对象
     * @param key  待移除的键
     * @return 移除后的紧凑JSON字符串；key不存在时返回原内容
     */
    public static String removeJson(String json, String key) {
        JsonObject jsonObject = parseLenient(json);
        jsonObject.remove(key);
        return compactJson(jsonObject);
    }

    /**
     * 提取JSON字符串中未被指定类声明的字段，组装为Map
     * <p>
     * 遍历JSON中的键，剔除目标类（含父类）已声明的字段名，剩余键值对入结果。
     * 典型用途：从携带扩展字段的JSON中分离出类外字段。
     *
     * @param json        JSON字符串，为null或空白时返回空Map
     * @param targetClass 目标类，为null时返回空Map
     * @return 未被类声明的字段Map，保持JSON中的键顺序
     */
    public static Map<String, Object> getUndeclaredFields(String json, Class<?> targetClass) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty() || targetClass == null) {
            return result;
        }
        Set<String> declaredFieldNames = new HashSet<>();
        for (Class<?> clazz = targetClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                declaredFieldNames.add(field.getName());
            }
        }
        JsonObject jsonObject;
        try {
            jsonObject = parseObject(json);
        } catch (RuntimeException e) {
            return result;
        }
        for (Map.Entry<String, Object> entry : jsonObject.getAllKeyValue()) {
            if (!declaredFieldNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // ==================== 宽容解析与Map转换 ====================

    /**
     * 解析JSON字符串为JsonObject，解析失败时返回null而不抛出异常
     *
     * @param json JSON字符串，为null或空白时返回空对象
     * @return JsonObject；格式非法时返回null
     */
    public static JsonObject parseObjectQuietly(String json) {
        try {
            return parseObject(json);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 解析JSON字符串为JsonArray，解析失败时返回null而不抛出异常
     *
     * @param json JSON字符串，为null或空白时返回空数组
     * @return JsonArray；格式非法时返回null
     */
    public static JsonArray parseArrayQuietly(String json) {
        try {
            return parseArray(json);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 反序列化，解析或转换失败时返回null而不抛出异常
     *
     * @param jsonStr JSON字符串
     * @param clazz   目标类型
     * @param <T>     目标类型
     * @return 反序列化结果；失败时返回null
     */
    public static <T> T fromJsonQuietly(String jsonStr, Class<T> clazz) {
        try {
            return fromJson(jsonStr, clazz);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 反序列化，解析或转换失败时返回null而不抛出异常
     *
     * @param jsonStr JSON字符串
     * @param typeRef 目标类型引用
     * @param <T>     目标类型
     * @return 反序列化结果；失败时返回null
     */
    public static <T> T fromJsonQuietly(String jsonStr, TypeReference<T> typeRef) {
        try {
            return fromJson(jsonStr, typeRef);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 序列化，失败时返回"null"而不抛出异常
     *
     * @param value 待序列化对象
     * @return JSON字符串；失败时返回"null"
     */
    public static String toJsonQuietly(Object value) {
        try {
            return toJson(value);
        } catch (RuntimeException e) {
            return "null";
        }
    }

    /**
     * 将Map转换为指定类型的对象
     * <p>
     * 先将Map序列化为JSON再反序列化为目标类型，键名与目标类型字段名匹配。
     *
     * @param map  源Map，为null时返回null
     * @param clazz 目标类型
     * @param <T>  目标类型
     * @return 转换结果
     */
    public static <T> T fromMap(Map<?, ?> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return fromJson(toJson(map), clazz);
    }

    /**
     * 宽松解析JSON字符串：null或空白时返回空对象而不是抛出异常
     *
     * @param json JSON字符串
     * @return JsonObject
     */
    private static JsonObject parseLenient(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        return parseObject(json);
    }

    /**
     * 将待写入的值规范化为JSON模型支持的类型
     *
     * @param value 原始值
     * @return 基本类型/String/JsonObject/JsonArray之一
     */
    private static Object normalizeValue(Object value) {
        if (value == null || value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof JsonObject || value instanceof JsonArray) {
            return value;
        }
        if (value instanceof Collection || value.getClass().isArray()) {
            return parseArray(toJson(value));
        }
        return parseObject(toJson(value));
    }

    /**
     * 值转Integer，支持Number与数字字符串
     */
    private static Integer toIntegerOrNull(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                try {
                    return (int) Double.parseDouble(((String) value).trim());
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 值转Long，支持Number与数字字符串
     */
    private static Long toLongOrNull(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong(((String) value).trim());
            } catch (NumberFormatException e) {
                try {
                    return (long) Double.parseDouble(((String) value).trim());
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 值转Double，支持Number与数字字符串
     */
    private static Double toDoubleOrNull(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 值转Boolean，支持Boolean与布尔字符串
     */
    private static Boolean toBooleanOrNull(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            if ("true".equalsIgnoreCase(text)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(text)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /**
     * 将JsonObject序列化为紧凑JSON字符串（无缩进换行）
     *
     * @param jsonObject JsonObject
     * @return 紧凑JSON字符串
     */
    private static String compactJson(JsonObject jsonObject) {
        List<Map.Entry<String, Object>> entries = jsonObject.getAllKeyValue();
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Map.Entry<String, Object> entry = entries.get(i);
            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            sb.append(compactValue(entry.getValue()));
        }
        return sb.append("}").toString();
    }

    /**
     * 将JsonArray序列化为紧凑JSON字符串
     *
     * @param jsonArray JsonArray
     * @return 紧凑JSON字符串
     */
    private static String compactArray(JsonArray jsonArray) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < jsonArray.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(compactValue(jsonArray.get(i)));
        }
        return sb.append("]").toString();
    }

    /**
     * 将任意JSON模型值序列化为紧凑JSON片段
     *
     * @param value JsonObject/JsonArray/基本类型/null
     * @return 紧凑JSON片段
     */
    private static String compactValue(Object value) {
        if (value instanceof JsonObject) {
            return compactJson((JsonObject) value);
        }
        if (value instanceof JsonArray) {
            return compactArray((JsonArray) value);
        }
        return toJson(value);
    }

    // ==================== 序列化内部实现 ====================

    private static String solveList(Collection<?> list) {
        if (list == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        int i = 0, size = list.size();
        for (Object item : list) {
            sb.append(toJson(item));
            if (++i < size) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String solveMap(Map<?, ?> map) {
        if (map == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        int i = 0, size = map.size();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append("\"").append(escapeString(String.valueOf(entry.getKey()))).append("\":");
            sb.append(toJson(entry.getValue()));
            if (++i < size) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String solveArray(Object arr) {
        int len = java.lang.reflect.Array.getLength(arr);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            sb.append(toJson(java.lang.reflect.Array.get(arr, i)));
            if (i < len - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String solvePojo(Object obj) {
        // 循环引用检测：当前 obj 已在祖先栈中，序列化为 null 避免 StackOverflow
        Set<Object> visited = SERIALIZE_VISITED.get();
        if (!visited.add(obj)) {
            return "null";
        }
        try {
        List<FieldMeta> metas = getSerializeMetas(obj.getClass());
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (FieldMeta meta : metas) {
            if (meta.ignore) continue;
            meta.field.setAccessible(true);
            Object value;
            try {
                value = meta.field.get(obj);
            } catch (Exception e) {
                value = null;
            }
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeString(meta.jsonName)).append("\":");
            if (value == null) {
                sb.append("null");
            } else if (meta.serializer != null) {
                sb.append(serializeWithSerializer(value, meta.serializer, meta.dateFormat));
            } else {
                sb.append(toJson(value));
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
        } finally {
            // 退出该 POJO 的递归层级，从 visited 中移除（让兄弟节点/复用节点能正常序列化）
            visited.remove(obj);
        }
    }

    private static String serializeWithSerializer(Object value, ValueSerializer serializer, String format) {
        try {
            return serializer.serialize(value, format);
        } catch (Exception e) {
            throw new RuntimeException("Serializer error: " + serializer.getClass().getSimpleName(), e);
        }
    }

    // ==================== 反序列化内部实现 ====================

    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Object parsed, java.lang.reflect.Type targetType) {
        if (parsed == null) return null;

        if (targetType == JsonObject.class) {
            if (parsed instanceof JsonObject) return (T) parsed;
            throw new JsonTypeException("Cannot convert to JsonObject: " + parsed.getClass());
        }
        if (targetType == JsonArray.class) {
            if (parsed instanceof JsonArray) return (T) parsed;
            throw new JsonTypeException("Cannot convert to JsonArray: " + parsed.getClass());
        }
        if (targetType == String.class) {
            return (T) (parsed instanceof String ? (String) parsed : parsed.toString());
        }
        if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(toInteger(parsed));
        }
        if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(toLong(parsed));
        }
        if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(toDouble(parsed));
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return (T) Boolean.valueOf(toBoolean(parsed));
        }

        // 枚举（兼容"枚举值字符串"格式，直接传 "GET"）：让接口更友好
        if (targetType instanceof Class && ((Class<?>) targetType).isEnum() && parsed instanceof String) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            T enumValue = (T) Enum.valueOf((Class<Enum>) ((Class<?>) targetType).asSubclass(Enum.class), (String) parsed);
            return enumValue;
        }

        // List<T>
        if (targetType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) targetType;
            if (pt.getRawType() == List.class || pt.getRawType() == Collection.class) {
                if (parsed instanceof JsonArray) {
                    JsonArray arr = (JsonArray) parsed;
                    java.lang.reflect.Type elemType = pt.getActualTypeArguments()[0];
                    List<Object> list = new ArrayList<>(arr.size());
                    for (int i = 0; i < arr.size(); i++) {
                        list.add(convertValue(arr.get(i), elemType));
                    }
                    return (T) list;
                }
            }
            if (pt.getRawType() == Map.class) {
                if (parsed instanceof JsonObject) {
                    JsonObject obj = (JsonObject) parsed;
                    java.lang.reflect.Type keyType = pt.getActualTypeArguments()[0];
                    java.lang.reflect.Type valType = pt.getActualTypeArguments()[1];
                    Map<Object, Object> map = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> e : obj.getAllKeyValue()) {
                        map.put(convertValue(e.getKey(), keyType), convertValue(e.getValue(), valType));
                    }
                    return (T) map;
                }
            }
        }

        // 数组
        if (targetType instanceof Class && ((Class<?>) targetType).isArray()) {
            if (parsed instanceof JsonArray) {
                JsonArray arr = (JsonArray) parsed;
                Class<?> compType = ((Class<?>) targetType).getComponentType();
                Object array = java.lang.reflect.Array.newInstance(compType, arr.size());
                for (int i = 0; i < arr.size(); i++) {
                    java.lang.reflect.Array.set(array, i, convertValue(arr.get(i), compType));
                }
                return (T) array;
            }
        }

        // POJO
        if (parsed instanceof JsonObject) {
            return deserializePojo((JsonObject) parsed, (Class<T>) targetType);
        }
        if (parsed instanceof JsonArray && targetType == List.class) {
            JsonArray arr = (JsonArray) parsed;
            List<Object> list = new ArrayList<>(arr.size());
            for (Object item : arr) {
                list.add(item);
            }
            return (T) list;
        }

        return (T) parsed;
    }

    private static <T> T deserializePojo(JsonObject obj, Class<T> clazz) {
        try {
            // 枚举类型特殊处理：用 "name" 字段 + Enum.valueOf（FEATURE008 P1 修复 v4 2026-06-25）
            // 修复原因：枚举没有无参构造器，clazz.getDeclaredConstructor() 会抛 NoSuchMethodException
            // 同时枚举序列化时输出 {"name":"GET","ordinal":0,"hash":0}，反序列化应提取 name
            if (clazz.isEnum()) {
                Object nameVal = obj.get("name");
                if (nameVal == null) {
                    throw new RuntimeException("Enum name not found in JSON: " + obj);
                }
                @SuppressWarnings({"unchecked", "rawtypes"})
                T enumValue = (T) Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), nameVal.toString());
                return enumValue;
            }

            T instance = clazz.getDeclaredConstructor().newInstance();
            List<FieldMeta> metas = getDeserializeMetas(clazz);
            for (FieldMeta meta : metas) {
                // 优先用注解 name 查找字段（支持别名）
                Object rawValue = obj.get(meta.jsonName);
                if (rawValue == null) continue;
                // 关键：用 field.getGenericType() 而非 getType() — 这样 List<InputParam> 能保留泛型
                // (FEATURE008 P1 修复 v5 2026-06-25)
                java.lang.reflect.Type targetType = meta.field.getGenericType();
                Object converted;
                if (meta.deserializer != null) {
                    converted = deserializeWithDeserializer(rawValue, meta.deserializer, meta.dateFormat, meta.field.getType());
                } else {
                    converted = convertValue(rawValue, targetType);
                }
                meta.field.setAccessible(true);
                meta.field.set(instance, converted);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("deserializePojo failed: " + clazz.getName(), e);
        }
    }

    private static Object deserializeWithDeserializer(Object rawValue, ValueDeserializer deserializer,
                                                     String format, Class<?> targetType) {
        try {
            String strVal;
            if (rawValue instanceof String) {
                strVal = (String) rawValue;
            } else {
                strVal = String.valueOf(rawValue);
            }
            return deserializer.deserialize(strVal, targetType, format);
        } catch (Exception e) {
            throw new RuntimeException("Deserializer error: " + deserializer.getClass().getSimpleName(), e);
        }
    }

    // ==================== Field Metadata ====================

    private static List<FieldMeta> getSerializeMetas(Class<?> clazz) {
        List<FieldMeta> cached = serializeMetaCache.get(clazz);
        if (cached != null) return cached;

        List<FieldMeta> metas = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            JsonSerialize ann = field.getAnnotation(JsonSerialize.class);
            String jsonName;
            boolean ignore;
            ValueSerializer serializer = null;
            String dateFormat = "";

            if (ann != null) {
                jsonName = ann.name().isEmpty() ? field.getName() : ann.name();
                ignore = ann.ignore();
                if (!ann.using().equals(JsonSerialize.NONE.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends ValueSerializer> serClass = (Class<? extends ValueSerializer>) ann.using();
                    serializer = REGISTRY.getSerializer(serClass);
                }
                dateFormat = ann.format();
            } else {
                jsonName = field.getName();
                ignore = false;
            }

            metas.add(new FieldMeta(field, jsonName, ignore, serializer, null, dateFormat));
        }

        serializeMetaCache.put(clazz, metas);
        return metas;
    }

    private static List<FieldMeta> getDeserializeMetas(Class<?> clazz) {
        List<FieldMeta> cached = deserializeMetaCache.get(clazz);
        if (cached != null) return cached;

        List<FieldMeta> metas = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            JsonDeserialize ann = field.getAnnotation(JsonDeserialize.class);
            String jsonName;
            ValueDeserializer deserializer = null;
            String dateFormat = "";

            if (ann != null) {
                jsonName = ann.name().isEmpty() ? field.getName() : ann.name();
                if (!ann.using().equals(JsonDeserialize.NONE.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends ValueDeserializer> deserClass = (Class<? extends ValueDeserializer>) ann.using();
                    deserializer = REGISTRY.getDeserializer(deserClass);
                }
                dateFormat = ann.format();
            } else {
                jsonName = field.getName();
            }

            metas.add(new FieldMeta(field, jsonName, false, null, deserializer, dateFormat));
        }

        deserializeMetaCache.put(clazz, metas);
        return metas;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                // 跳过 static 字段（FEATURE008 P1 修复 v3 2026-06-25）：
                //   - 枚举类的 $VALUES / $ENTRIES 静态数组会触发指数级膨胀
                //     （def.toJson() 从 200 字节爆到 197 MB）
                //   - 普通类的 static 字段也不属于"实例状态"，不应序列化
                // 同时跳过 synthetic 字段（compiler 生成的 this$0 / 桥接方法等）
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.isSynthetic()) continue;
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    // ==================== 工具方法 ====================

    static String escapeString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:   sb.append(ch); break;
            }
        }
        return sb.toString();
    }

    private static String prettyPrint(String json, int indent) {
        if (json == null || json.isEmpty()) return json;
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '{' || ch == '[') {
                sb.append(ch).append('\n');
                depth++;
                sb.append(indent(depth));
            } else if (ch == '}' || ch == ']') {
                sb.append('\n');
                depth--;
                sb.append(indent(depth)).append(ch);
            } else if (ch == ',') {
                sb.append(ch).append('\n');
                sb.append(indent(depth));
            } else if (ch == ':') {
                sb.append(ch).append(' ');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        return sb.toString();
    }

    private static int toInteger(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (NumberFormatException e) { return 0L; }
    }

    private static double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static boolean toBoolean(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
