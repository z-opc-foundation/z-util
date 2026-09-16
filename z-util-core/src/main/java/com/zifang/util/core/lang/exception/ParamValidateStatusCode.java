package com.zifang.util.core.lang.exception;


import com.zifang.util.core.meta.StatusCode;

/**
 * 接口参数异常错误码定义
 */
public enum ParamValidateStatusCode implements StatusCode {

    PARAMETER_ERROR(1, "{}");

    private final int code;
    private final String message;

    ParamValidateStatusCode(int code, String message) {
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
