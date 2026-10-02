package com.zifang.util.validation.annotation;

import java.lang.annotation.*;

/**
 * 校验正则表达式
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
/**
 * Pattern注解。
 */
public @interface Pattern {
    String regex();

    String message() default "格式不匹配";
}