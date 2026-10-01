package com.zifang.util.core.pattern.cache;

/**
 * Exception thrown by cache operations.
 */
public class CacheException extends RuntimeException {

    /**
     * CacheException方法。
     * * @param message String类型参数
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * CacheException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}