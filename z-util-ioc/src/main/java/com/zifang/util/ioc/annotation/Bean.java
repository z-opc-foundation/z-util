package com.zifang.util.ioc.annotation;

import java.lang.annotation.*;

/**
 * 标记配置类中的工厂方法，返回一个 Bean 实例。
 * 相当于 Spring {@code @Bean}。
 *
 * @see javax.inject.Named
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * Bean 名称，默认为方法名
     */
    String value() default "";
}