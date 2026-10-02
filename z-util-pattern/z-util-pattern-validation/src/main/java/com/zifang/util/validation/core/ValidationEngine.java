package com.zifang.util.validation.core;

import com.zifang.util.validation.annotation.*;
import com.zifang.util.validation.validator.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验引擎 - 核心校验器
 */
public class ValidationEngine {

    private static final Map<Class<? extends Annotation>, Validator<? extends Annotation>> VALIDATORS = new HashMap<>();

    static {
        VALIDATORS.put(NotNull.class, new NotNullValidator());
        VALIDATORS.put(Length.class, new LengthValidator());
        VALIDATORS.put(Range.class, new RangeValidator());
        VALIDATORS.put(Email.class, new EmailValidator());
        VALIDATORS.put(Pattern.class, new PatternValidator());
    }

    /**
     * 注册自定义校验器
     */
    public static <T extends Annotation> void registerValidator(Class<T> annotationClass, Validator<T> validator) {
        VALIDATORS.put(annotationClass, validator);
    }

    /**
     * 校验对象
     */
    public static ValidateResult validate(Object target) {
        ValidateResult result = new ValidateResult();
        if (target == null) {
            result.addError("object", "对象不能为空");
            return result;
        }

        Class<?> clazz = target.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                @SuppressWarnings("unchecked")
                Validator<Annotation> validator = (Validator<Annotation>) VALIDATORS.get(annotation.annotationType());
                if (validator != null) {
                    validator.validate(target, field, annotation, result);
                }
            }
        }

        return result;
    }

    /**
     * 快速校验 - 如果有错误则抛出异常
     */
    public static void validateAndThrow(Object target) {
        ValidateResult result = validate(target);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * 校验对象指定字段
     */
    public static ValidateResult validateField(Object target, String fieldName) {
        ValidateResult result = new ValidateResult();
        if (target == null) {
            result.addError("object", "对象不能为空");
            return result;
        }

        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            for (Annotation annotation : field.getAnnotations()) {
                @SuppressWarnings("unchecked")
                Validator<Annotation> validator = (Validator<Annotation>) VALIDATORS.get(annotation.annotationType());
                if (validator != null) {
                    validator.validate(target, field, annotation, result);
                }
            }
        } catch (NoSuchFieldException e) {
            result.addError(fieldName, "字段不存在");
        }

        return result;
    }
}