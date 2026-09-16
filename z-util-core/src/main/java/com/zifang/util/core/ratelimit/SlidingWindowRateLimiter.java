package com.zifang.util.core.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * 自研滑动窗口限流器（对标 Bucket4j 的"sliding window" / Resilience4j 的 {@code TimeLimiter} 计数变体）。
 * <p>
 * 原理：维护一个 {@code windowSeconds} 长度的时间窗口，记录每次请求的时间戳。
 * 当窗口内请求数 ≥ {@code maxPermits}，拒绝；否则通过。
 * <p>
 * 实现：用 {@link Deque} 存最近请求时间戳，每次 {@code tryAcquire} 先弹出超出窗口的过期项，再判断大小。
 * <p>
 * 内存：每次成功占用 1 个 Long（时间戳）。窗口内请求越多占用越多，可考虑硬上限（默认 1e6）。
 */
public class SlidingWindowRateLimiter implements RateLimiter {

    private final long windowNanos;
    private final int maxPermits;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final int hardCap;

    public SlidingWindowRateLimiter(long window, TimeUnit unit, int maxPermits) {
        if (maxPermits <= 0) throw new IllegalArgumentException("maxPermits must be > 0");
        if (window <= 0) throw new IllegalArgumentException("window must be > 0");
        this.windowNanos = unit.toNanos(window);
        this.maxPermits = maxPermits;
        this.hardCap = 1_000_000;   // 防 OOM
    }

    @Override
    public RateLimitDecision tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public synchronized RateLimitDecision tryAcquire(int permits) {
        if (permits <= 0) throw new IllegalArgumentException("permits must be > 0");
        if (permits > maxPermits) return RateLimitDecision.deny(windowNanos);

        long now = System.nanoTime();
        long cutoff = now - windowNanos;
        // 弹出过期
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
            timestamps.pollFirst();
        }
        // 限硬上限
        if (timestamps.size() >= hardCap) {
            // 拒绝，防止 OOM
            return RateLimitDecision.deny(windowNanos);
        }
        if (timestamps.size() + permits > maxPermits) {
            // 计算 retry-after：等最老的时间戳出窗口
            long oldest = timestamps.peekFirst();
            long retry = (oldest + windowNanos) - now;
            return RateLimitDecision.deny(Math.max(retry, 0));
        }
        for (int i = 0; i < permits; i++) timestamps.addLast(now);
        return RateLimitDecision.allow(maxPermits - timestamps.size());
    }
}
