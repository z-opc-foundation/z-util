package com.zifang.util.validation.annotation;

import java.lang.annotation.*;

/**
 * 校验是否为空
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
/**
 * NotNull注解。
 */
public @interface NotNull {
    String message() default "不能为空";
}