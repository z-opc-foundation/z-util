package com.zifang.util.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 自研 W-TinyLFU 缓存实现。
 * <p>
 * 结构：
 * <ul>
 *   <li><b>Window 区段</b>（默认占 1% 容量）：新写入的 key 先进入 Window（短 LRU）</li>
 *   <li><b>Main 区段</b>（默认占 99% 容量）：经历过 Window 淘汰竞争的 key 进入 Main（LRU）</li>
 *   <li><b>CountMinSketch</b>：4-bit 频次近似器。Window 淘汰时弹出的候选 K，
 *       若 sketch.estimate(candidate) &gt; sketch.estimate(mainLruHead)，则胜出（保留并淘汰 Main 队首），
 *       反之直接丢弃。这能有效抗扫描（scan resistance）。</li>
 * </ul>
 * <p>
 * 线程安全：所有方法在 synchronized 块中完成（简单实现，性能足够大多数业务）。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class WTinyLfuCache<K, V> implements Cache<K, V> {

    // ===== 核心区段 =====
    private final LruNode<K, V> window;       // 1% 容量，新写入
    private final LruNode<K, V> main;         // 99% 容量
    private final CountMinSketch sketch;      // 频次近似器

    // ===== 配置 =====
    private final String name;
    private final long maximumSize;
    private final long expireAfterWriteNanos;
    private final long expireAfterAccessNanos;
    private final long refreshAfterWriteNanos;
    private final long cleanupIntervalNanos;
    private final boolean recordStats;
    private final Set<RemovalListener<K, V>> listeners;
    private final boolean nullValueProtection;
    private final Expiry<K, V> expiry;

    // ===== 运行时状态 =====
    private final LinkedHashMap<K, Node<K, V>> data;  // 总索引：O(1) 取 Node 字段（writeNanos/accessNanos）
    private final CacheStats stats = new CacheStats();
    private final ScheduledExecutorService scheduler;
    private final AtomicLong approximateSize = new AtomicLong(0L);
    private volatile boolean shutdown = false;

    WTinyLfuCache(CacheBuilder<K, V> b) {
        this.name = b.getName();
        this.maximumSize = b.getMaximumSize();
        this.expireAfterWriteNanos = b.getExpireAfterWriteNanos();
        this.expireAfterAccessNanos = b.getExpireAfterAccessNanos();
        this.refreshAfterWriteNanos = b.getRefreshAfterWriteNanos();
        // 复用 MemoryCache 的清理间隔算法：min expire 的 1/4，下限 1s，上限 30s
        long minExp = Math.min(
                expireAfterWriteNanos > 0 ? expireAfterWriteNanos : Long.MAX_VALUE,
                expireAfterAccessNanos > 0 ? expireAfterAccessNanos : Long.MAX_VALUE);
        this.cleanupIntervalNanos = minExp == Long.MAX_VALUE
                ? TimeUnit.SECONDS.toNanos(30)
                : Math.max(TimeUnit.SECONDS.toNanos(1), minExp / 4);
        this.recordStats = b.isRecordStats();
        this.listeners = b.getListeners() == null ? Collections.emptySet() : b.getListeners();
        this.nullValueProtection = b.isNullValueProtection();
        this.expiry = b.getExpiry();

        // 计算 Window/Main 容量（保持 1% / 99%）
        int total = (int) Math.max(2, b.getMaximumSize());
        int windowCap = Math.max(1, (int) (total * 0.01));
        int mainCap = total - windowCap;
        this.window = new LruNode<>(windowCap);
        this.main = new LruNode<>(mainCap);

        // CMS 大小：约为总容量 10 倍，最小 64；sampleSize ≈ 10 * total
        int cmsSize = nextPowerOf2(Math.max(64, total * 10));
        int sampleSize = Math.max(10, total * 10);
        this.sketch = new CountMinSketch(cmsSize, sampleSize);

        this.data = new LinkedHashMap<>(total, 0.75f, true);

        // 后台清理线程
        if (cleanupIntervalNanos > 0) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "z-util-cache-wtinylfu-cleanup-" + name);
                t.setDaemon(true);
                return t;
            });
            long intervalMs = TimeUnit.NANOSECONDS.toMillis(cleanupIntervalNanos);
            this.scheduler.scheduleWithFixedDelay(this::cleanupExpired,
                    intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        } else {
            this.scheduler = null;
        }
    }

    // ===== Cache 接口实现 =====

    private static int nextPowerOf2(int n) {
        if (n < 2) return 2;
        return Integer.highestOneBit(n - 1) << 1;
    }

    @Override
    public V get(K key) {
        return getOrLoad(key, null);
    }

    public V getOrLoad(K key, Function<K, V> loader) {
        checkNotShutdown();
        synchronized (this) {
            Node<K, V> n = data.get(key);
            if (n != null) {
                if (isExpired(n, false)) {
                    removeLocked(key, RemovalListener.RemovalCause.EXPIRED);
                    stats.recordMiss();
                } else {
                    if (n.isNullSentinel) {
                        stats.recordHit();
                        return null;
                    }
                    // 频次 +1
                    sketch.increment(key);
                    // 刷新 accessNanos
                    n.accessNanos = System.nanoTime();
                    // 触发 LRU 顺序更新（LinkedHashMap accessOrder=true 会自动移到队尾）
                    return n.value;
                }
            } else {
                stats.recordMiss();
            }
            // miss → loader
            if (loader == null) return null;
            V loaded = loadValue(key, loader);
            if (loaded == null && nullValueProtection) {
                putSentinel(key);
                return null;
            }
            if (loaded != null) {
                put(key, loaded);
            }
            return loaded;
        }
    }

    @Override
    public void put(K key, V value) {
        checkNotShutdown();
        if (key == null || value == null) {
            throw new NullPointerException("WTinyLfuCache does not allow null key/value (use nullValueProtection)");
        }
        synchronized (this) {
            sketch.increment(key);
            Node<K, V> existing = data.get(key);
            if (existing != null) {
                // 覆盖：更新 value、writeNanos；保持所在区段不变
                existing.value = value;
                existing.writeNanos = System.nanoTime();
                existing.accessNanos = existing.writeNanos;
                existing.isNullSentinel = false;
                // 更新所在 LRU 区段
                if (window.contains(key)) window.put(key, value);
                else main.put(key, value);
                return;
            }
            // 新写入 → 先进 Window
            Node<K, V> n = new Node<>();
            n.key = key;
            n.value = value;
            n.writeNanos = System.nanoTime();
            n.accessNanos = n.writeNanos;
            data.put(key, n);
            approximateSize.incrementAndGet();
            window.put(key, value);
            evict();
        }
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return getOrLoad(key, loader == null ? null : loader::apply);
    }

    @Override
    public boolean contains(K key) {
        synchronized (this) {
            Node<K, V> n = data.get(key);
            return n != null && !isExpired(n, false);
        }
    }

    @Override
    public void put(K key, Function<? super K, ? extends V> loader) {
        V v = loader.apply(key);
        if (v != null) put(key, v);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        java.util.Map<K, V> result = new java.util.LinkedHashMap<>();
        for (K k : keys) {
            V v = get(k);
            if (v != null) result.put(k, v);
        }
        return result;
    }

    @Override
    public boolean remove(K key) {
        synchronized (this) {
            Node<K, V> n = data.get(key);
            if (n == null) return false;
            removeLocked(key, RemovalListener.RemovalCause.EXPLICIT);
            return true;
        }
    }

    @Override
    public void invalidateAll() {
        clear();
    }

    @Override
    public long size() {
        return approximateSize.get();
    }

    @Override
    public void clear() {
        synchronized (this) {
            for (K k : new java.util.ArrayList<>(data.keySet())) {
                removeLocked(k, RemovalListener.RemovalCause.EXPLICIT);
            }
            window.setMaxCapacity(1);
            main.setMaxCapacity((int) Math.max(1, maximumSize - 1));
        }
    }

    @Override
    public CacheStats stats() {
        return recordStats ? stats : null;
    }

    @Override
    public Map<K, V> asMap() {
        synchronized (this) {
            java.util.Map<K, V> view = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<K, Node<K, V>> e : data.entrySet()) {
                Node<K, V> n = e.getValue();
                if (!isExpired(n, false) && !n.isNullSentinel) {
                    view.put(e.getKey(), n.value);
                }
            }
            return java.util.Collections.unmodifiableMap(view);
        }
    }

    @Override
    public Set<RemovalListener<K, V>> removalListeners() {
        return listeners;
    }

    @Override
    public String getName() {
        return name;
    }

    // ===== 内部 =====

    @Override
    public void shutdown() {
        if (shutdown) return;
        shutdown = true;
        if (scheduler != null) scheduler.shutdownNow();
    }

    /**
     * 弹一个 entry：先看 Window 是否超额；超额则按 W-TinyLFU 准入策略决出"输家"。
     */
    private void evict() {
        if (approximateSize.get() <= maximumSize) return;

        // Window 满了：从 Window 弹一个 candidate
        if (window.size() > window.maxCapacity()) {
            Map.Entry<K, V> candidate = window.evict();
            if (candidate == null) return;
            K candKey = candidate.getKey();

            // Main 队首
            Map.Entry<K, V> victim = !main.isEmpty() ? main.peekFirst() : null;
            if (victim == null) {
                // Main 是空的 → candidate 直接进 Main（建缓存的瞬时）
                main.put(candKey, candidate.getValue());
                return;
            }

            // W-TinyLFU 准入：candidate 的 sketch 频次 vs victim 的 sketch 频次
            int candFreq = sketch.estimate(candKey);
            int victimFreq = sketch.estimate(victim.getKey());
            if (candFreq > victimFreq) {
                // candidate 胜出：进 Main，弹出 victim
                main.remove(victim.getKey());
                Node<K, V> vNode = data.get(victim.getKey());
                data.remove(victim.getKey());
                approximateSize.decrementAndGet();
                fireRemoval(victim.getKey(), victim.getValue(), RemovalListener.RemovalCause.SIZE_LIMIT);
                stats.recordEviction();
                main.put(candKey, candidate.getValue());
            } else {
                // candidate 输：丢弃
                data.remove(candKey);
                approximateSize.decrementAndGet();
                stats.recordEviction();
                fireRemoval(candKey, candidate.getValue(), RemovalListener.RemovalCause.SIZE_LIMIT);
            }
        } else if (main.size() > main.maxCapacity()) {
            // 只 Main 满：直接弹 Main 队首
            Map.Entry<K, V> victim = main.evict();
            if (victim != null) {
                data.remove(victim.getKey());
                approximateSize.decrementAndGet();
                fireRemoval(victim.getKey(), victim.getValue(), RemovalListener.RemovalCause.SIZE_LIMIT);
                stats.recordEviction();
            }
        }
    }

    private void putSentinel(K key) {
        // NullValue 击穿：直接 put 一份到 data，频次不增（避免污染 sketch）
        Node<K, V> n = new Node<>();
        n.key = key;
        @SuppressWarnings("unchecked")
        V sentinel = (V) NullValue.INSTANCE;
        n.value = sentinel;
        n.writeNanos = System.nanoTime();
        n.accessNanos = n.writeNanos;
        n.isNullSentinel = true;
        data.put(key, n);
        approximateSize.incrementAndGet();
        window.put(key, sentinel);
        evict();
    }

    private V loadValue(K key, Function<K, V> loader) {
        long t0 = System.nanoTime();
        try {
            V v = loader.apply(key);
            if (v != null) {
                stats.recordLoadSuccess(System.nanoTime() - t0);
            } else {
                stats.recordLoadFailure(System.nanoTime() - t0);
            }
            return v;
        } catch (RuntimeException e) {
            stats.recordLoadFailure(System.nanoTime() - t0);
            throw e;
        }
    }

    private boolean isExpired(Node<K, V> n, boolean forRefresh) {
        long now = System.nanoTime();
        if (expiry != null) {
            // 简化：创建/读/写 都用同一接口（ExpirePolicy 不实现完整三段式）
            return false;   // TODO: 接 Expiry 三段式
        }
        if (expireAfterWriteNanos > 0 && now - n.writeNanos >= expireAfterWriteNanos) return true;
        if (expireAfterAccessNanos > 0 && now - n.accessNanos >= expireAfterAccessNanos) return true;
        if (forRefresh && refreshAfterWriteNanos > 0 && now - n.writeNanos >= refreshAfterWriteNanos) return true;
        return false;
    }

    private void removeLocked(K key, RemovalListener.RemovalCause cause) {
        Node<K, V> n = data.remove(key);
        if (n != null) {
            window.remove(key);
            main.remove(key);
            approximateSize.decrementAndGet();
            fireRemoval(key, n.value, cause);
            stats.recordRemoval(cause);
        }
    }

    private void fireRemoval(K key, V value, RemovalListener.RemovalCause cause) {
        RemovalListener.RemovalNotification<K, V> n =
                new RemovalListener.RemovalNotification<>(key, value, cause);
        for (RemovalListener<K, V> l : listeners) {
            try {
                l.onRemoval(n);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void cleanupExpired() {

        if (shutdown) {
            return;
        }

        try {
            long now = System.nanoTime();
            synchronized (this) {
                java.util.Iterator<java.util.Map.Entry<K, Node<K, V>>> it = data.entrySet().iterator();
                int cleaned = 0;
                while (it.hasNext() && cleaned < 64) {
                    java.util.Map.Entry<K, Node<K, V>> e = it.next();
                    Node<K, V> n = e.getValue();
                    if (now - n.writeNanos >= expireAfterWriteNanos && expireAfterWriteNanos > 0) {
                        removeLocked(e.getKey(), RemovalListener.RemovalCause.EXPIRED);
                        cleaned++;
                    } else if (now - n.accessNanos >= expireAfterAccessNanos && expireAfterAccessNanos > 0) {
                        removeLocked(e.getKey(), RemovalListener.RemovalCause.ACCESS_EXPIRED);
                        cleaned++;
                    }
                }
            }
        } catch (RuntimeException ignored) {

        }
    }

    private void checkNotShutdown() {
        if (shutdown) throw new IllegalStateException("cache shut down: " + name);
    }

    // ===== 内部类 =====
    private static final class Node<K, V> {
        K key;
        volatile V value;
        volatile long writeNanos;
        volatile long accessNanos;
        volatile boolean isNullSentinel;
    }
}
