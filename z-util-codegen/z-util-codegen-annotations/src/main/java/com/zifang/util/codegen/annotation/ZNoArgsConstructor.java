package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成无参构造器。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZNoArgsConstructor {

    /** 构造器访问修饰符，默认 public。 */
    ZAllArgsConstructor.Access access() default ZAllArgsConstructor.Access.PUBLIC;

    /** 是否强制生成：类中已存在无参构造器时是否报错，默认 false。 */
    boolean force() default false;
}
