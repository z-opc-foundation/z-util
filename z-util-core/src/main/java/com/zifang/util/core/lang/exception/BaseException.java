package com.zifang.util.core.lang.exception;

import com.zifang.util.core.meta.Result;
import com.zifang.util.core.meta.StatusCode;

import static com.zifang.util.core.meta.Result.buildMessage;

/**
 * 基础异常类，继承自 RuntimeException，提供状态码和错误信息功能。
 * <p>
 * 该异常用于统一管理系统中的异常状态，包含错误码和错误信息。
 * 支持通过 StatusCode 或 Result 对象创建异常实例。
 *
 * @author zifang
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 2059913032332171665L;

    /**
     * 状态码，包含错误码和错误信息
     */
    private final StatusCode statusCode;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 使用指定的状态码创建异常实例。
     *
     * @param statusCode 状态码对象，不能为 null
     */
    public BaseException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.message = statusCode.getMessage();
    }

    /**
     * 使用指定的状态码和格式化参数创建异常实例。
     * <p>
     * 错误信息将通过 {@link Result#buildMessage(String, Object...)} 进行格式化。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param params     格式化参数，用于填充错误信息中的占位符
     */
    public BaseException(StatusCode statusCode, Object... params) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        String baseMessage = statusCode.getMessage();
        this.message = buildMessage(baseMessage, params);
    }

    /**
     * 使用指定的状态码和原始异常创建异常实例。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param e          原始异常，通常是导致当前异常的根本原因
     */
    public BaseException(StatusCode statusCode, Throwable e) {
        super(statusCode.getMessage(), e);
        this.statusCode = statusCode;
        this.message = statusCode.getMessage();
    }

    /**
     * 使用指定的 Result 对象和格式化参数创建异常实例。
     *
     * @param result Result 对象，包含错误码和错误信息
     * @param params 格式化参数，用于填充错误信息中的占位符
     */
    public BaseException(Result<?> result, Object... params) {
        this(new StatusCode() {
            @Override
            /**
             * getCode方法。
             * @return int类型返回值
             */
            public int getCode() {
                return result.getCode();
            }

            @Override
            /**
             * getMessage方法。
             * @return String类型返回值
             */
            public String getMessage() {
                return result.getMessage();
            }
        }, params);
    }

    /**
     * 使用指定的 Result 对象和原始异常创建异常实例。
     *
     * @param result Result 对象，包含错误码和错误信息
     * @param e      原始异常，通常是导致当前异常的根本原因
     */
    public BaseException(Result<?> result, Throwable e) {
        this(new StatusCode() {
            @Override
            /**
             * getCode方法。
             * @return int类型返回值
             */
            public int getCode() {
                return result.getCode();
            }

            @Override
            /**
             * getMessage方法。
             * @return String类型返回值
             */
            public String getMessage() {
                return result.getMessage();
            }
        }, e);
    }

    /**
     * 使用指定的 Result 对象创建异常实例。
     *
     * @param result Result 对象，包含错误码和错误信息
     */
    public BaseException(Result<?> result) {
        this(new StatusCode() {
            @Override
            /**
             * getCode方法。
             * @return int类型返回值
             */
            public int getCode() {
                return result.getCode();
            }

            @Override
            /**
             * getMessage方法。
             * @return String类型返回值
             */
            public String getMessage() {
                return result.getMessage();
            }
        });
    }

    /**
     * 获取状态码对象。
     *
     * @return 状态码对象，包含错误码和错误信息
     */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码数字
     */
    public int getCode() {
        return statusCode.getCode();
    }

    /**
     * 获取错误信息。
     * <p>
     * 该方法返回格式化后的错误信息，与原始状态码中的信息可能不同
     * （当通过带参数的构造方法创建时）。
     *
     * @return 错误信息字符串
     */
    @Override
    /**
     * getMessage方法。
     * @return String类型返回值
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * 返回异常的字符串表示形式。
     *
     * @return 包含状态码、错误码和错误信息的字符串
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return String.format("BaseException[status:%s(%s),message:%s]", statusCode, statusCode.getCode(), message);
    }
}
