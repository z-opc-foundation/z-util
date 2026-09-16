package com.zifang.util.core.bean;

/**
 * 属性拷贝异常。
 */
public class BeanCopyException extends RuntimeException {
    public BeanCopyException(String message) {
        super(message);
    }

    public BeanCopyException(String message, Throwable cause) {
        super(message, cause);
    }
}
