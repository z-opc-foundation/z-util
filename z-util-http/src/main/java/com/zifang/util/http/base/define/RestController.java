package com.zifang.util.http.base.define;


import java.lang.annotation.*;

/**
 * REST控制器注解
 * <p>
 * 用于标记一个类为REST控制器，类似于Spring MVC的@RestController。
 * 控制器类中的方法可以通过 {@link RequestMapping} 系列注解映射到具体的HTTP请求。
 * </p>
 *
 * @author zifang
 * @see RequestMapping
 * @see GetMapping
 * @see PostMapping
 * @see PutMapping
 * @see DeleteMapping
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RestController注解。
 */
public @interface RestController {

    /**
     * 控制器的根路径。
     *
     * @return 控制器路径
     */
    String value();
}
