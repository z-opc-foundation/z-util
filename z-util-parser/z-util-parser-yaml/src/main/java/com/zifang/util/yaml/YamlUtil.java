package com.zifang.util.yaml;

import com.zifang.util.yaml.define.TypeReference;
import com.zifang.util.yaml.dsl.YamlPathParser;
import com.zifang.util.yaml.exception.YamlParseException;

import java.util.List;
import java.util.Map;

/**
 * YAML 工具类，提供 YAML 序列化与反序列化、YAMLPath 查询等功能。
 *
 * @author zifang
 */
/**
 * YamlUtil类。
 */

/**
 * YamlUtil类。
 */
public class YamlUtil {

    private static final YamlParser DEFAULT_PARSER = new YamlParser();

    private YamlUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 将对象序列化为 YAML 字符串。
     *
     * @param t 待序列化的对象
     * @param <T> 对象类型
     * @return YAML 字符串，序列化失败时返回 null
     */
    /**
     * toYaml方法。
     *      * @param t T类型参数
     * @return static <T> String类型返回值
     */
    /**
     * toYaml方法。
     * * @param t T类型参数
     *
     * @return static <T> String类型返回值
     */
    public static <T> String toYaml(T t) {
        if (t == null) {
            return "null\n";
        }
        try {
            return DEFAULT_PARSER.toYaml(t);
        } catch (Exception e) {
            throw new YamlParseException("Failed to serialize object to YAML: " + e.getMessage(), e);
        }
    }

    /**
     * 将对象序列化为格式化的 YAML 字符串（带缩进）。
     *
     * @param t 待序列化的对象
     * @param <T> 对象类型
     * @return 格式化的 YAML 字符串
     */
    /**
     * toPrettyYaml方法。
     *      * @param t T类型参数
     * @return static <T> String类型返回值
     */
    /**
     * toPrettyYaml方法。
     * * @param t T类型参数
     *
     * @return static <T> String类型返回值
     */
    public static <T> String toPrettyYaml(T t) {
        if (t == null) {
            return "null\n";
        }
        try {
            return DEFAULT_PARSER.toPrettyYaml(t);
        } catch (Exception e) {
            throw new YamlParseException("Failed to serialize object to YAML: " + e.getMessage(), e);
        }
    }

    /**
     * 将 YAML 字符串反序列化为对象。
     *
     * @param yamlStr YAML 字符串
     * @param <T>     泛型参数
     * @return 反序列化后的对象，若失败返回 null
     */
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param clazz ClassT类型参数
     * @return static <T> T类型返回值
     */
    /**
     * fromYaml方法。
     * * @param yamlStr String类型参数
     *
     * @param clazz ClassT类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T fromYaml(String yamlStr, Class<T> clazz) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return null;
        }
        return new YamlParser().parse(yamlStr, clazz);
    }

    /**
     * 将 YAML 字符串反序列化为对象，使用 TypeReference 保留泛型类型。
     *
     * @param yamlStr YAML 字符串
     * @param typeRef 类型引用
     * @param <T>     泛型参数
     * @return 反序列化后的对象，若失败返回 null
     */
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param typeRef TypeReferenceT类型参数
     * @return static <T> T类型返回值
     */
    /**
     * fromYaml方法。
     * * @param yamlStr String类型参数
     *
     * @param typeRef TypeReferenceT类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T fromYaml(String yamlStr, TypeReference<T> typeRef) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return null;
        }
        try {
            Object result = new YamlParser().parse(yamlStr);
            return cast(result, typeRef);
        } catch (Exception e) {
            throw new YamlParseException("Failed to deserialize YAML: " + e.getMessage(), e);
        }
    }

    /**
     * 将 YAML 字符串解析为通用对象（{@link Map} 或 {@link List}）。
     *
     * @param yamlStr YAML 字符串
     * @return 解析后的对象
     */
    /**
     * parse方法。
     *      * @param yamlStr String类型参数
     * @return static Object类型返回值
     */
    /**
     * parse方法。
     * * @param yamlStr String类型参数
     *
     * @return static Object类型返回值
     */
    public static Object parse(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return null;
        }
        return new YamlParser().parse(yamlStr);
    }

    /**
     * 使用 YAMLPath 表达式查询 YAML 数据。
     * <p>
     * 支持的路径示例：
     * <ul>
     *   <li>"$.name" - 获取根对象下 name 键的值</li>
     *   <li>"$.items[0]" - 获取 items 数组的第一个元素</li>
     *   <li>"$.config.database.host" - 获取嵌套结构中的值</li>
     * </ul>
     *
     * @param yaml YAML 字符串
     * @param path YAMLPath 表达式
     * @return 匹配结果的列表
     */
    /**
     * query方法。
     *      * @param yaml String类型参数
     * @param path String类型参数
     * @return static List<Object>类型返回值
     */
    /**
     * query方法。
     * * @param yaml String类型参数
     *
     * @param path String类型参数
     * @return static List<Object>类型返回值
     */
    public static List<Object> query(String yaml, String path) {
        return new YamlPathParser().query(yaml, path);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object obj, TypeReference<T> typeRef) {
        // 简单的类型转换逻辑
        Class<?> rawType = typeRef.getRawType();
        if (rawType.isInstance(obj)) {
            return (T) obj;
        }
        if (obj instanceof Map && rawType.isAssignableFrom(Map.class)) {
            return (T) obj;
        }
        if (obj instanceof List && rawType.isAssignableFrom(List.class)) {
            return (T) obj;
        }
        throw new YamlParseException("Cannot cast " + obj.getClass().getName() + " to " + rawType.getName());
    }
}
