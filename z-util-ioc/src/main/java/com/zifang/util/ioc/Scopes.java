package com.zifang.util.ioc;

import com.zifang.util.ioc.metadata.Scope;

/**
 * Guice 风格的 {@link Scope} 工厂，对标 {@code Scopes}。
 * <p>
 * 提供常见作用域的静态常量与创建方法。
 */
public final class Scopes {

    /**
     * 单例作用域
     */
    public static final Scope SINGLETON = Scope.SINGLETON;
    /**
     * 原型作用域（每次新建）
     */
    public static final Scope PROTOTYPE = Scope.PROTOTYPE;
    /**
     * 默认作用域（= SINGLETON）
     */
    public static final Scope DEFAULT = Scope.DEFAULT;

    private Scopes() {
    }
}
