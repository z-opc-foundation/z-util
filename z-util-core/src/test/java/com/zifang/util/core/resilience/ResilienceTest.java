package com.zifang.util.core.resilience;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 自研 resilience 能力测试。
 */
public class ResilienceTest {

    private TimeLimiter tl;

    @After
    public void teardown() {
        if (tl != null) tl.shutdown();
    }

    // ===== Retry =====

    @Test
    public void testRetry_succeedsEventually() {
        AtomicInteger calls = new AtomicInteger();
        String r = Retry.builder().maxAttempts(3).initialDelay(1, java.util.concurrent.TimeUnit.MILLISECONDS)
                .retryOn(RuntimeException.class)
                .build().call(() -> {
                    if (calls.incrementAndGet() < 3) throw new RuntimeException("not yet");
                    return "ok";
                });
        assertEquals("ok", r);
        assertEquals(3, calls.get());
    }

    @Test
    public void testRetry_exhausted() {
        AtomicInteger calls = new AtomicInteger();
        try {
            Retry.builder().maxAttempts(2).initialDelay(1, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .retryOn(RuntimeException.class)
                    .build().call(() -> {
                        calls.incrementAndGet();
                        throw new RuntimeException("always");
                    });
            fail();
        } catch (RetryExhaustedException expected) { /* ok */ }
        assertEquals(2, calls.get());
    }

    @Test
    public void testRetry_filterByException() {
        AtomicInteger calls = new AtomicInteger();
        try {
            Retry.builder().maxAttempts(3).initialDelay(1, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .retryOn(IllegalStateException.class)
                    .build().call(() -> {
                        calls.incrementAndGet();
                        throw new IllegalArgumentException("wrong type");
                    });
            fail();
        } catch (RetryExhaustedException expected) { /* ok */ }
        // 只跑 1 次（异常类型不匹配不重试）
        assertEquals(1, calls.get());
    }

    // ===== CircuitBreaker =====

    @Test
    public void testCircuitBreaker_opensAfterFailures() {
        CircuitBreaker cb = new CircuitBreaker(2, 1, 100);
        // 连续 2 次失败
        for (int i = 0; i < 2; i++) {
            try {
                cb.call(() -> {
                    throw new RuntimeException("boom");
                });
                fail();
            } catch (RuntimeException e) {
            }
        }
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        // 第 3 次直接被熔断
        try {
            cb.call(() -> "x");
            fail("expected open");
        } catch (CircuitOpenException expected) { /* ok */ }
    }

    @Test
    public void testCircuitBreaker_halfOpenThenClose() throws Exception {
        CircuitBreaker cb = new CircuitBreaker(1, 1, 50);
        // 触发 OPEN
        try {
            cb.call(() -> {
                throw new RuntimeException();
            });
        } catch (Exception ignored) {
        }
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        Thread.sleep(60);
        // 试探调用成功 → CLOSED
        String r = cb.call(() -> "ok");
        assertEquals("ok", r);
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    // ===== Bulkhead =====

    @Test
    public void testBulkhead_rejectsOverflow() throws Exception {
        Bulkhead bh = new Bulkhead(1);
        // 占用 1 个 slot
        Thread t = new Thread(() -> bh.call(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            return "x";
        }));
        t.start();
        Thread.sleep(50);
        // 第 2 个被拒
        try {
            bh.call(() -> "x");
            fail("expected");
        } catch (ResilienceException expected) { /* ok */ }
        t.join();
    }

    @Test
    public void testBulkhead_releasesAfterCall() {
        Bulkhead bh = new Bulkhead(1);
        bh.call(() -> "x");
        assertEquals(1, bh.availablePermits());
    }

    // ===== TimeLimiter =====

    @Test
    public void testTimeLimiter_timeout() {
        tl = new TimeLimiter(2, 1000);
        try {
            tl.call(() -> {
                Thread.sleep(200);
                return "late";
            }, 50);
            fail("expected timeout");
        } catch (ResilienceException e) {
            assertTrue(e.getMessage().contains("timeout"));
        }
    }

    @Test
    public void testTimeLimiter_ok() {
        tl = new TimeLimiter(2, 1000);
        assertEquals("fast", tl.call(() -> "fast", 100));
    }
}
