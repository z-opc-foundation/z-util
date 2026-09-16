package com.zifang.util.ioc.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 方法级别匹配器，对标 Guice 的 {@code Matcher<Method>}。
 * <p>
 * 常用方法：
 * <ul>
 *   <li>{@link #any()} — 匹配所有方法</li>
 *   <li>{@link #annotatedWith(Class)} — 匹配带指定注解的方法</li>
 *   <li>{@link #named(String)} — 按方法名匹配</li>
 *   <li>{@link #nameStartsWith(String)} — 按方法名前缀匹配</li>
 * </ul>
 */
@FunctionalInterface
public interface MethodMatcher {

    static MethodMatcher any() {
        return method -> true;
    }

    static MethodMatcher annotatedWith(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "annotation");
        return method -> method.isAnnotationPresent(annotation);
    }

    static MethodMatcher named(String name) {
        Objects.requireNonNull(name, "name");
        return method -> method.getName().equals(name);
    }

    static MethodMatcher nameStartsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        return method -> method.getName().startsWith(prefix);
    }

    boolean matches(Method method);

    default MethodMatcher and(MethodMatcher other) {
        return method -> this.matches(method) && other.matches(method);
    }

    default MethodMatcher or(MethodMatcher other) {
        return method -> this.matches(method) || other.matches(method);
    }
}
