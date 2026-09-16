package com.zifang.util.core.meta;

/**
 * 基础的状态码定义
 */
public enum BaseStatusCode implements StatusCode {
    OK(200, "success"),
    UN_LOGIN(401, "未登录"),
    TOO_MANY_REQUESTS(429, "太多请求"),
    PARAM_VALID_EXCEPTION(600, "参数校验异常"),
    PARAM_BIND_EXCEPTION(601, "参数绑定异常"),
    PARAM_PARSE_EXCEPTION(602, "参数解析异常"),
    UNKNOWN_ERROR(500, "未知异常"),
    FAIL(510, "处理失败");

    private final int code;
    private final String message;

    BaseStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    /**
     * getCode方法。
     * @return int类型返回值
     */
    public int getCode() {
        return code;
    }

    @Override
    /**
     * getMessage方法。
     * @return String类型返回值
     */
    public String getMessage() {
        return message;
    }
}
