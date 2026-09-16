package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;

import java.util.concurrent.atomic.LongAdder;

/**
 * 指标装饰器：在每次 get/put/remove 上自增计数器，方便 Prometheus / Log 抓取。
 * <p>
 * 用法：
 * <pre>{@code
 *   Cache<K, V> base = ...;
 *   MeteredCache<K, V> metered = new MeteredCache<>(base);
 *   metered.get("k");
 *   metered.meter().getCount();   // 总访问数
 *   metered.meter().hitCount();   // 命中数
 * }</pre>
 */
public class MeteredCache<K, V> extends ForwardingCache<K, V> {

    private final Cache<K, V> delegate;
    private final LongAdder hitCount = new LongAdder();
    private final LongAdder missCount = new LongAdder();
    private final LongAdder putCount = new LongAdder();
    private final LongAdder removeCount = new LongAdder();
    private final LongAdder evictionCount = new LongAdder();
    private final long startNanos = System.nanoTime();

    public MeteredCache(Cache<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Cache<K, V> delegate() {
        return delegate;
    }

    @Override
    public V get(K key) {
        V v = delegate.get(key);
        if (v == null) missCount.increment();
        else hitCount.increment();
        return v;
    }

    @Override
    public V get(K key, java.util.function.Function<? super K, ? extends V> loader) {
        // 不破坏 delegate 行为；统计简化处理（按 hit/miss）
        V v;
        try {
            v = delegate.get(key, loader);
        } catch (RuntimeException e) {
            missCount.increment();
            throw e;
        }
        if (v == null) missCount.increment();
        else hitCount.increment();
        return v;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        putCount.increment();
    }

    @Override
    public boolean remove(K key) {
        boolean removed = delegate.remove(key);
        if (removed) removeCount.increment();
        return removed;
    }

    @Override
    public void clear() {
        long before = delegate.size();
        delegate.clear();
        if (before > 0) evictionCount.add(before);
    }

    public Meter meter() {
        long baseEvict = delegate.stats() == null ? 0L : delegate.stats().evictionCount();
        long elapsed = System.nanoTime() - startNanos;
        return new Meter(
                hitCount.sum(),
                missCount.sum(),
                putCount.sum(),
                removeCount.sum(),
                evictionCount.sum() + baseEvict,
                elapsed);
    }

    /**
     * 不可变快照。
     */
    public static final class Meter {
        private final long hits, misses, puts, removes, evictions, elapsedNanos;

        Meter(long h, long m, long p, long r, long e, long en) {
            this.hits = h;
            this.misses = m;
            this.puts = p;
            this.removes = r;
            this.evictions = e;
            this.elapsedNanos = en;
        }

        public long getCount() {
            return hits + misses;
        }

        public long hitCount() {
            return hits;
        }

        public long missCount() {
            return misses;
        }

        public long putCount() {
            return puts;
        }

        public long removeCount() {
            return removes;
        }

        public long evictionCount() {
            return evictions;
        }

        public double hitRate() {
            long t = getCount();
            return t == 0 ? 0.0 : (double) hits / t;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }
    }
}
