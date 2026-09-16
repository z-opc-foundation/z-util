package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;
import com.zifang.util.cache.CacheStats;
import com.zifang.util.cache.RemovalListener;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 空缓存装饰器：所有操作都是 no-op，get 永远返回 null。
 * <p>
 * 用法：测试时替换真实 cache，做 null-object pattern。
 */
public class NullCache<K, V> implements Cache<K, V> {

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return loader == null ? null : loader.apply(key);
    }

    @Override
    public boolean contains(K key) {
        return false;
    }

    @Override
    public void put(K key, V value) {
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
    }

    @Override
    public void put(K key, Function<? super K, ? extends V> loader) {
    }

    @Override
    public boolean remove(K key) {
        return false;
    }

    @Override
    public void invalidateAll() {
    }

    @Override
    public void clear() {
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public String getName() {
        return "null-cache";
    }

    @Override
    public CacheStats stats() {
        return null;
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        return Collections.emptyMap();
    }

    @Override
    public Map<K, V> asMap() {
        return Collections.emptyMap();
    }

    @Override
    public Set<RemovalListener<K, V>> removalListeners() {
        return Collections.emptySet();
    }

    @Override
    public void shutdown() {
    }
}
