package com.zifang.util.core.anoation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface MethodInfo {
    String author() default "Pankaj";

    String date();

    int revision() default 1;

    String comments();
}
