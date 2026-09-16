package com.zifang.util.core.pattern.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least Recently Used（最近最少使用）淘汰策略实现类
 * <p>
 * 当缓存容量达到上限时，会优先淘汰最近最少使用的元素
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LRU<K, V> extends LinkedHashMap<K, V> {

    // 保存缓存的容量
    private int capacity;

    /**
     * 构造一个LRU缓存
     *
     * @param capacity   缓存容量，必须大于0
     * @param loadFactor 负载因子，用于确定扩容时机
     */
    public LRU(int capacity, float loadFactor) {
        super(capacity, loadFactor, true);
        this.capacity = capacity;
    }

    /**
     * 重写removeEldestEntry()方法设置何时移除旧元素
     * <p>
     * 当元素个数大于缓存容量时，移除最旧的元素
     *
     * @param eldest 最近最少使用的元素条目
     * @return 如果应该移除最旧元素则返回true，否则返回false
     */
    @Override
    /**
     * removeEldestEntry方法。
     *      * @param eldest Map.EntryK,类型参数
     * @return boolean类型返回值
     */
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 当元素个数大于了缓存的容量, 就移除元素
        return size() > this.capacity;
    }
}