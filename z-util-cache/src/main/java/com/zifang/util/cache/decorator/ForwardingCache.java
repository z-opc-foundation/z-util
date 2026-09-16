package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;
import com.zifang.util.cache.CacheStats;
import com.zifang.util.cache.RemovalListener;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 装饰器基类：把 {@link Cache} 全部方法转发到 {@link #delegate}，子类只需覆盖关心的方法。
 * <p>
 * 抽象类模式（避免每个装饰器写 13 个方法）。对标 Guava {@code ForwardingCache}。
 */
public abstract class ForwardingCache<K, V> implements Cache<K, V> {

    protected abstract Cache<K, V> delegate();

    @Override
    public V get(K key) {
        return delegate().get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return delegate().get(key, loader);
    }

    @Override
    public boolean contains(K key) {
        return delegate().contains(key);
    }

    @Override
    public void put(K key, V value) {
        delegate().put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate().putAll(map);
    }

    @Override
    public void put(K key, Function<? super K, ? extends V> loader) {
        delegate().put(key, loader);
    }

    @Override
    public boolean remove(K key) {
        return delegate().remove(key);
    }

    @Override
    public void invalidateAll() {
        delegate().invalidateAll();
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public long size() {
        return delegate().size();
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public CacheStats stats() {
        return delegate().stats();
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        return delegate().getAllPresent(keys);
    }

    @Override
    public Map<K, V> asMap() {
        return delegate().asMap();
    }

    @Override
    public Set<RemovalListener<K, V>> removalListeners() {
        return delegate().removalListeners();
    }

    @Override
    public void shutdown() {
        delegate().shutdown();
    }
}
