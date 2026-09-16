package com.zifang.util.core.lang.exception;

import com.zifang.util.core.meta.StatusCode;

/**
 * 不打印的业务异常类，继承自 BaseException。
 * <p>
 * 与 BusinessException 类似，用于表示业务处理失败的情况。
 * 区别在于：此类异常在统一异常处理时不会记录日志或打印异常信息，
 * 适用于某些不需要记录日志的业务异常场景。
 *
 * @author zifang
 */
public class NoPrintBusinessException extends BaseException {

    private static final long serialVersionUID = 2519740137508800641L;

    /**
     * 使用指定的状态码和原始异常创建不打印的业务异常。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param e          原始异常，通常是导致当前异常的根本原因
     */
    public NoPrintBusinessException(StatusCode statusCode, Throwable e) {
        super(statusCode, e);
    }

    /**
     * 使用指定的状态码创建不打印的业务异常。
     *
     * @param statusCode 状态码对象，不能为 null
     */
    public NoPrintBusinessException(StatusCode statusCode) {
        super(statusCode);
    }

    /**
     * 使用指定的状态码和格式化参数创建不打印的业务异常。
     * <p>
     * 错误信息将通过格式化参数进行填充。
     *
     * @param statusCode 状态码对象，不能为 null
     * @param params     格式化参数，用于填充错误信息中的占位符
     */
    public NoPrintBusinessException(StatusCode statusCode, Object... params) {
        super(statusCode, params);
    }

}
