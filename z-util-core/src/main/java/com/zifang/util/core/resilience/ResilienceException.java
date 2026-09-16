package com.zifang.util.core.resilience;

/**
 * 弹性能力基类异常。
 */
public class ResilienceException extends RuntimeException {
    public ResilienceException(String message) {
        super(message);
    }

    public ResilienceException(String message, Throwable cause) {
        super(message, cause);
    }
}
