package com.zifang.util.zex.bust.chapter9;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.lang.annotation.*;


@Target({ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE_USE,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
/**
 * AnnotationTest注解。
 */
public @interface AnnotationTest {
    String value() default "";
}