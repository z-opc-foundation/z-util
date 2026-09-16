package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RetryTest {

    @Test
    public void testSucceedOnFirstAttempt() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retry.<String>builder().maxAttempts(3).build()
                .execute(() -> {
                    calls.incrementAndGet();
                    return "ok";
                });
        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    public void testRetryUntilSuccess() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retry.<String>builder()
                .maxAttempts(5)
                .backoff(new Retry.FixedBackoff(1))
                .build()
                .execute(() -> {
                    int n = calls.incrementAndGet();
                    if (n < 3) throw new RuntimeException("fail #" + n);
                    return "ok";
                });
        assertEquals("ok", result);
        assertEquals(3, calls.get());
    }

    @Test(expected = RuntimeException.class)
    public void testExhaustRetries() {
        AtomicInteger calls = new AtomicInteger();
        try {
            Retry.<String>builder()
                    .maxAttempts(3)
                    .backoff(new Retry.FixedBackoff(1))
                    .build()
                    .execute(() -> {
                        calls.incrementAndGet();
                        throw new RuntimeException("always fail");
                    });
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
    }

    @Test
    public void testRetryOnlyOnSpecifiedException() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retry.<String>builder()
                .maxAttempts(3)
                .backoff(new Retry.FixedBackoff(1))
                .retryOn(IllegalStateException.class)
                .build()
                .execute(() -> {
                    calls.incrementAndGet();
                    if (calls.get() == 1) throw new IllegalStateException("retry");
                    return "done";
                });
        assertEquals("done", result);
    }

    @Test
    public void testExponentialBackoff() {
        Retry.ExponentialBackoff eb = new Retry.ExponentialBackoff(100, 2.0, 5000);
        assertEquals(100, eb.delayMillis(1));
        assertEquals(200, eb.delayMillis(2));
        assertEquals(400, eb.delayMillis(3));
        assertEquals(800, eb.delayMillis(4));
    }

    @Test
    public void testExponentialBackoffMaxCap() {
        Retry.ExponentialBackoff eb = new Retry.ExponentialBackoff(100, 2.0, 500);
        // 2^10 * 100 = 102400, capped at 500
        assertEquals(500, eb.delayMillis(10));
    }
}