package com.zifang.util.ioc.exception;

/**
 * 绑定配置错误异常。
 * <p>
 * 用于：在 Module 中重复绑定同一键、为接口绑定非子类的实现、绑定到自身等场景。
 */
public class BindingException extends RuntimeException {

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
