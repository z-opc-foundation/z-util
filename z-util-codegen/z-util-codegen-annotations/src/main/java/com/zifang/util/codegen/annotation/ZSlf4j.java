package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 SLF4J logger：生成 {@code private static final org.slf4j.Logger log}，
 * 初始化为 {@code LoggerFactory.getLogger(目标类.class)}。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZSlf4j {

    /** logger 字段名，默认 {@code log}。 */
    String fieldName() default "log";

    /** logger 类型全限定名，默认 {@code org.slf4j.Logger}。 */
    String loggerType() default "org.slf4j.Logger";

    /** logger 工厂全限定名，默认 {@code org.slf4j.LoggerFactory}。 */
    String factoryType() default "org.slf4j.LoggerFactory";
}
