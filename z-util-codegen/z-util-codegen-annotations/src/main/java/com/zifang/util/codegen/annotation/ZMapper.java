package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个转换接口：编译期为它生成实现类 {@code {InterfaceName}Impl}。
 *
 * <p>接口方法形如 {@code Target convert(Source source)}，引擎按属性名匹配生成赋值语句；
 * 需要定制时用 {@link ZMapping} 逐项覆盖。</p>
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZMapper {

    /** 实现类名；为空时取 {@code {InterfaceName}Impl}。 */
    String implClassName() default "";

    /** 未被映射的目标属性时的策略，默认忽略。 */
    UnmappedTargetPolicy unmappedTargetPolicy() default UnmappedTargetPolicy.IGNORE;

    /** 未被映射的目标属性时的策略。 */
    enum UnmappedTargetPolicy {
        /** 报错：编译失败。 */
        ERROR,
        /** 警告：编译通过但打印警告。 */
        WARN,
        /** 静默忽略。 */
        IGNORE
    }
}
