package com.zifang.util.yaml.model;

import com.zifang.util.yaml.define.YamlNodeType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * YAML 映射节点，对应 YAML 中的映射（mapping）结构，
 * 即键值对集合，类似 JSON 中的对象。
 *
 * @author zifang
 */
/**
 * YamlMap类。
 */

/**
 * YamlMap类。
 */
public class YamlMap implements Map<String, Object> {

    private final Map<String, Object> delegate = new LinkedHashMap<>();

    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        return delegate.size();
    }

    @Override
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    /**
     * containsKey方法。
     *      * @param key Object类型参数
     * @return boolean类型返回值
     */
    /**
     * containsKey方法。
     *      * @param key Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    /**
     * containsValue方法。
     *      * @param value Object类型参数
     * @return boolean类型返回值
     */
    /**
     * containsValue方法。
     *      * @param value Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    /**
     * get方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    /**
     * get方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    /**
     * put方法。
     *      * @param key String类型参数
     * @param value Object类型参数
     * @return Object类型返回值
     */
    /**
     * put方法。
     *      * @param key String类型参数
     * @param value Object类型参数
     * @return Object类型返回值
     */
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    /**
     * remove方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    /**
     * remove方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    /**
     * putAll方法。
     *      * @param m Map?类型参数
     */
    /**
     * putAll方法。
     *      * @param m Map?类型参数
     */
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    @Override
    /**
     * clear方法。
     */
    /**
     * clear方法。
     */
    public void clear() {
        delegate.clear();
    }

    @Override
    /**
     * keySet方法。
     * @return Set<String>类型返回值
     */
    /**
     * keySet方法。
     * @return Set<String>类型返回值
     */
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    /**
     * values方法。
     * @return Collection<Object>类型返回值
     */
    /**
     * values方法。
     * @return Collection<Object>类型返回值
     */
    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    /**
     * entrySet方法。
     * @return Set<Entry<String, Object>>类型返回值
     */
    /**
     * entrySet方法。
     * @return Set<Entry<String, Object>>类型返回值
     */
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    /**
     * getNodeType方法。
     * @return YamlNodeType类型返回值
     */
    /**
     * getNodeType方法。
     *
     * @return YamlNodeType类型返回值
     */
    public YamlNodeType getNodeType() {
        return YamlNodeType.MAP;
    }

    /**
     * 获取字符串值，辅助方法。
     *
     * @param key 键
     * @return 字符串值，若不存在或类型不匹配返回 null
     */
    /**
     * getString方法。
     *      * @param key String类型参数
     * @return String类型返回值
     */
    /**
     * getString方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String getString(String key) {
        Object val = get(key);
        return val instanceof String ? (String) val : null;
    }

    /**
     * 获取整数值，辅助方法。
     *
     * @param key 键
     * @return 整数值，若不存在或类型不匹配返回 null
     */
    /**
     * getInt方法。
     *      * @param key String类型参数
     * @return int类型返回值
     */
    /**
     * getInt方法。
     * * @param key String类型参数
     *
     * @return int类型返回值
     */
    public Integer getInt(String key) {
        Object val = get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }

    /**
     * 获取嵌套映射，辅助方法。
     *
     * @param key 键
     * @return 嵌套的 YamlMap，若不存在或类型不匹配返回 null
     */
    /**
     * getMap方法。
     *      * @param key String类型参数
     * @return YamlMap类型返回值
     */
    /**
     * getMap方法。
     * * @param key String类型参数
     *
     * @return YamlMap类型返回值
     */
    public YamlMap getMap(String key) {
        Object val = get(key);
        return val instanceof YamlMap ? (YamlMap) val : null;
    }

    /**
     * 获取嵌套列表，辅助方法。
     *
     * @param key 键
     * @return 嵌套的 YamlArray，若不存在或类型不匹配返回 null
     */
    /**
     * getArray方法。
     *      * @param key String类型参数
     * @return YamlArray类型返回值
     */
    /**
     * getArray方法。
     * * @param key String类型参数
     *
     * @return YamlArray类型返回值
     */
    public YamlArray getArray(String key) {
        Object val = get(key);
        return val instanceof YamlArray ? (YamlArray) val : null;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return delegate.toString();
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlMap)) return false;
        return delegate.equals(((YamlMap) o).delegate);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return delegate.hashCode();
    }
}
