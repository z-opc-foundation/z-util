package com.zifang.util.core.resilience;

/**
 * 重试耗尽。
 */
public class RetryExhaustedException extends ResilienceException {
    public RetryExhaustedException(String message) {
        super(message);
    }

    public RetryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
