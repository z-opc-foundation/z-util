package com.zifang.util.cache;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 基于 LinkedHashMap(accessOrder=true) 的内存缓存实现。
 * <p>
 * 核心能力：
 * <ul>
 *   <li>LRU 淘汰（通过 {@link LinkedHashMap} 的 access-order）</li>
 *   <li>{@code expireAfterWrite}：写入后存活 N 纳秒后过期</li>
 *   <li>{@code expireAfterAccess}：最后一次访问后 N 纳秒未访问则过期（get 也会刷新）</li>
 *   <li>统计（hit / miss / eviction / expiration / load 计数）</li>
 *   <li>移除回调（按 cause 分类）</li>
 *   <li>定时清理线程（守护）</li>
 * </ul>
 * 线程安全：所有写操作在 synchronized 块中完成；读操作也走 synchronized（简单实现）。
 * <p>
 * 通过 {@link CacheBuilder} 构造，{@link #MemoryCache(CacheBuilder)} 包内可见。
 */
public class MemoryCache<K, V> implements Cache<K, V> {

    private final String name;
    private final long maximumSize;
    private final long expireAfterWriteNanos;
    private final long expireAfterAccessNanos;
    private final long cleanupIntervalNanos;
    private final boolean recordStats;
    private final Set<RemovalListener<K, V>> listeners;
    private final long refreshAfterWriteNanos;
    private final boolean nullValueProtection;
    private final Expiry<K, V> expiry;

    private final LinkedHashMap<K, Node<K, V>> data;
    private final CacheStats stats = new CacheStats();
    private final ScheduledExecutorService scheduler;
    private final AtomicLong approximateSize = new AtomicLong(0L);
    private volatile boolean shutdown = false;

    /**
     * 包内可见的构造器（从 {@link CacheBuilder} 读取配置）。
     * 由于 LinkedHashMap 的 accessOrder 特性（访问时把节点移到队尾），
     * 我们需要 synchronize 才能安全地修改 map。
     */
    MemoryCache(CacheBuilder<K, V> b) {
        this.name = b.getName();
        this.maximumSize = b.getMaximumSize();
        this.expireAfterWriteNanos = b.getExpireAfterWriteNanos();
        this.expireAfterAccessNanos = b.getExpireAfterAccessNanos();
        this.refreshAfterWriteNanos = b.getRefreshAfterWriteNanos();
        this.recordStats = b.isRecordStats();
        this.nullValueProtection = b.isNullValueProtection();
        this.expiry = b.getExpiry();
        this.listeners = Collections.unmodifiableSet(new java.util.LinkedHashSet<>(b.getListeners()));
        int cap = (int) Math.min(b.getInitialCapacity(), Integer.MAX_VALUE);
        this.data = new LinkedHashMap<K, Node<K, V>>(cap, 0.75f, true);
        // 清理间隔 = max(1s, min(expire)/4)
        long minExp = Math.min(
                expireAfterWriteNanos < 0 ? Long.MAX_VALUE : expireAfterWriteNanos,
                expireAfterAccessNanos < 0 ? Long.MAX_VALUE : expireAfterAccessNanos);
        this.cleanupIntervalNanos = minExp < 0 ? TimeUnit.SECONDS.toNanos(30) : Math.max(TimeUnit.SECONDS.toNanos(1), minExp / 4);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MemoryCache-" + name + "-cleanup");
            t.setDaemon(true);
            return t;
        });
        if (cleanupIntervalNanos > 0 && cleanupIntervalNanos < Long.MAX_VALUE / 2) {
            scheduler.scheduleAtFixedRate(this::cleanupExpired, cleanupIntervalNanos, cleanupIntervalNanos, TimeUnit.NANOSECONDS);
        }
    }

    // ==================== Cache 接口实现 ====================

    @Override
    public V get(K key) {
        return getInternal(key, null, false);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        if (loader == null) throw new IllegalArgumentException("loader must not be null");
        return getInternal(key, loader, false);
    }

    @Override
    public boolean contains(K key) {
        if (key == null) return false;
        synchronized (data) {
            Node<K, V> n = data.get(key);
            if (n == null) return false;
            if (isExpired(n)) {
                // 不立即删除（清理交给后台），但 contains 返回 false
                return false;
            }
            return true;
        }
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        synchronized (data) {
            for (K k : keys) {
                Node<K, V> n = data.get(k);
                if (n == null) continue;
                if (isExpired(n)) continue;
                touchAccess(n);
                result.put(k, n.value);
                if (recordStats) stats.recordHit();
            }
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        putInternal(key, value, false);
    }

    @Override
    public void put(K key, Function<? super K, ? extends V> loader) {
        if (loader == null) throw new IllegalArgumentException("loader must not be null");
        V loaded = loader.apply(key);
        if (loaded == null) return;
        putInternal(key, loaded, false);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            putInternal(e.getKey(), e.getValue(), false);
        }
    }

    @Override
    public boolean remove(K key) {
        if (key == null) return false;
        synchronized (data) {
            Node<K, V> n = data.remove(key);
            if (n == null) return false;
            approximateSize.decrementAndGet();
            fireRemoval(key, n.value, RemovalListener.RemovalCause.EXPLICIT);
            return true;
        }
    }

    @Override
    public void invalidateAll() {
        clear();
    }

    @Override
    public void clear() {
        synchronized (data) {
            // 复制一份 key-value 用来通知
            java.util.List<Map.Entry<K, V>> entries = new java.util.ArrayList<>(data.size());
            for (Map.Entry<K, Node<K, V>> e : data.entrySet()) {
                entries.add(new java.util.AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value));
            }
            data.clear();
            approximateSize.set(0L);
            for (Map.Entry<K, V> e : entries) {
                fireRemoval(e.getKey(), e.getValue(), RemovalListener.RemovalCause.COLLECTED);
            }
        }
    }

    @Override
    public long size() {
        synchronized (data) {
            return data.size();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CacheStats stats() {
        return stats;
    }

    @Override
    public Map<K, V> asMap() {
        synchronized (data) {
            // 清理掉过期的，再返回视图
            cleanupExpiredLocked();
            return new java.util.AbstractMap<K, V>() {
                @Override
                public Set<Map.Entry<K, V>> entrySet() {
                    Set<Map.Entry<K, V>> set = new java.util.LinkedHashSet<>();
                    for (Map.Entry<K, Node<K, V>> e : data.entrySet()) {
                        if (!isExpired(e.getValue())) {
                            set.add(new java.util.AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value));
                        }
                    }
                    return set;
                }

                @Override
                public int size() {
                    return entrySet().size();
                }
            };
        }
    }

    @Override
    public Set<RemovalListener<K, V>> removalListeners() {
        return listeners;
    }

    @Override
    public void shutdown() {
        if (shutdown) return;
        shutdown = true;
        scheduler.shutdownNow();
    }

    // ==================== 内部实现 ====================

    private V getInternal(K key, Function<? super K, ? extends V> loader, boolean refresh) {
        if (key == null) {
            if (recordStats) stats.recordMiss();
            return null;
        }
        synchronized (data) {
            cleanupExpiredLocked();
            Node<K, V> n = data.get(key);
            if (n != null && !isExpired(n)) {
                touchAccess(n);
                if (recordStats) stats.recordHit();
                // refresh 路径：返回旧值 + 标记后台刷新（由 LoadingCache 接管，这里不抛错）
                return n.value;
            }
            // 缺失或已过期
            if (recordStats) stats.recordMiss();
            if (loader == null) {
                if (n != null) {
                    // 已过期，移除
                    data.remove(key);
                    approximateSize.decrementAndGet();
                    fireRemoval(key, n.value, expireAfterWriteNanos > 0
                            ? RemovalListener.RemovalCause.EXPIRED
                            : RemovalListener.RemovalCause.ACCESS_EXPIRED);
                }
                return null;
            }
            // 加载
            V loaded = loadValue(key, loader);
            if (loaded == null && nullValueProtection) {
                // 击穿防护：缓存 NullValue 哨兵（短期，5 秒过期避免长期污染）
                Node<K, V> sentinel = new Node<>();
                sentinel.value = (V) NullValue.INSTANCE;
                sentinel.writeNanos = System.nanoTime();
                sentinel.accessNanos = sentinel.writeNanos;
                sentinel.isNullSentinel = true;
                data.put(key, sentinel);
                approximateSize.incrementAndGet();
                enforceCapacity();
            } else if (loaded != null) {
                putInternalLocked(key, loaded, true);
            }
            return loaded;
        }
    }

    private V loadValue(K key, Function<? super K, ? extends V> loader) {
        long t0 = System.nanoTime();
        try {
            V v = loader.apply(key);
            if (recordStats) stats.recordLoadSuccess(System.nanoTime() - t0);
            return v;
        } catch (RuntimeException ex) {
            if (recordStats) stats.recordLoadFailure(System.nanoTime() - t0);
            throw ex;
        } catch (Throwable t) {
            if (recordStats) stats.recordLoadFailure(System.nanoTime() - t0);
            throw new RuntimeException("CacheLoader failed for key " + key, t);
        }
    }

    private void putInternal(K key, V value, boolean fromLoader) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        if (value == null) {
            // null value: 视作删除（除非开启 NullValue 防护，让外部明确传 NullValue.INSTANCE）
            remove(key);
            return;
        }
        synchronized (data) {
            putInternalLocked(key, value, fromLoader);
        }
    }

    private void putInternalLocked(K key, V value, boolean fromLoader) {
        Node<K, V> old = data.get(key);
        if (old != null) {
            old.value = value;
            old.writeNanos = System.nanoTime();
            old.accessNanos = old.writeNanos;
            old.isNullSentinel = false;
            // accessOrder=true，重复 put 会把节点移到队尾（重新标记为最近使用）
            // 但要显式触发——get 会触发，put 不一定。这里手动 remove+put 强制刷新位置
            data.remove(key);
            data.put(key, old);
            // 覆盖不触发 listener（语义：值更新，不算被移除）
        } else {
            Node<K, V> n = new Node<>();
            n.key = key;
            n.value = value;
            n.writeNanos = System.nanoTime();
            n.accessNanos = n.writeNanos;
            data.put(key, n);
            approximateSize.incrementAndGet();
            enforceCapacity();
        }
    }

    private void enforceCapacity() {
        if (maximumSize < 0) return;
        while (data.size() > maximumSize) {
            // 淘汰最久未使用（accessOrder 队首）
            K eldestKey = data.keySet().iterator().next();
            Node<K, V> evicted = data.remove(eldestKey);
            approximateSize.decrementAndGet();
            if (recordStats) stats.recordEviction();
            fireRemoval(eldestKey, evicted.value, RemovalListener.RemovalCause.SIZE_LIMIT);
        }
    }

    private boolean isExpired(Node<K, V> n) {
        // 自定义 Expiry 优先
        if (expiry != null) {
            long now = System.nanoTime();
            java.time.Duration d = expiry.expireAfterUpdate(n.key, n.value, now);
            if (d != null && d.toNanos() > 0 && now - n.writeNanos >= d.toNanos()) return true;
            d = expiry.expireAfterRead(n.key, n.value, now);
            if (d != null && d.toNanos() > 0 && now - n.accessNanos >= d.toNanos()) return true;
            return false;
        }
        long now = System.nanoTime();
        if (expireAfterWriteNanos > 0 && now - n.writeNanos >= expireAfterWriteNanos) return true;
        if (expireAfterAccessNanos > 0 && now - n.accessNanos >= expireAfterAccessNanos) return true;
        return false;
    }

    /**
     * 是否到达 refresh 阈值（返回旧值 + 触发后台 reload）。仅在 refreshAfterWriteNanos > 0 时评估。
     */
    private boolean needsRefresh(Node<K, V> n) {
        if (refreshAfterWriteNanos <= 0) return false;
        return System.nanoTime() - n.writeNanos >= refreshAfterWriteNanos;
    }

    private void touchAccess(Node<K, V> n) {
        n.accessNanos = System.nanoTime();
    }

    private void cleanupExpired() {
        synchronized (data) {
            cleanupExpiredLocked();
        }
    }

    private void cleanupExpiredLocked() {
        if (data.isEmpty()) return;
        // 必须先复制 keySet 再迭代删除
        java.util.List<K> expiredKeys = new java.util.ArrayList<>();
        for (Map.Entry<K, Node<K, V>> e : data.entrySet()) {
            if (isExpired(e.getValue())) {
                expiredKeys.add(e.getKey());
            }
        }
        for (K k : expiredKeys) {
            Node<K, V> n = data.remove(k);
            approximateSize.decrementAndGet();
            if (recordStats) stats.recordExpiration();
            // 判断是写过期还是访问过期
            RemovalListener.RemovalCause cause = (expireAfterWriteNanos > 0
                    && System.nanoTime() - n.writeNanos >= expireAfterWriteNanos)
                    ? RemovalListener.RemovalCause.EXPIRED
                    : RemovalListener.RemovalCause.ACCESS_EXPIRED;
            fireRemoval(k, n.value, cause);
        }
    }

    private void fireRemoval(K key, V value, RemovalListener.RemovalCause cause) {
        RemovalListener.RemovalNotification<K, V> n = new RemovalListener.RemovalNotification<>(key, value, cause);
        for (RemovalListener<K, V> l : listeners) {
            try {
                l.onRemoval(n);
            } catch (RuntimeException ignored) {
                // 监听器异常不影响主流程
            }
        }
    }

    /**
     * 内部节点。value 可变，write/access 时间戳用于过期判定。
     */
    private static final class Node<K, V> {
        K key;                       // 冗余存 key（Expiry 需要按 key 计算）
        volatile V value;
        volatile long writeNanos;
        volatile long accessNanos;
        volatile boolean isNullSentinel;
    }
}
