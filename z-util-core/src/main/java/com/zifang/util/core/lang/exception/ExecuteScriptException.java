package com.zifang.util.core.lang.exception;


/**
 * 脚本执行异常类，继承自 RuntimeException。
 * <p>
 * 用于表示脚本（如 JavaScript、Python、Shell 等）执行过程中发生的异常。
 * 当脚本解释器返回错误或脚本执行超时、出错时抛出此异常。
 *
 * @author zifang
 */
public class ExecuteScriptException extends RuntimeException {

    private static final long serialVersionUID = -2590455067812497134L;

    /**
     * 使用指定错误信息创建脚本执行异常。
     *
     * @param message 错误信息，描述脚本执行失败的原因
     */
    public ExecuteScriptException(String message) {
        super(message);
    }

    /**
     * 使用指定错误信息和原始异常创建脚本执行异常。
     *
     * @param message 错误信息，描述脚本执行失败的原因
     * @param cause   原始异常，通常是导致当前异常的根本原因
     */
    public ExecuteScriptException(String message, Throwable cause) {
        super(message, cause);
    }

}
