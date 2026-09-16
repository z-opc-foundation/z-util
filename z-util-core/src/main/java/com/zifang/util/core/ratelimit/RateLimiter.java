package com.zifang.util.core.ratelimit;

/**
 * 限流器接口。
 */
public interface RateLimiter {

    /**
     * 尝试获取 1 个许可。非阻塞。
     */
    RateLimitDecision tryAcquire();

    /**
     * 尝试获取 n 个许可。非阻塞。
     */
    RateLimitDecision tryAcquire(int permits);
}
