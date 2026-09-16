package com.zifang.util.core.lock;

/**
 * 锁异常。
 */
public class LockException extends RuntimeException {
    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
