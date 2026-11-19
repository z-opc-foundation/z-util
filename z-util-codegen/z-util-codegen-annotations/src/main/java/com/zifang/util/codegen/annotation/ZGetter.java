package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成 getter。可标注在类上（作用于全部非静态字段）或单个字段上。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ZGetter {

    /** 显式指定 getter 名；为空时按 {@code getXxx} / {@code isXxx} 推导。 */
    String value() default "";

    /** 布尔字段是否使用 {@code isXxx} 命名，默认 true。 */
    boolean booleanIsPrefix() default true;
}
