package com.zifang.util.validation.validator;

import com.zifang.util.validation.annotation.NotNull;
import com.zifang.util.validation.core.ValidateResult;
import com.zifang.util.validation.core.Validator;

import java.lang.reflect.Field;

/**
 * 非空校验器
 */
public class NotNullValidator implements Validator<NotNull> {

    @Override
    /**
     * validate方法。
     *      * @param target Object类型参数
     * @param field Field类型参数
     * @param annotation NotNull类型参数
     * @param result ValidateResult类型参数
     */
    public void validate(Object target, Field field, NotNull annotation, ValidateResult result) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(target);
        } catch (IllegalAccessException e) {
            result.addError(field.getName(), annotation.message());
            return;
        }

        if (value == null) {
            result.addError(field.getName(), annotation.message());
        }
    }
}