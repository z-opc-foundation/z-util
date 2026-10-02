package com.zifang.util.validation.core;

/**
 * 校验异常
 */
public class ValidationException extends RuntimeException {

    private final ValidateResult result;

    /**
     * ValidationException方法。
     * * @param result ValidateResult类型参数
     */
    public ValidationException(ValidateResult result) {
        super("Validation failed: " + result.getErrors());
        this.result = result;
    }

    /**
     * getResult方法。
     *
     * @return ValidateResult类型返回值
     */
    public ValidateResult getResult() {
        return result;
    }
}