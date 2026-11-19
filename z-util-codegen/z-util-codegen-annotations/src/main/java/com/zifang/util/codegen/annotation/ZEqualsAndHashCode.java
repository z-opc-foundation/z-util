package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成 {@code equals(Object)} 与 {@code hashCode()}。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZEqualsAndHashCode {

    /** 是否包含非静态字段，默认包含。 */
    boolean includeFieldNames() default true;

    /** 是否只比较参与比较的字段，默认 false（全部参与）。 */
    boolean onlyExplicitlyIncluded() default false;
}
