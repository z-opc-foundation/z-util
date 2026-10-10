package com.zifang.util.http.base.define;

import java.lang.annotation.*;

/**
 * 请求行信息
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RequestLine注解。
 */
public @interface RequestLine {
    String url();

    RequestMethod requestMethod();

    String protocol() default "http1.1";
}
