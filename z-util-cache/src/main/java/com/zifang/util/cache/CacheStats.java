package com.zifang.util.cache;

import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存运行统计：命中、未命中、淘汰、过期、加载耗时等。
 * <p>
 * 所有计数器采用 {@link LongAdder} 实现，在高并发下竞争更分散。
 */
public class CacheStats {

    private final LongAdder hitCount = new LongAdder();
    private final LongAdder missCount = new LongAdder();
    private final LongAdder loadSuccessCount = new LongAdder();
    private final LongAdder loadFailureCount = new LongAdder();
    private final LongAdder evictionCount = new LongAdder();
    private final LongAdder expirationCount = new LongAdder();
    private final LongAdder totalLoadTimeNanos = new LongAdder();

    public void recordHit() {
        hitCount.increment();
    }

    public void recordMiss() {
        missCount.increment();
    }

    public void recordLoadSuccess(long nanos) {
        loadSuccessCount.increment();
        totalLoadTimeNanos.add(nanos);
    }

    public void recordLoadFailure(long nanos) {
        loadFailureCount.increment();
        totalLoadTimeNanos.add(nanos);
    }

    public void recordEviction() {
        evictionCount.increment();
    }

    public void recordExpiration() {
        expirationCount.increment();
    }

    /**
     * 按移除原因分类计数。
     */
    public void recordRemoval(com.zifang.util.cache.RemovalListener.RemovalCause cause) {
        if (cause == null) return;
        switch (cause) {
            case SIZE_LIMIT:
            case COLLECTED:
                evictionCount.increment();
                break;
            case EXPIRED:
            case ACCESS_EXPIRED:
            case REPLACED:
                expirationCount.increment();
                break;
            case EXPLICIT:
            default:
                // 不单独计
                break;
        }
    }

    public long hitCount() {
        return hitCount.sum();
    }

    public long missCount() {
        return missCount.sum();
    }

    public long loadSuccessCount() {
        return loadSuccessCount.sum();
    }

    public long loadFailureCount() {
        return loadFailureCount.sum();
    }

    public long evictionCount() {
        return evictionCount.sum();
    }

    public long expirationCount() {
        return expirationCount.sum();
    }

    public long totalLoadTimeNanos() {
        return totalLoadTimeNanos.sum();
    }

    /**
     * 命中率（0-1）。无请求时返回 0。
     */
    public double hitRate() {
        long h = hitCount.sum();
        long m = missCount.sum();
        long t = h + m;
        return t == 0 ? 0.0 : (double) h / t;
    }

    /**
     * 平均加载耗时（纳秒）。无加载时返回 0。
     */
    public double averageLoadPenaltyNanos() {
        long n = loadSuccessCount.sum() + loadFailureCount.sum();
        return n == 0 ? 0.0 : (double) totalLoadTimeNanos.sum() / n;
    }

    @Override
    public String toString() {
        return "CacheStats{hit=" + hitCount() + ", miss=" + missCount()
                + ", hitRate=" + String.format("%.2f%%", hitRate() * 100)
                + ", loadSuccess=" + loadSuccessCount() + ", loadFailure=" + loadFailureCount()
                + ", eviction=" + evictionCount() + ", expiration=" + expirationCount()
                + ", avgLoadNanos=" + String.format("%.0f", averageLoadPenaltyNanos()) + "}";
    }
}
