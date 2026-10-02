package com.zifang.util.yaml;

import com.zifang.util.yaml.exception.YamlParseException;
import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * YAML 解析器，将 YAML 字符串解析为 Java 对象。
 *
 * @author zifang
 */
/**
 * YamlParser类。
 */

/**
 * YamlParser类。
 */
public class YamlParser {

    private final Yaml yaml;

    /**
     * YamlParser方法。
     */
    /**
     * YamlParser方法。
     */
    public YamlParser() {
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        representer.getPropertyUtils().setSkipMissingProperties(true);
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(loaderOptions);
        this.yaml = new Yaml(constructor, representer);
    }

    /**
     * 将 YAML 字符串解析为 Java 对象。
     *
     * @param yamlStr YAML 字符串
     * @return 解析后的对象
     * @throws YamlParseException 如果 YAML 格式非法
     */
    /**
     * parse方法。
     *      * @param yamlStr String类型参数
     * @return Object类型返回值
     */
    /**
     * parse方法。
     * * @param yamlStr String类型参数
     *
     * @return Object类型返回值
     */
    public Object parse(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return null;
        }
        try {
            Object result = yaml.load(new StringReader(yamlStr));
            return convert(result);
        } catch (Exception e) {
            throw new YamlParseException("Failed to parse YAML: " + e.getMessage(), e);
        }
    }

    /**
     * 将 YAML 字符串解析为指定类型。
     *
     * @param yamlStr YAML 字符串
     * @param clazz   目标类型
     * @param <T>     泛型类型
     * @return 解析后的对象
     * @throws YamlParseException 如果 YAML 格式非法
     */
    /**
     * parse方法。
     *      * @param yamlStr String类型参数
     * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    /**
     * parse方法。
     * * @param yamlStr String类型参数
     *
     * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    public <T> T parse(String yamlStr, Class<T> clazz) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return null;
        }
        try {
            LoaderOptions opts = new LoaderOptions();
            Yaml typedYaml = new Yaml(new Constructor(clazz, opts));
            Object result = typedYaml.load(new StringReader(yamlStr));
            return clazz.cast(result);
        } catch (Exception e) {
            throw new YamlParseException("Failed to parse YAML to " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * 将对象序列化为 YAML 字符串。
     *
     * @param obj 待序列化的对象
     * @return YAML 字符串
     */
    /**
     * toYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    /**
     * toYaml方法。
     * * @param obj Object类型参数
     *
     * @return String类型返回值
     */
    public String toYaml(Object obj) {
        if (obj == null) {
            return "null\n";
        }
        return yaml.dump(obj);
    }

    /**
     * 将对象序列化为格式化的 YAML 字符串（带缩进）。
     *
     * @param obj 待序列化的对象
     * @return 格式化的 YAML 字符串
     */
    /**
     * toPrettyYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    /**
     * toPrettyYaml方法。
     * * @param obj Object类型参数
     *
     * @return String类型返回值
     */
    public String toPrettyYaml(Object obj) {
        if (obj == null) {
            return "null\n";
        }
        return yaml.dump(obj);
    }

    /**
     * 将 SnakeYAML 原生对象转换为我们的模型类。
     */
    @SuppressWarnings("unchecked")
    private Object convert(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            YamlMap map = new YamlMap();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
                map.put(entry.getKey(), convert(entry.getValue()));
            }
            return map;
        }
        if (obj instanceof List) {
            YamlArray array = new YamlArray();
            for (Object item : (List<Object>) obj) {
                array.add(convert(item));
            }
            return array;
        }
        return obj;
    }
}
