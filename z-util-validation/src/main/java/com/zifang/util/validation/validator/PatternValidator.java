package com.zifang.util.validation.validator;

import com.zifang.util.validation.annotation.Pattern;
import com.zifang.util.validation.core.ValidateResult;
import com.zifang.util.validation.core.Validator;

import java.lang.reflect.Field;

/**
 * 正则表达式校验器
 */
public class PatternValidator implements Validator<Pattern> {

    @Override
    /**
     * validate方法。
     *      * @param target Object类型参数
     * @param field Field类型参数
     * @param annotation Pattern类型参数
     * @param result ValidateResult类型参数
     */
    public void validate(Object target, Field field, Pattern annotation, ValidateResult result) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(target);
        } catch (IllegalAccessException e) {
            result.addError(field.getName(), annotation.message());
            return;
        }

        if (value == null) {
            return; // null值由NotNull校验
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(annotation.regex());
        if (!pattern.matcher(value.toString()).matches()) {
            result.addError(field.getName(), annotation.message());
        }
    }
}