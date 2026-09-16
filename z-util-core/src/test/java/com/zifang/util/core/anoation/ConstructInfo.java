package com.zifang.util.core.anoation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
@Documented
@Inherited
@interface ConstructInfo {
    String constructName();
}
