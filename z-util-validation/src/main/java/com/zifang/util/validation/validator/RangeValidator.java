package com.zifang.util.validation.validator;

import com.zifang.util.validation.annotation.Range;
import com.zifang.util.validation.core.ValidateResult;
import com.zifang.util.validation.core.Validator;

import java.lang.reflect.Field;

/**
 * 数值范围校验器
 */
public class RangeValidator implements Validator<Range> {

    @Override
    /**
     * validate方法。
     *      * @param target Object类型参数
     * @param field Field类型参数
     * @param annotation Range类型参数
     * @param result ValidateResult类型参数
     */
    public void validate(Object target, Field field, Range annotation, ValidateResult result) {
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

        double num;
        if (value instanceof Number) {
            num = ((Number) value).doubleValue();
        } else {
            try {
                num = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                result.addError(field.getName(), "无法转换为数字");
                return;
            }
        }

        if (num < annotation.min() || num > annotation.max()) {
            result.addError(field.getName(), annotation.message());
        }
    }
}