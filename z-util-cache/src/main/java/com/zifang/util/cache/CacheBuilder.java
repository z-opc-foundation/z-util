package com.zifang.util.cache;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 缓存构建器（对标 Guava CacheBuilder / Caffeine）。
 * <p>
 * 链式 API + 必填 name + 可选配置：
 * <pre>{@code
 *   Cache<String, User> cache = CacheBuilder.<String, User>newBuilder()
 *       .name("user-cache")
 *       .maximumSize(10_000)
 *       .expireAfterWrite(Duration.ofMinutes(5))
 *       .expireAfterAccess(Duration.ofMinutes(2))
 *       .recordStats()
 *       .addListener((n) -> log.info("evicted {}", n.getKey()))
 *       .build();
 *
 *   LoadingCache<String, User> loading = CacheBuilder.<String, User>newBuilder()
 *       .name("loading-user")
 *       .maximumSize(10_000)
 *       .build(loader);   // 传入 CacheLoader 得到 LoadingCache
 * }</pre>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class CacheBuilder<K, V> {

    private final Set<RemovalListener<K, V>> listeners = new HashSet<>();
    private String name = "cache-" + System.nanoTime();
    private long maximumSize = -1L;            // -1 表示无界
    private long expireAfterWriteNanos = -1L;  // -1 表示不过期
    private long expireAfterAccessNanos = -1L; // -1 表示不过期
    private long refreshAfterWriteNanos = -1L; // -1 表示不刷新
    private long initialCapacity = 16;
    private boolean recordStats = false;
    private boolean nullValueProtection = false;
    private Expiry<K, V> expiry = null;          // 自定义过期
    private Algorithm algorithm = Algorithm.LRU; // 淘汰算法（默认 LRU）

    private CacheBuilder() {
    }

    public static <K, V> CacheBuilder<K, V> newBuilder() {
        return new CacheBuilder<>();
    }

    public CacheBuilder<K, V> name(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
        this.name = name;
        return this;
    }

    public CacheBuilder<K, V> initialCapacity(int initialCapacity) {
        if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity must be > 0");
        this.initialCapacity = initialCapacity;
        return this;
    }

    /**
     * 设置最大条目数。超过时按 LRU 淘汰。{@code -1} 或不调用表示无界。
     */
    public CacheBuilder<K, V> maximumSize(long maximumSize) {
        if (maximumSize < 0 && maximumSize != -1) {
            throw new IllegalArgumentException("maximumSize must be >= 0 or -1 (unbounded)");
        }
        this.maximumSize = maximumSize;
        return this;
    }

    /**
     * 写入后存活时间。{@code Duration.ZERO} 或不调用表示不过期。
     */
    public CacheBuilder<K, V> expireAfterWrite(Duration duration) {
        this.expireAfterWriteNanos = duration == null ? -1L : duration.toNanos();
        return this;
    }

    /**
     * 最后一次访问后空闲时间（含 get 也会刷新）。{@code Duration.ZERO} 或不调用表示不过期。
     */
    public CacheBuilder<K, V> expireAfterAccess(Duration duration) {
        this.expireAfterAccessNanos = duration == null ? -1L : duration.toNanos();
        return this;
    }

    public CacheBuilder<K, V> recordStats() {
        this.recordStats = true;
        return this;
    }

    public CacheBuilder<K, V> addListener(RemovalListener<K, V> listener) {
        if (listener != null) this.listeners.add(listener);
        return this;
    }

    /**
     * 写入后多久"软过期"——访问时仍返回旧值，但异步触发 reload。
     * 注意：必须配合 {@code build(loader)} 一起用才有效（纯 {@code build()} 时与 expireAfterWrite 行为一致）。
     */
    public CacheBuilder<K, V> refreshAfterWrite(Duration duration) {
        this.refreshAfterWriteNanos = duration == null ? -1L : duration.toNanos();
        return this;
    }

    /**
     * 启用 NullValue 击穿防护：loader 返回 null 时缓存 {@link NullValue#INSTANCE}，避免
     * 恶意/不存在 key 反复穿透到下游。
     */
    public CacheBuilder<K, V> nullValueProtection() {
        this.nullValueProtection = true;
        return this;
    }

    /**
     * 自定义过期策略（与 expireAfterWrite/expireAfterAccess 互斥；同时设置时优先 Expiry）。
     */
    public CacheBuilder<K, V> expireAfter(Expiry<? super K, ? super V> expiry) {
        @SuppressWarnings("unchecked")
        Expiry<K, V> cast = (Expiry<K, V>) expiry;
        this.expiry = cast;
        return this;
    }

    /**
     * 缓存淘汰算法选择（默认 {@link Algorithm#LRU}）。
     * <ul>
     *   <li>LRU：标准最近最少使用（{@link MemoryCache}）</li>
     *   <li>WTINYLFU：自研 W-TinyLFU（Window + Main LRU + 4-bit Count-Min Sketch），
     *       抗扫描（scan resistant）、高命中率。容量 ≤ 1 时回退到 LRU。</li>
     * </ul>
     */
    public CacheBuilder<K, V> algorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * 构建一个普通 {@link Cache}。
     */
    public Cache<K, V> build() {
        if (algorithm == Algorithm.WTINYLFU) {
            return new WTinyLfuCache<>(this);
        }
        return new MemoryCache<>(this);
    }

    /**
     * 构建一个 {@link LoadingCache}，loader 在 key 缺失时被调用。
     */
    public LoadingCache<K, V> build(CacheLoader<K, V> loader) {
        if (loader == null) throw new NullPointerException("loader must not be null");
        // LoadingCache 暂不接入 W-TinyLFU
        return new LoadingMemoryCache<>(this, loader);
    }

    String getName() {
        return name;
    }

    // ==================== 包内可见的访问器（供 MemoryCache 使用） ====================

    long getMaximumSize() {
        return maximumSize;
    }

    long getExpireAfterWriteNanos() {
        return expireAfterWriteNanos;
    }

    long getExpireAfterAccessNanos() {
        return expireAfterAccessNanos;
    }

    long getRefreshAfterWriteNanos() {
        return refreshAfterWriteNanos;
    }

    long getInitialCapacity() {
        return initialCapacity;
    }

    boolean isRecordStats() {
        return recordStats;
    }

    boolean isNullValueProtection() {
        return nullValueProtection;
    }

    Expiry<K, V> getExpiry() {
        return expiry;
    }

    Set<RemovalListener<K, V>> getListeners() {
        return listeners;
    }

    /**
     * 缓存淘汰算法枚举。
     */
    public enum Algorithm {LRU, WTINYLFU}
}
