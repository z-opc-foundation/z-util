package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成 builder：产出与目标类同包的 {@code {ClassName}Builder}，并在目标类上注入
 * {@code static {ClassName}Builder builder()} 静态工厂。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ZBuilder {

    /** builder 类名；为空时取 {@code {ClassName}Builder}。 */
    String builderClassName() default "";

    /** 目标类上注入的静态工厂方法名，默认 {@code builder}。 */
    String builderMethodName() default "builder";

    /** builder 类的访问修饰符，默认 public。 */
    ZAllArgsConstructor.Access access() default ZAllArgsConstructor.Access.PUBLIC;
}
