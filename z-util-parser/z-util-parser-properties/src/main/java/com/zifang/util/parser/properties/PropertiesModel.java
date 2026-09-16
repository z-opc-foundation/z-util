package com.zifang.util.parser.properties;

import java.util.*;

/**
 * Properties 数据模型，包含键值对、有序键列表及注释信息。
 *
 * @author zifang
 */

/**
 * PropertiesModel类。
 */
public class PropertiesModel {

    private final Map<String, String> properties = new LinkedHashMap<>();
    private final List<String> orderedKeys = new ArrayList<>();
    private final Map<String, String> comments = new LinkedHashMap<>();

    /**
     * 设置一个属性。
     *
     * @param key   属性键
     * @param value 属性值
     */
    /**
     * setProperty方法。
     * * @param key String类型参数
     *
     * @param value String类型参数
     */
    public void setProperty(String key, String value) {
        if (!properties.containsKey(key)) {
            orderedKeys.add(key);
        }
        properties.put(key, value);
    }

    /**
     * 获取属性值。
     *
     * @param key 属性键
     * @return 属性值，若不存在返回 null
     */
    /**
     * getProperty方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * 获取所有属性键的有序列表。
     *
     * @return 键列表
     */
    /**
     * getOrderedKeys方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getOrderedKeys() {
        return Collections.unmodifiableList(orderedKeys);
    }

    /**
     * 获取所有属性。
     *
     * @return 属性 Map
     */
    /**
     * getProperties方法。
     *
     * @return Map<String, String>类型返回值
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * 设置键的注释。
     *
     * @param key     属性键
     * @param comment 注释内容（不含 # 或 ! 前缀）
     */
    /**
     * setComment方法。
     * * @param key String类型参数
     *
     * @param comment String类型参数
     */
    public void setComment(String key, String comment) {
        comments.put(key, comment);
    }

    /**
     * 获取键的注释。
     *
     * @param key 属性键
     * @return 注释内容，若无注释返回 null
     */
    /**
     * getComment方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String getComment(String key) {
        return comments.get(key);
    }

    /**
     * 检查是否包含指定键。
     *
     * @param key 属性键
     * @return 是否包含
     */
    /**
     * containsKey方法。
     * * @param key String类型参数
     *
     * @return boolean类型返回值
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * 获取属性值，如果不存在则返回默认值。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值，若不存在返回默认值
     */
    /**
     * getPropertyOrDefault方法。
     * * @param key String类型参数
     *
     * @param defaultValue String类型参数
     * @return String类型返回值
     */
    public String getPropertyOrDefault(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * 删除指定键的属性。
     *
     * @param key 属性键
     * @return 被删除的值，若不存在返回 null
     */
    /**
     * removeProperty方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String removeProperty(String key) {
        orderedKeys.remove(key);
        return properties.remove(key);
    }

    /**
     * 获取所有属性键的列表。
     *
     * @return 键列表
     */
    /**
     * getAllKeys方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getAllKeys() {
        return getOrderedKeys();
    }

    /**
     * 获取属性数量。
     *
     * @return 属性数量
     */
    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        return properties.size();
    }

    /**
     * 转换为标准 Properties 格式字符串。
     * 注释不会在默认 toString 中输出，需要使用 store() 方法保留注释。
     *
     * @return Properties 格式字符串
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : orderedKeys) {
            String value = properties.get(key);
            sb.append(key).append("=").append(value).append("\n");
        }
        return sb.toString();
    }
}
