package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个不可变值类型的抽象定义（抽象类或接口）：编译期生成
 * {@code Immutable{SimpleName}} 实现——全 final 字段、全参构造、静态 {@code of} 工厂，
 * 外加 equals/hashCode/toString。
 *
 * <p>对标 AutoValue：属性取自无参抽象方法，{@code getX()} / {@code isX()} / record 风格
 * {@code x()} 三种命名都认。走标准源码生成通道，IDE 零插件可用。</p>
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZImmutable {
}
