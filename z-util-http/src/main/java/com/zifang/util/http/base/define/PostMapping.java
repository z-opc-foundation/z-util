package com.zifang.util.http.base.define;

import java.lang.annotation.*;

/**
 * POST请求映射注解
 * <p>
 * 用于将HTTP POST请求映射到控制器的处理方法。是 {@link RequestMapping} 的快捷方式，
 * 默认为POST方法。
 * </p>
 *
 * @author zifang
 * @see RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.POST)
/**
 * PostMapping注解。
 */
public @interface PostMapping {

    /**
     * 请求的URL路径数组（支持多个路径）。
     *
     * @return URL路径数组
     */
    String[] values() default {};

    /**
     * 请求的URL路径。
     *
     * @return URL路径
     */
    String value() default "";
}
