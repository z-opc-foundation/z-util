package com.zifang.util.ioc.binder;

import com.zifang.util.ioc.metadata.Scope;

import javax.inject.Provider;

/**
 * Guice 风格的链式绑定构建器。
 * <p>
 * 由 {@link Binder#bind(Class)} 返回，支持链式调用 {@link #to(Class)} / {@link #toInstance(Object)} /
 * {@link #toProvider(Provider)} / {@link #in(Scope)} 等。
 *
 * <h3>典型用法：</h3>
 * <pre>{@code
 * bind(Service.class).to(ServiceImpl.class);
 * bind(Service.class).toInstance(new ServiceImpl());
 * bind(Service.class).toProvider(MyProvider.class);
 * bind(Service.class).in(Scopes.SINGLETON);
 * }</pre>
 */
public interface BindingBuilder<T> {

    /**
     * 将类型 T 绑定到实现类 {@code impl}（{@code impl} 必须是 {@code T} 的子类）。
     */
    BindingBuilder<T> to(Class<? extends T> impl);

    /**
     * 将类型 T 绑定到一个具体实例（隐含单例）。
     */
    BindingBuilder<T> toInstance(T instance);

    /**
     * 将类型 T 绑定到一个 {@link Provider}，由 Provider 决定如何获取实例。
     */
    BindingBuilder<T> toProvider(Provider<T> provider);

    /**
     * 将类型 T 绑定到一个 {@link Provider} 类型（容器实例化该 Provider 类）。
     */
    BindingBuilder<T> toProvider(Class<? extends Provider<T>> providerType);

    /**
     * 指定作用域，对标 Guice 的 {@code .in(Singleton.class)} 等。
     */
    BindingBuilder<T> in(Scope scope);

    /**
     * 标记为急切单例：在 Injector 创建时就立即实例化（而非首次获取时）。
     */
    BindingBuilder<T> asEagerSingleton();
}
