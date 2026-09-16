package com.zifang.util.ioc.exception;

/**
 * Bean 创建 / 注入失败异常，对标 Guice 的 {@code com.google.inject.ProvisionException}。
 * <p>
 * 包装底层实例化、字段注入、构造器注入、{@code @PostConstruct} 回调等异常。
 */
public class ProvisionException extends RuntimeException {

    public ProvisionException(String message) {
        super(message);
    }

    public ProvisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
