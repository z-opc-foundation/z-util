package com.zifang.util.ioc.aop;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 类级别匹配器，对标 Guice 的 {@code Matcher<Class<?>>}。
 * <p>
 * 常用方法：
 * <ul>
 *   <li>{@link #any()} — 匹配所有类</li>
 *   <li>{@link #only(Class)} — 精确匹配</li>
 *   <li>{@link #subclassOf(Class)} — 匹配子类/实现类</li>
 *   <li>{@link #annotatedWith(Class)} — 匹配带指定注解的类</li>
 * </ul>
 */
@FunctionalInterface
public interface ClassMatcher {

    static ClassMatcher any() {
        return clazz -> true;
    }

    static ClassMatcher only(Class<?> exact) {
        Objects.requireNonNull(exact, "exact");
        return clazz -> clazz == exact;
    }

    static ClassMatcher subclassOf(Class<?> base) {
        Objects.requireNonNull(base, "base");
        // 包含 base 自身 + 所有子类
        return clazz -> base.isAssignableFrom(clazz);
    }

    static ClassMatcher annotatedWith(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "annotation");
        return clazz -> clazz.isAnnotationPresent(annotation);
    }

    boolean matches(Class<?> clazz);

    /**
     * 复合：与（AND）。
     */
    default ClassMatcher and(ClassMatcher other) {
        return clazz -> this.matches(clazz) && other.matches(clazz);
    }

    /**
     * 复合：或（OR）。
     */
    default ClassMatcher or(ClassMatcher other) {
        return clazz -> this.matches(clazz) || other.matches(clazz);
    }
}
