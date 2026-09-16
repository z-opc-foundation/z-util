package com.zifang.util.core.lang;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * 重试工具（对标 Spring Retry / Resilience4j Retry）。
 * <p>
 * 用法：
 * <pre>{@code
 *   String result = Retry.<String>builder()
 *       .maxAttempts(3)
 *       .backoff(new ExponentialBackoff(100, 2.0, 5000))
 *       .retryOn(RuntimeException.class)
 *       .build()
 *       .execute(() -> callRemoteService());
 * }</pre>
 *
 * @author zifang
 */
public class Retry {

    private final int maxAttempts;
    private final Backoff backoff;
    private final Predicate<Throwable> retryPredicate;

    private Retry(Builder b) {
        this.maxAttempts = b.maxAttempts;
        this.backoff = b.backoff;
        this.retryPredicate = b.retryPredicate;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 执行带重试的操作。
     *
     * @param action 要执行的操作
     * @return action 的返回值
     * @throws Exception 最后一次失败的异常
     */
    public <T> T execute(ThrowingSupplier<T> action) throws Exception {
        Throwable lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Throwable t) {
                lastError = t;
                if (attempt == maxAttempts || !retryPredicate.test(t)) {
                    break;
                }
                long sleep = backoff.delayMillis(attempt);
                Thread.sleep(sleep);
            }
        }
        if (lastError instanceof Exception) {
            throw (Exception) lastError;
        }
        throw new RuntimeException(lastError);
    }

    /**
     * 函数式接口，可抛出异常。
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public interface Backoff {
        /**
         * 返回第 attempt 次失败后应等待的毫秒数（attempt 从 1 开始）。
         */
        long delayMillis(int attempt);
    }

    /**
     * 固定间隔。
     */
    public static class FixedBackoff implements Backoff {
        private final long baseMillis;

        public FixedBackoff(long baseMillis) {
            this.baseMillis = baseMillis;
        }

        @Override
        public long delayMillis(int attempt) {
            return baseMillis;
        }
    }

    /**
     * 指数退避（可选 jitter）。
     */
    public static class ExponentialBackoff implements Backoff {
        private final long baseMillis;
        private final double multiplier;
        private final long maxMillis;
        private final boolean jitter;

        public ExponentialBackoff(long baseMillis, double multiplier, long maxMillis) {
            this(baseMillis, multiplier, maxMillis, false);
        }

        public ExponentialBackoff(long baseMillis, double multiplier, long maxMillis, boolean jitter) {
            this.baseMillis = baseMillis;
            this.multiplier = multiplier;
            this.maxMillis = maxMillis;
            this.jitter = jitter;
        }

        @Override
        public long delayMillis(int attempt) {
            double v = baseMillis * Math.pow(multiplier, attempt - 1);
            long delay = (long) Math.min(v, maxMillis);
            if (jitter) {
                // 加性 jitter：[delay/2, delay]
                long low = delay / 2;
                delay = low + (ThreadLocalRandom.current().nextLong(0, delay - low + 1));
            }
            return delay;
        }
    }

    public static class Builder {
        private int maxAttempts = 3;
        private Backoff backoff = new FixedBackoff(100L);
        private Predicate<Throwable> retryPredicate = t -> true;

        public Builder maxAttempts(int n) {
            if (n < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
            this.maxAttempts = n;
            return this;
        }

        public Builder backoff(Backoff b) {
            this.backoff = b;
            return this;
        }

        public Builder retryOn(Class<? extends Throwable>... types) {
            this.retryPredicate = t -> {
                for (Class<? extends Throwable> c : types) {
                    if (c.isInstance(t)) return true;
                }
                return false;
            };
            return this;
        }

        public Builder retryIf(Predicate<Throwable> p) {
            this.retryPredicate = p;
            return this;
        }

        public Retry build() {
            return new Retry(this);
        }
    }
}