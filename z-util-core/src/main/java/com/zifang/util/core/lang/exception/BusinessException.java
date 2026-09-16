package com.zifang.util.core.lang.exception;

import com.zifang.util.core.meta.StatusCode;

/**
 * 业务异常类，继承自 BaseException。
 * <p>
 * 用于在业务逻辑中抛出异常，表示业务处理失败或业务规则不满足的情况。
 * 业务异常通常需要被捕获并转换为用户友好的错误提示。
 *
 * @author zifang
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1646453246258984129L;

    /**
     * 使用指定的状态码和错误信息创建业务异常。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param msg        错误信息，将覆盖状态码中的默认信息
     */
    public BusinessException(StatusCode statusCode, String msg) {
        super(statusCode, msg);
    }

    /**
     * 使用指定的状态码和原始异常创建业务异常。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param e          原始异常，通常是导致当前异常的根本原因
     */
    public BusinessException(StatusCode statusCode, Throwable e) {
        super(statusCode, e);
    }

    /**
     * 使用指定的状态码创建业务异常。
     *
     * @param statusCode 状态码对象，不能为 null
     */
    public BusinessException(StatusCode statusCode) {
        super(statusCode);
    }

    /**
     * 使用指定的状态码和格式化参数创建业务异常。
     * <p>
     * 错误信息将通过格式化参数进行填充。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param params     格式化参数，用于填充错误信息中的占位符
     */
    public BusinessException(StatusCode statusCode, Object... params) {
        super(statusCode, params);
    }
}
