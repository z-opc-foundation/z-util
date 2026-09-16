package com.zifang.util.cache;

/**
 * 加载型缓存的 key-value 来源。
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    /**
     * 加载一个 key 的值。抛错时调用方会收到原始异常。
     */
    V load(K key) throws Exception;
}
