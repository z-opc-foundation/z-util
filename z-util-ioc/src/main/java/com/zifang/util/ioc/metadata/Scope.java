package com.zifang.util.ioc.metadata;

import javax.inject.Named;
import java.lang.annotation.Annotation;

/**
 * Bean 作用域，对标 Guice 的 {@code Scope}。
 * <p>
 * 本身只是标记（identifier），实际作用域语义由 {@link com.zifang.util.ioc.inject.Instantiator} 解释。
 *
 * <h3>内置作用域：</h3>
 * <ul>
 *   <li>{@link #SINGLETON} — 容器内单例（默认）</li>
 *   <li>{@link #PROTOTYPE} — 每次请求新建实例</li>
 * </ul>
 */
public final class Scope {

    public static final Scope SINGLETON = new Scope();
    public static final Scope PROTOTYPE = new Scope();
    public static final Scope DEFAULT = SINGLETON;

    private Scope() {
    }

    /**
     * 从注解实例解析作用域。
     */
    public static Scope fromAnnotation(Annotation annotation) {
        if (annotation instanceof javax.inject.Singleton) {
            return SINGLETON;
        }
        if (annotation instanceof Named) {
            return SINGLETON;
        }
        return DEFAULT;
    }

    @Override
    public String toString() {
        return this == SINGLETON ? "singleton" : "prototype";
    }
}
