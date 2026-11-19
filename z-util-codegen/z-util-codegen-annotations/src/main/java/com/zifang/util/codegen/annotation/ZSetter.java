package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成 setter。可标注在类上（作用于全部非静态字段）或单个字段上。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ZSetter {

    /** 显式指定 setter 名；为空时按 {@code setXxx} 推导。 */
    String value() default "";
}
