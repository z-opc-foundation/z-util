package com.zifang.util.http.base.define;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RequestParam注解。
 */
public @interface RequestParam {

    String value() default "";

    boolean required() default true;

    String defaultValue() default "";

}
