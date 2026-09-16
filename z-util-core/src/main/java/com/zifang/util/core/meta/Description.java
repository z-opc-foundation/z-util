package com.zifang.util.core.meta;

import java.lang.annotation.*;

/**
 * 描述注解，用于为字段、方法或类添加描述信息。
 * <p>
 * 可用于代码生成、文档输出、国际化等场景。
 *
 * @author zifang
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {
    String value();
}
