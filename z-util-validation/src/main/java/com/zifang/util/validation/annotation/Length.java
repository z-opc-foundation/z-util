package com.zifang.util.validation.annotation;

import java.lang.annotation.*;

/**
 * 校验字符串长度范围
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
/**
 * Length注解。
 */
public @interface Length {
    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "长度不在合法范围内";
}