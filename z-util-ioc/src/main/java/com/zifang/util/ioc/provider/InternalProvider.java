package com.zifang.util.ioc.provider;

import com.zifang.util.ioc.Injector;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * 容器内部的 {@link Provider} 实现，对标 Guice 的 {@code InternalFactoryToProviderAdapter}。
 * <p>
 * 持有一个 {@link Injector} 与查找键，每次 {@link #get()} 时委托给 Injector 按需解析（可能触发 Bean 创建）。
 *
 * @param <T> 提供类型
 */
public class InternalProvider<T> implements Provider<T> {

    private final Injector injector;
    private final Class<T> type;
    private final Class<? extends Annotation> qualifier;
    private final String name;

    public InternalProvider(Injector injector, Class<T> type) {
        this(injector, type, null, null);
    }

    public InternalProvider(Injector injector, Class<T> type, Class<? extends Annotation> qualifier) {
        this(injector, type, qualifier, null);
    }

    public InternalProvider(Injector injector, Class<T> type, Class<? extends Annotation> qualifier, String name) {
        this.injector = injector;
        this.type = type;
        this.qualifier = qualifier;
        this.name = name;
    }

    @Override
    public T get() {
        if (qualifier != null) {
            return injector.getInstance(type, qualifier);
        }
        if (name != null && !name.isEmpty()) {
            return injector.getInstance(type, name);
        }
        return injector.getInstance(type);
    }

    @Override
    public String toString() {
        return "InternalProvider<" + type.getSimpleName()
                + (qualifier != null ? " @" + qualifier.getSimpleName() : "")
                + (name != null ? " [\"" + name + "\"]" : "")
                + ">";
    }
}
