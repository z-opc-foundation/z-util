package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单个属性的转换规则，标注在 {@link ZMapper} 接口的方法上。
 *
 * @author zifang
 */
@Documented
@Repeatable(ZMappings.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ZMapping {

    /** 源属性名；为空时按目标属性同名匹配。 */
    String source() default "";

    /** 目标属性名；为空时按源属性同名匹配。 */
    String target() default "";

    /** 跳过该目标属性。 */
    boolean ignore() default false;

    /** 目标属性的取值表达式，形如 {@code java(trim(src.getName()))}；给了它就优先于属性直赋。 */
    String expression() default "";

    /** 目标属性的常量字面量（如 {@code "N/A"}、{@code 0}），给了它就优先于属性直赋。 */
    String constant() default "";

    /** 嵌套对象的转换方式，默认自动推断。 */
    Nesting nesting() default Nesting.AUTO;

    /** 嵌套转换方式。 */
    enum Nesting {
        /** 自动：源与目标类型相同则直赋，不同则找嵌套 mapper。 */
        AUTO,
        /** 强制直赋。 */
        FLAT,
        /** 强制走嵌套 mapper。 */
        NESTED
    }
}
