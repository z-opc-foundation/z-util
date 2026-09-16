package com.zifang.util.core.meta;

import javax.servlet.http.HttpServletResponse;

/**
 * 响应状态码枚举
 * <p>
 * 定义了系统常用的响应状态码，遵循HTTP状态码规范。
 * </p>
 *
 * @see HttpServletResponse
 */
public enum ResultCode {

    /**
     * 业务处理失败
     */
    FAILURE(HttpServletResponse.SC_BAD_REQUEST, "Biz Exception"),

    /**
     * 操作成功
     */
    SUCCESS(HttpServletResponse.SC_OK, "Operation is Successful"),

    /**
     * 资源未找到
     */
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "404 Not Found"),

    /**
     * 请求被拒绝
     */
    REQ_REJECT(HttpServletResponse.SC_FORBIDDEN, "Request Rejected"),

    /**
     * 请求未授权
     */
    UN_AUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "Request Unauthorized"),

    /**
     * 缺少必需参数
     */
    PARAM_MISS(HttpServletResponse.SC_BAD_REQUEST, "Missing Required Parameter"),

    /**
     * 消息无法读取
     */
    MSG_NOT_READABLE(HttpServletResponse.SC_BAD_REQUEST, "Message Can't be Read"),

    /**
     * 参数类型不匹配
     */
    PARAM_TYPE_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Type Mismatch"),

    /**
     * 参数绑定错误
     */
    PARAM_BIND_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Binding Error"),

    /**
     * 参数校验错误
     */
    PARAM_VALID_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Validation Error"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_SUPPORTED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Supported"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"),

    /**
     * 媒体类型不支持
     */
    MEDIA_TYPE_NOT_SUPPORTED(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Media Type Not Supported");

    final Integer code;
    final String msg;

    /**
     * 构造方法
     *
     * @param code HTTP状态码
     * @param msg  状态描述信息
     */
    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取HTTP状态码
     *
     * @return HTTP状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取状态描述信息
     *
     * @return 状态描述
     */
    public String getMsg() {
        return msg;
    }
}
