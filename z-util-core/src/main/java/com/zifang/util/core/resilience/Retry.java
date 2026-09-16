package com.zifang.util.core.resilience;

import java.util.function.Predicate;

/**
 * 重试器（自研，对标 Spring Retry / Failsafe）。
 * <p>
 * 策略：
 * <ul>
 *   <li>最大尝试次数（首次 + 重试 N-1 次）</li>
 *   <li>固定 / 指数退避</li>
 *   <li>异常过滤：只对指定异常类型重试</li>
 * </ul>
 */
public class Retry {

    private final int maxAttempts;
    private final long initialDelayNanos;
    private final double multiplier;
    private final long maxDelayNanos;
    private final Predicate<Throwable> retryOn;

    private Retry(Builder b) {
        this.maxAttempts = b.maxAttempts;
        this.initialDelayNanos = b.initialDelayNanos;
        this.multiplier = b.multiplier;
        this.maxDelayNanos = b.maxDelayNanos;
        this.retryOn = b.retryOn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T call(java.util.concurrent.Callable<T> task) {
        Throwable last = null;
        long delay = initialDelayNanos;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return task.call();
            } catch (Throwable t) {
                last = t;
                if (attempt == maxAttempts) break;
                if (!retryOn.test(t)) break;
                try {
                    Thread.sleep(delay / 1_000_000L, (int) (delay % 1_000_000L));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                delay = Math.min((long) (delay * multiplier), maxDelayNanos);
            }
        }
        throw new RetryExhaustedException("retries exhausted (" + maxAttempts + " attempts)", last);
    }

    public static class Builder {
        private int maxAttempts = 3;
        private long initialDelayNanos = 100_000_000L;   // 100ms
        private double multiplier = 2.0;
        private long maxDelayNanos = 5_000_000_000L;     // 5s
        private Predicate<Throwable> retryOn = t -> t instanceof RuntimeException;

        public Builder maxAttempts(int n) {
            this.maxAttempts = n;
            return this;
        }

        public Builder initialDelay(long d, java.util.concurrent.TimeUnit u) {
            this.initialDelayNanos = u.toNanos(d);
            return this;
        }

        public Builder multiplier(double m) {
            this.multiplier = m;
            return this;
        }

        public Builder maxDelay(long d, java.util.concurrent.TimeUnit u) {
            this.maxDelayNanos = u.toNanos(d);
            return this;
        }

        public Builder retryOn(Predicate<Throwable> p) {
            this.retryOn = p;
            return this;
        }

        public Builder retryOn(Class<? extends Throwable>... classes) {
            this.retryOn = t -> {
                for (Class<? extends Throwable> c : classes) if (c.isInstance(t)) return true;
                return false;
            };
            return this;
        }

        public Retry build() {
            return new Retry(this);
        }
    }
}
