package com.zifang.util.core.lang.exception;

/**
 * 未知异常类，继承自 RuntimeException。
 * <p>
 * 用于表示系统中未预期的异常情况，当无法确定具体异常类型时使用。
 * 通常作为异常捕获的最后一层保护，避免异常信息丢失。
 *
 * @author zifang
 */
public class UnknownException extends RuntimeException {

    private static final long serialVersionUID = -7655513487870988265L;

    /**
     * 使用指定错误信息创建未知异常。
     *
     * @param message 错误信息
     */
    public UnknownException(String message) {
        super(message);
    }

    /**
     * 使用指定错误信息和原始异常创建未知异常。
     *
     * @param message 错误信息
     * @param cause   原始异常，通常是导致当前异常的根本原因
     */
    public UnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个空的未知异常。
     */
    public UnknownException() {
        super();
    }

    /**
     * 使用原始异常创建未知异常。
     *
     * @param cause 原始异常
     */
    public UnknownException(Throwable cause) {
        super(cause);
    }

}
