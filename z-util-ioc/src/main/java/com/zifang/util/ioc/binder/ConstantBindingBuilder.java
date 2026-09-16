package com.zifang.util.ioc.binder;

/**
 * 常量绑定构建器，对标 Guice 的 {@code ConstantBindingBuilder}。
 * <p>
 * 用于绑定 String / Integer / Long / Boolean / Class / Enum 等常量值，
 * 必须先 {@link #annotatedWith(Class)} 或 {@link #annotatedWith(Class, String)} 设置限定符，
 * 再 {@link #to(String)} / {@link #to(int)} 等设置值。
 */
public interface ConstantBindingBuilder {

    /**
     * 通过注解限定符（如 {@code Names.named("port")}）标记常量。
     */
    ConstantBindingBuilder annotatedWith(Class<? extends java.lang.annotation.Annotation> qualifier);

    /**
     * 通过注解限定符 + 自定义值（如 Guice Names.named 的字符串值）标记常量。
     */
    ConstantBindingBuilder annotatedWith(Class<? extends java.lang.annotation.Annotation> qualifierType, String qualifierValue);

    /**
     * 通过 Guice 风格的 {@code @Named("xxx")} 标记常量。
     */
    ConstantBindingBuilder annotatedWithNamed(String name);

    /**
     * 设置 String 值。
     */
    void to(String value);

    /**
     * 设置 int 值。
     */
    void to(int value);

    /**
     * 设置 long 值。
     */
    void to(long value);

    /**
     * 设置 boolean 值。
     */
    void to(boolean value);

    /**
     * 设置 double 值。
     */
    void to(double value);

    /**
     * 设置 Class 值。
     */
    void to(Class<?> value);

    /**
     * 设置 Enum 值。
     */
    <E extends Enum<E>> void to(E value);
}
