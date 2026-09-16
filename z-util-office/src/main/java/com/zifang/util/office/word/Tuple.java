package com.zifang.util.office.word;

/**
 * 键值对泛型类
 * 用于存储一一对应的键值数据
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class Tuple<K, V> {
    private K key;
    private V value;

    /**
     * 构造方法
     *
     * @param key   键
     * @param value 值
     */
    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 获取键
     *
     * @return 键
     */
    public K getKey() {
        return key;
    }

    /**
     * 设置键
     *
     * @param key 键
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public V getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(V value) {
        this.value = value;
    }
}
