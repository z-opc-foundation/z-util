package com.zifang.util.http.base.define;


import java.lang.annotation.*;

/**
 * 请求映射注解
 * <p>
 * 用于将HTTP请求映射到控制器的处理方法。可指定请求的URL路径和HTTP方法。
 * </p>
 *
 * @author zifang
 * @see GetMapping
 * @see PostMapping
 * @see PutMapping
 * @see DeleteMapping
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RequestMapping注解。
 */
public @interface RequestMapping {

    /**
     * 请求的名称。
     *
     * @return 请求名称
     */
    String name() default "";

    /**
     * 请求的URL路径。
     *
     * @return URL路径
     */
    String value() default "";

    /**
     * 请求的HTTP方法。
     *
     * @return HTTP方法
     */
    RequestMethod method();

}
