package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成全参构造器：按字段声明顺序接收全部非静态字段。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZAllArgsConstructor {

    /** 构造器访问修饰符，默认 public。 */
    Access access() default Access.PUBLIC;

    /** 构造器访问级别。 */
    enum Access {
        PUBLIC, PROTECTED, PACKAGE, PRIVATE
    }
}
