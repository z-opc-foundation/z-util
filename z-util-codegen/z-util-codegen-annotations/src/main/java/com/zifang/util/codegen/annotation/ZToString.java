package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成 {@code toString()}。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZToString {

    /** toString 中是否包含非静态字段，默认包含。 */
    boolean includeFieldNames() default true;

    /** 是否包含 {@code super.toString()}，默认不包含。 */
    boolean callSuper() default false;
}
