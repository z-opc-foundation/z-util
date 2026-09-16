package com.zifang.util.core.anoation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
@Inherited
@interface FieldInfo {
    String name();

    String password();

    String comments() default "this is default parts";
}
