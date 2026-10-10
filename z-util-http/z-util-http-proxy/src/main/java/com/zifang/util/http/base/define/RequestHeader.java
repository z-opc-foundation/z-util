package com.zifang.util.http.base.define;

import java.lang.annotation.*;

/**
 * 请求行信息
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RequestHeader注解。
 */
public @interface RequestHeader {
    String key();

    String value();
}
