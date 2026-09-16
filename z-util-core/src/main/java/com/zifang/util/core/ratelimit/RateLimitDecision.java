package com.zifang.util.core.ratelimit;

/**
 * 限流结果。
 */
public final class RateLimitDecision {

    private final boolean allowed;
    private final long retryAfterNanos;
    private final long remaining;

    public RateLimitDecision(boolean allowed, long retryAfterNanos, long remaining) {
        this.allowed = allowed;
        this.retryAfterNanos = retryAfterNanos;
        this.remaining = remaining;
    }

    public static RateLimitDecision allow(long remaining) {
        return new RateLimitDecision(true, 0L, remaining);
    }

    public static RateLimitDecision deny(long retryAfterNanos) {
        return new RateLimitDecision(false, retryAfterNanos, 0L);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRetryAfterNanos() {
        return retryAfterNanos;
    }

    public long getRemaining() {
        return remaining;
    }
}
