package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 容量上界装饰器：在 {@link Cache} 之上额外加一层"软上限"（不会触发 delegate 的 evict 策略）。
 * <p>
 * 用法场景：测试 / 临时保护——确保某把缓存不会无限涨。
 * <p>
 * 超出时 {@link #put} 静默丢弃（不抛错）；{@link #tryPut} 返回 boolean 供业务判别。
 */
public class BoundedCache<K, V> extends ForwardingCache<K, V> {

    private final Cache<K, V> delegate;
    private final long maxSize;
    private final AtomicLong currentSize = new AtomicLong();

    public BoundedCache(Cache<K, V> delegate, long maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("maxSize must be > 0");
        this.delegate = delegate;
        this.maxSize = maxSize;
        this.currentSize.set(delegate.size());
    }

    @Override
    protected Cache<K, V> delegate() {
        return delegate;
    }

    @Override
    public void put(K key, V value) {
        if (currentSize.get() >= maxSize && !delegate.contains(key)) {
            return;   // 满了且是新的 → 丢弃
        }
        delegate.put(key, value);
        currentSize.set(delegate.size());
    }

    /**
     * 满了不丢，返回 false。
     */
    public boolean tryPut(K key, V value) {
        if (currentSize.get() >= maxSize && !delegate.contains(key)) {
            return false;
        }
        delegate.put(key, value);
        currentSize.set(delegate.size());
        return true;
    }

    @Override
    public boolean remove(K key) {
        boolean r = delegate.remove(key);
        currentSize.set(delegate.size());
        return r;
    }

    @Override
    public void clear() {
        delegate.clear();
        currentSize.set(0);
    }

    public long maxSize() {
        return maxSize;
    }

    public long currentSize() {
        return currentSize.get();
    }
}
