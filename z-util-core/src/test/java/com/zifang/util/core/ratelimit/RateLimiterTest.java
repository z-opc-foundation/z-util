package com.zifang.util.core.ratelimit;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 自研令牌桶 / 滑动窗口限流器测试。
 */
public class RateLimiterTest {

    @Test
    public void testTokenBucket_burstAllowed() {
        // 10 permits/秒，桶容量 5 → 启动后立即可用 5 个
        TokenBucketRateLimiter rl = new TokenBucketRateLimiter(10, 5);
        for (int i = 0; i < 5; i++) {
            assertTrue(rl.tryAcquire().isAllowed());
        }
        // 第 6 个必须等 → deny
        RateLimitDecision d = rl.tryAcquire();
        assertFalse(d.isAllowed());
        assertTrue("retry-after should be > 0, got " + d.getRetryAfterNanos(),
                d.getRetryAfterNanos() > 0);
    }

    @Test
    public void testTokenBucket_refillOverTime() throws InterruptedException {
        // 100 permits/秒，桶容量 1
        TokenBucketRateLimiter rl = new TokenBucketRateLimiter(100, 1);
        assertTrue(rl.tryAcquire().isAllowed());
        // 立即再取 → 应 deny
        assertFalse(rl.tryAcquire().isAllowed());
        // 等 50ms 至少能再取 5 个（100 * 0.05 = 5，但桶容量只有 1）
        Thread.sleep(50);
        assertTrue(rl.tryAcquire().isAllowed());
    }

    @Test
    public void testTokenBucket_acquireMany() throws InterruptedException {
        // 10 permits/s，桶容量 1：故意用低速率，确保 acquireMany 测试期间桶内令牌少
        TokenBucketRateLimiter rl = new TokenBucketRateLimiter(10, 1);
        // 等 200ms 让桶内历史令牌（如果残留）耗尽
        Thread.sleep(200);
        // 一次性取 1 个（桶容量 1）
        assertTrue(rl.tryAcquire(1).isAllowed());
        // 立即再取 1 → 不够（10/s = 100ms/个）
        assertFalse(rl.tryAcquire(1).isAllowed());
    }

    @Test
    public void testSlidingWindow() throws InterruptedException {
        SlidingWindowRateLimiter rl = new SlidingWindowRateLimiter(1, TimeUnit.SECONDS, 3);
        for (int i = 0; i < 3; i++) {
            assertTrue(rl.tryAcquire().isAllowed());
        }
        assertFalse(rl.tryAcquire().isAllowed());
        // 等 1.1s，窗口滑出 → 重新可取
        Thread.sleep(1100);
        assertTrue(rl.tryAcquire().isAllowed());
    }

    @Test
    public void testSlidingWindow_retryAfter() {
        SlidingWindowRateLimiter rl = new SlidingWindowRateLimiter(1, TimeUnit.SECONDS, 2);
        assertTrue(rl.tryAcquire().isAllowed());
        assertTrue(rl.tryAcquire().isAllowed());
        RateLimitDecision d = rl.tryAcquire();
        assertFalse(d.isAllowed());
        assertTrue("retry-after should be near window, got " + d.getRetryAfterNanos(),
                d.getRetryAfterNanos() > 0 && d.getRetryAfterNanos() <= TimeUnit.SECONDS.toNanos(1));
    }

    @Test
    public void testTokenBucket_invalidArgs() {
        try {
            new TokenBucketRateLimiter(0, 1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new TokenBucketRateLimiter(1, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testTokenBucket_concurrent() throws Exception {
        final TokenBucketRateLimiter rl = new TokenBucketRateLimiter(1000, 100);
        final java.util.concurrent.atomic.AtomicInteger allowed = new java.util.concurrent.atomic.AtomicInteger();
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    if (rl.tryAcquire().isAllowed()) allowed.incrementAndGet();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        // 20 线程 × 50 次 = 1000 请求；初始 100 个 + 后续补充不会太多；至少允许 ≥ 100
        assertTrue("expected at least burst allowed, got " + allowed.get(), allowed.get() >= 100);
    }
}
