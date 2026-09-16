package com.zifang.util.http.base.define;


import java.lang.annotation.*;

/**
 * GET请求映射注解
 * <p>
 * 用于将HTTP GET请求映射到控制器的处理方法。是 {@link RequestMapping} 的快捷方式，
 * 默认为GET方法。
 * </p>
 *
 * @author zifang
 * @see RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.GET)
/**
 * GetMapping注解。
 */
public @interface GetMapping {

    /**
     * 请求的URL路径。
     *
     * @return URL路径
     */
    String value() default "";
}
