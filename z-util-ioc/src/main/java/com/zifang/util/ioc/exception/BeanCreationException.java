package com.zifang.util.ioc.exception;

/**
 * Bean 创建失败异常
 */
public class BeanCreationException extends RuntimeException {
    public BeanCreationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}