package com.zifang.util.ioc.annotation;

import java.lang.annotation.*;

/**
 * 标记配置类，内部可包含 {@link Bean} 工厂方法。
 * 相当于 Spring {@code @Configuration}。
 *
 * @see Bean
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {
}