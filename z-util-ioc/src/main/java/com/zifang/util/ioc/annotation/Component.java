package com.zifang.util.ioc.annotation;

import java.lang.annotation.*;

/**
 * 标记类为 IOC 容器管理的 Bean。
 * 名称默认取自类名首字母小写，也可在 value 中指定。
 *
 * <p>相当于 Spring {@code @Component}，但使用 JSR 330 / JSR 250 标准注解作为底层。
 *
 * @see javax.inject.Named
 * @see javax.inject.Singleton
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * Bean 名称，默认为空字符串（使用类名首字母小写）
     */
    String value() default "";

    /**
     * 作用域，默认空表示使用 {@link javax.inject.Singleton}
     */
    String scope() default "";
}