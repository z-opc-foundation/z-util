package com.zifang.util.parser.ini;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * INI Section 模型
 */

/**
 * IniSection类。
 */
public class IniSection {

    private String name;
    private Map<String, String> values;

    /**
     * IniSection方法。
     */
    public IniSection() {
        this.values = new LinkedHashMap<>();
    }

    /**
     * IniSection方法。
     * * @param name String类型参数
     */
    public IniSection(String name) {
        this.name = name;
        this.values = new LinkedHashMap<>();
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getValues方法。
     *
     * @return Map<String, String>类型返回值
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * setValues方法。
     * * @param values MapString,类型参数
     */
    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    /**
     * get方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String get(String key) {
        return values.get(key);
    }

    /**
     * put方法。
     * * @param key String类型参数
     *
     * @param value String类型参数
     */
    public void put(String key, String value) {
        values.put(key, value);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            sb.append("[").append(name).append("]").append("\n");
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
