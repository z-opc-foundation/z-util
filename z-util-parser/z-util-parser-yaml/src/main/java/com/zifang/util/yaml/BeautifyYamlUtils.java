package com.zifang.util.yaml;

import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;

/**
 * YAML 美化工具，提供 YAML 格式化、压缩、排序等功能。
 *
 * @author zifang
 */
/**
 * BeautifyYamlUtils类。
 */

/**
 * BeautifyYamlUtils类。
 */
public class BeautifyYamlUtils {

    private BeautifyYamlUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 格式化 YAML 字符串（增加缩进，使结构更清晰）。
     *
     * @param yamlStr 原始 YAML 字符串
     * @return 格式化后的 YAML 字符串
     */
    /**
     * beautify方法。
     *      * @param yamlStr String类型参数
     * @return static String类型返回值
     */
    /**
     * beautify方法。
     * * @param yamlStr String类型参数
     *
     * @return static String类型返回值
     */
    public static String beautify(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return yamlStr;
        }
        Object parsed = new YamlParser().parse(yamlStr);
        return new YamlParser().toPrettyYaml(parsed);
    }

    /**
     * 压缩 YAML 字符串（移除多余空行和注释）。
     *
     * @param yamlStr 原始 YAML 字符串
     * @return 压缩后的 YAML 字符串
     */
    /**
     * minify方法。
     *      * @param yamlStr String类型参数
     * @return static String类型返回值
     */
    /**
     * minify方法。
     * * @param yamlStr String类型参数
     *
     * @return static String类型返回值
     */
    public static String minify(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return yamlStr;
        }
        Object parsed = new YamlParser().parse(yamlStr);
        String yaml = new YamlParser().toYaml(parsed);
        // 移除多余空行
        return yaml.replaceAll("\n{3,}", "\n\n");
    }

    /**
     * 对 YAML 的键进行字母排序（仅对映射节点生效）。
     *
     * @param yamlStr 原始 YAML 字符串
     * @return 键排序后的 YAML 字符串
     */
    /**
     * sortKeys方法。
     *      * @param yamlStr String类型参数
     * @return static String类型返回值
     */
    /**
     * sortKeys方法。
     * * @param yamlStr String类型参数
     *
     * @return static String类型返回值
     */
    public static String sortKeys(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) {
            return yamlStr;
        }
        Object parsed = new YamlParser().parse(yamlStr);
        Object sorted = sortKeysRecursive(parsed);
        return new YamlParser().toPrettyYaml(sorted);
    }

    @SuppressWarnings("unchecked")
    private static Object sortKeysRecursive(Object obj) {
        if (obj instanceof YamlMap) {
            YamlMap map = (YamlMap) obj;
            YamlMap sorted = new YamlMap();
            java.util.List<String> keys = new java.util.ArrayList<>(map.keySet());
            java.util.Collections.sort(keys);
            for (String key : keys) {
                sorted.put(key, sortKeysRecursive(map.get(key)));
            }
            return sorted;
        }
        if (obj instanceof YamlArray) {
            YamlArray arr = (YamlArray) obj;
            YamlArray sorted = new YamlArray();
            for (Object item : arr) {
                sorted.add(sortKeysRecursive(item));
            }
            return sorted;
        }
        return obj;
    }
}
