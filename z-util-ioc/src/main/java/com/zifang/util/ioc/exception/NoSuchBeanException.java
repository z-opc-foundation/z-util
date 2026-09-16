package com.zifang.util.ioc.exception;

/**
 * Bean 未找到异常
 */
public class NoSuchBeanException extends RuntimeException {
    public NoSuchBeanException(String msg) {
        super(msg);
    }
}