package com.zifang.util.validation.annotation;

import java.lang.annotation.*;

/**
 * 校验数字大小范围
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
/**
 * Range注解。
 */
public @interface Range {
    double min() default Double.MIN_VALUE;

    double max() default Double.MAX_VALUE;

    String message() default "数值不在合法范围内";
}