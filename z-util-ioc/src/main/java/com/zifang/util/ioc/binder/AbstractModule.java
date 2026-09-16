package com.zifang.util.ioc.binder;

import com.zifang.util.ioc.Module;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Guice 风格的 {@link Module} 抽象基类，对标 {@code AbstractModule}。
 * <p>
 * 子类重写 {@link #configure()}（注意：不带参数）使用 {@link #bind(Class)} 等便捷方法注册绑定。
 *
 * <pre>{@code
 * public class MyModule extends AbstractModule {
 *     @Override
 *     protected void configure() {
 *         bind(Service.class).to(ServiceImpl.class);
 *         bind(Service.class).annotatedWith(Names.named("primary")).to(PrimaryService.class);
 *     }
 * }
 * }</pre>
 */
public abstract class AbstractModule implements Module {

    private final List<DefaultBinder.BindingBuilderImpl<?>> openBuilders = new ArrayList<>();
    private DefaultBinder binder;

    @Override
    public final void configure(Binder binder) {
        if (this.binder != null) {
            throw new IllegalStateException("Re-entry into configure() is not allowed.");
        }
        this.binder = (DefaultBinder) binder;
        try {
            configure();
            // commit any unfinalized bindings as JIT (Guice-style: bind(X) alone is valid)
            for (DefaultBinder.BindingBuilderImpl<?> b : openBuilders) {
                b.commitIfNotFinalized();
            }
        } finally {
            this.binder = null;
            openBuilders.clear();
        }
    }

    /**
     * 子类重写此方法进行绑定。
     */
    protected abstract void configure();

    /**
     * 绑定类型（不带限定符）。
     */
    protected <T> BindingBuilder<T> bind(Class<T> type) {
        DefaultBinder.BindingBuilderImpl<T> b = (DefaultBinder.BindingBuilderImpl<T>) binder.bind(type);
        openBuilders.add(b);
        return b;
    }

    /**
     * 绑定类型 + 限定符。
     */
    protected <T> BindingBuilder<T> bind(Class<T> type, Class<? extends Annotation> qualifier) {
        DefaultBinder.BindingBuilderImpl<T> b = (DefaultBinder.BindingBuilderImpl<T>) binder.bind(type, qualifier);
        openBuilders.add(b);
        return b;
    }

    /**
     * 绑定常量。
     */
    protected ConstantBindingBuilder bindConstant() {
        return binder.bindConstant();
    }

    /**
     * 安装嵌套 Module。
     */
    protected void install(Module module) {
        binder.install(module);
    }

    /**
     * 暴露当前 Binder 以供高级用法。
     */
    protected Binder binder() {
        return binder;
    }
}
