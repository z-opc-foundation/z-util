package com.zifang.util.core.pattern.chain;

import java.util.*;

/**
 * 链执行上下文
 * <p>
 * 携带数据在链中传递，支持任意类型的key-value数据存储
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author zifang
 */
public class ChainContext<K, V> implements Map<K, V> {

    private final Map<K, V> data;
    private final List<ChainContext<K, V>> history;
    private boolean executed;

    /**
     * ChainContext方法。
     */
    public ChainContext() {
        this.data = new HashMap<>();
        this.history = new ArrayList<>();
        this.executed = false;
    }

    /**
     * ChainContext方法。
     * * @param initialData MapK,类型参数
     */
    public ChainContext(Map<K, V> initialData) {
        this.data = new HashMap<>(initialData);
        this.history = new ArrayList<>();
        this.executed = false;
    }

    /**
     * 创建并初始化上下文的工厂方法
     */
    public static <K, V> ChainContext<K, V> create() {
        return new ChainContext<>();
    }

    /**
     * create方法。
     * * @param initialData MapK,类型参数
     *
     * @return static <K, V> ChainContext<K, V>类型返回值
     */
    public static <K, V> ChainContext<K, V> create(Map<K, V> initialData) {
        return new ChainContext<>(initialData);
    }

    /**
     * 获取值并自动类型转换
     */
    @SuppressWarnings("unchecked")
    /**
     * get方法。
     *      * @param key K类型参数
     * @param type ClassT类型参数
     * @return <T extends V> T类型返回值
     */
    public <T extends V> T get(K key, Class<T> type) {
        V value = data.get(key);
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException("Value for key " + key + " is not of type " + type);
        }
        return (T) value;
    }

    /**
     * 获取值，如果不存在则使用默认值
     */
    public V getOrDefaultValue(K key, V defaultValue) {
        V value = data.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 如果不存在则插入
     */
    public V putIfAbsent(K key, V value) {
        return data.putIfAbsent(key, value);
    }

    /**
     * 放入值并返回this，支持链式调用
     */
    public ChainContext<K, V> putChain(K key, V value) {
        data.put(key, value);
        return this;
    }

    /**
     * 获取执行历史
     */
    public List<ChainContext<K, V>> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * 标记为已执行
     */
    public void markExecuted() {
        this.executed = true;
    }

    /**
     * 是否已执行
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * 复制上下文
     */
    public ChainContext<K, V> copy() {
        ChainContext<K, V> copy = new ChainContext<>(this.data);
        copy.history.addAll(this.history);
        copy.executed = this.executed;
        return copy;
    }

    // Map接口实现

    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        return data.size();
    }

    @Override
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    /**
     * containsKey方法。
     *      * @param key Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    /**
     * containsValue方法。
     *      * @param value Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    /**
     * get方法。
     *      * @param key Object类型参数
     * @return V类型返回值
     */
    public V get(Object key) {
        return data.get(key);
    }

    @Override
    /**
     * put方法。
     *      * @param key K类型参数
     * @param value V类型参数
     * @return V类型返回值
     */
    public V put(K key, V value) {
        return data.put(key, value);
    }

    @Override
    /**
     * remove方法。
     *      * @param key Object类型参数
     * @return V类型返回值
     */
    public V remove(Object key) {
        return data.remove(key);
    }

    @Override
    /**
     * putAll方法。
     *      * @param m Map?类型参数
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        data.putAll(m);
    }

    @Override
    /**
     * clear方法。
     */
    public void clear() {
        data.clear();
    }

    @Override
    /**
     * keySet方法。
     * @return Set<K>类型返回值
     */
    public Set<K> keySet() {
        return data.keySet();
    }

    @Override
    /**
     * values方法。
     * @return Collection<V>类型返回值
     */
    public Collection<V> values() {
        return data.values();
    }

    @Override
    /**
     * entrySet方法。
     * @return Set<Entry<K, V>>类型返回值
     */
    public Set<Entry<K, V>> entrySet() {
        return data.entrySet();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ChainContext{" +
                "data=" + data +
                ", executed=" + executed +
                '}';
    }
}