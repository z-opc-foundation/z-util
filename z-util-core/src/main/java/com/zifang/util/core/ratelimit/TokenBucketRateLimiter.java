package com.zifang.util.core.ratelimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 自研令牌桶限流器（对标 Guava RateLimiter / Bucket4j 经典算法）。
 * <p>
 * 原理：以恒定速率往桶里放令牌（{@code permitsPerSecond}），桶容量 = {@code burstCapacity}。
 * 每次取 {@code n} 个令牌：取到则通过；取不到则拒绝并告知需等多久。
 * <p>
 * 线程安全：{@code synchronized} 内使用 {@link AtomicLong} 维护状态，无锁竞争。
 * <p>
 * 公平性：默认非公平；高并发下桶内令牌可能被饿死（即"先到先取"近似 FIFO）。
 */
public class TokenBucketRateLimiter implements RateLimiter {

    private final long capacity;
    private final long permitsPerSecond;
    private final long intervalNanos;     // 1 个令牌的间隔
    private final AtomicLong storedPermits = new AtomicLong(0);
    private final AtomicLong nextFreeNanos = new AtomicLong(0);
    /**
     * 全局时钟（无锁），用于跨线程同步。
     */
    private final AtomicLong nowNanos = new AtomicLong(System.nanoTime());

    public TokenBucketRateLimiter(long permitsPerSecond, long burstCapacity) {
        if (permitsPerSecond <= 0) throw new IllegalArgumentException("permitsPerSecond must be > 0");
        if (burstCapacity <= 0) throw new IllegalArgumentException("burstCapacity must be > 0");
        this.permitsPerSecond = permitsPerSecond;
        this.capacity = burstCapacity;
        this.intervalNanos = 1_000_000_000L / permitsPerSecond;
    }

    @Override
    public RateLimitDecision tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public RateLimitDecision tryAcquire(int permits) {
        if (permits <= 0) throw new IllegalArgumentException("permits must be > 0");
        synchronized (this) {
            long now = updateNow();
            // 1) 计算可发放的令牌（从 nextFree 到 now 之间累积的）
            long newPermits = (now - nextFreeNanos.get()) / intervalNanos;
            long stored = Math.min(capacity, storedPermits.get() + newPermits);
            long newNext = newPermits > 0 ? nextFreeNanos.get() + newPermits * intervalNanos : now;
            // 2) 尝试取
            if (stored >= permits) {
                stored -= permits;
                storedPermits.set(stored);
                nextFreeNanos.set(newNext);
                return RateLimitDecision.allow(stored);
            } else {
                // 不够：还要等 (permits - stored) 个 token
                long need = permits - stored;
                long waitNanos = need * intervalNanos;
                nextFreeNanos.set(newNext);
                return RateLimitDecision.deny(waitNanos);
            }
        }
    }

    private long updateNow() {
        // 始终单调递增
        long prev = nowNanos.get();
        long now = Math.max(System.nanoTime(), prev);
        nowNanos.set(now);
        return now;
    }
}
