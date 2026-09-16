package com.zifang.util.validation.annotation;

import java.lang.annotation.*;

/**
 * 校验邮箱格式
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
/**
 * Email注解。
 */
public @interface Email {
    String message() default "邮箱格式不正确";
}