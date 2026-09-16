package com.zifang.util.validation.validator;

import com.zifang.util.validation.annotation.Email;
import com.zifang.util.validation.core.ValidateResult;
import com.zifang.util.validation.core.Validator;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * 邮箱格式校验器
 */
public class EmailValidator implements Validator<Email> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @Override
    /**
     * validate方法。
     *      * @param target Object类型参数
     * @param field Field类型参数
     * @param annotation Email类型参数
     * @param result ValidateResult类型参数
     */
    public void validate(Object target, Field field, Email annotation, ValidateResult result) {
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

        if (!EMAIL_PATTERN.matcher(value.toString()).matches()) {
            result.addError(field.getName(), annotation.message());
        }
    }
}