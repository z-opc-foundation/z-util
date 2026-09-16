package com.zifang.util.http.base.define;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * PUT请求映射注解
 * <p>
 * 用于将HTTP PUT请求映射到控制器的处理方法。
 * </p>
 *
 * @author zifang
 */
@Retention(RetentionPolicy.RUNTIME)
/**
 * PutMapping注解。
 */
public @interface PutMapping {

    /**
     * 请求的URL路径。
     *
     * @return URL路径
     */
    String value();
}
