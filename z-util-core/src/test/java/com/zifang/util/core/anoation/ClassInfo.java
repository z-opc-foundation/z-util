package com.zifang.util.core.anoation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
@SuperInfo(superInfo = "superInfo")
/**
 * ClassInfo注解。
 */
public @interface ClassInfo {
    String className();

    String value() default "default-class-value";
}
