package com.zifang.util.core.resilience;

import com.zifang.util.core.ratelimit.RateLimiter;
import com.zifang.util.core.ratelimit.TokenBucketRateLimiter;
import com.zifang.util.core.resilience.*;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 端到端场景 2：抗雪崩组合（限流 + 熔断 + 舱壁 + 超时 + 重试）。
 * <p>
 * 模拟外部 RPC 调用：每个保护层独立可关，组合抗雪崩。
 */
public class ResilienceCompositionIT {

    @Test
    public void testRateLimiter_throttlesExcess() {
        RateLimiter rl = new TokenBucketRateLimiter(10, 1);
        AtomicInteger allowed = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            if (rl.tryAcquire().isAllowed()) allowed.incrementAndGet();
        }
        assertTrue("rate limit should throttle most calls, allowed=" + allowed.get(),
                allowed.get() < 5);
    }

    @Test
    public void testBulkhead_rejectsOverflow() throws Exception {
        Bulkhead bulkhead = new Bulkhead(2);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        Thread[] ts = new Thread[5];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> {
                try {
                    bulkhead.call(() -> {
                        int c = inside.incrementAndGet();
                        maxInside.updateAndGet(prev -> Math.max(prev, c));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        inside.decrementAndGet();
                        return "ok";
                    });
                } catch (ResilienceException e) {
                    rejected.incrementAndGet();
                }
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        assertEquals("max concurrent should be 2", 2, maxInside.get());
        assertTrue("some should be rejected, got " + rejected.get(), rejected.get() >= 2);
    }

    @Test
    public void testCircuitBreaker_opensAfterFailures() {
        FlakyRemoteService svc = new FlakyRemoteService(1, 1.0);
        CircuitBreaker cb = new CircuitBreaker(2, 1, 100);

        for (int i = 0; i < 2; i++) {
            try {
                cb.call(() -> svc.call("x"));
                fail();
            } catch (Exception ignored) {
            }
        }
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        int before = svc.totalCalls();
        try {
            cb.call(() -> svc.call("x"));
            fail("expected open");
        } catch (com.zifang.util.core.resilience.CircuitOpenException expected) { /* ok */ }
        assertEquals("circuit should short-circuit, no new remote calls",
                before, svc.totalCalls());
    }

    @Test
    public void testTimeLimiter_interruptsLongCall() {
        TimeLimiter tl = new TimeLimiter(2, 1000);
        try {
            tl.call(() -> {
                Thread.sleep(5000);
                return "late";
            }, 100);
            fail("expected timeout");
        } catch (ResilienceException e) {
            assertTrue("message should mention timeout, got: " + e.getMessage(),
                    e.getMessage().contains("timeout"));
        }
    }

    @Test
    public void testRetry_succeedsOn2ndAttempt() {
        FlakyRemoteService svc = new FlakyRemoteService(0, 0.0) {
            private final AtomicInteger callCount = new AtomicInteger();

            @Override
            public String call(String req) throws InterruptedException {
                int n = callCount.incrementAndGet();
                if (n < 2) throw new RuntimeException("transient " + n);
                return super.call(req);
            }
        };
        String r = Retry.builder()
                .maxAttempts(3)
                .initialDelay(5, TimeUnit.MILLISECONDS)
                .retryOn(RuntimeException.class)
                .build()
                .call(() -> svc.call("retry-me"));
        assertEquals("ok:retry-me", r);
    }

    @Test
    public void testFullStackRateLimitPlusBulkheadPlusBreaker() {
        RateLimiter rl = new TokenBucketRateLimiter(10, 5);
        Bulkhead bh = new Bulkhead(2);
        CircuitBreaker cb = new CircuitBreaker(2, 1, 200);

        FlakyRemoteService svc = new FlakyRemoteService(1, 0.0);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger throttled = new AtomicInteger();

        for (int i = 0; i < 50; i++) {
            if (!rl.tryAcquire().isAllowed()) {
                throttled.incrementAndGet();
                continue;
            }
            try {
                String r = cb.call(() -> bh.call(() -> svc.call("x")));
                success.incrementAndGet();
            } catch (Exception e) {
                throttled.incrementAndGet();
            }
        }
        assertTrue("should have some throttled, got " + throttled.get(), throttled.get() > 0);
        assertTrue("should have some success, got " + success.get(), success.get() > 0);
    }

    /**
     * 模拟外部服务：可控时延、偶发失败。
     */
    public static class FlakyRemoteService {
        private final AtomicInteger calls = new AtomicInteger();
        private final long latencyMs;
        private final double failureRate;

        public FlakyRemoteService(long latencyMs, double failureRate) {
            this.latencyMs = latencyMs;
            this.failureRate = failureRate;
        }

        public String call(String req) throws InterruptedException {
            int n = calls.incrementAndGet();
            Thread.sleep(latencyMs);
            if (Math.random() < failureRate) {
                throw new RuntimeException("simulated failure #" + n);
            }
            return "ok:" + req;
        }

        public int totalCalls() {
            return calls.get();
        }
    }
}
