package com.zifang.util.ioc.aop;

import com.zifang.util.aop.Advise;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AOP 绑定规则：类匹配器 + 方法匹配器 + 一组 advise（按数组顺序串行）。
 */
public class InterceptorBinding {

    private final ClassMatcher classMatcher;
    private final MethodMatcher methodMatcher;
    private final List<Class<? extends Advise>> advises;

    public InterceptorBinding(ClassMatcher classMatcher, MethodMatcher methodMatcher,
                              List<Class<? extends Advise>> advises) {
        this.classMatcher = classMatcher;
        this.methodMatcher = methodMatcher;
        this.advises = Collections.unmodifiableList(advises);
    }

    public InterceptorBinding(ClassMatcher classMatcher, List<Class<? extends Advise>> advises) {
        this(classMatcher, MethodMatcher.any(), advises);
    }

    @SafeVarargs
    public static InterceptorBinding of(ClassMatcher cm, Class<? extends Advise>... advises) {
        return new InterceptorBinding(cm, Arrays.asList(advises));
    }

    @SafeVarargs
    public static InterceptorBinding of(ClassMatcher cm, MethodMatcher mm, Class<? extends Advise>... advises) {
        return new InterceptorBinding(cm, mm, Arrays.asList(advises));
    }

    public ClassMatcher getClassMatcher() {
        return classMatcher;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    public List<Class<? extends Advise>> getAdvises() {
        return advises;
    }

    public boolean matchesClass(Class<?> clazz) {
        return classMatcher != null && classMatcher.matches(clazz);
    }

    public boolean matchesMethod(java.lang.reflect.Method method) {
        return methodMatcher == null || methodMatcher.matches(method);
    }
}
