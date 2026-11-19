package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成必填参构造器：只接收 final 字段或标注了 {@link ZNonNull} 的字段。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZRequiredArgsConstructor {

    /** 构造器访问修饰符，默认 public。 */
    ZAllArgsConstructor.Access access() default ZAllArgsConstructor.Access.PUBLIC;
}
