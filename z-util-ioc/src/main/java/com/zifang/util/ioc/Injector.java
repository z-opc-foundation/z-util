package com.zifang.util.ioc;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * Guice 风格的核心容器 API，对标 {@code com.google.inject.Injector}。
 * <p>
 * 通过 {@link #createInjector(Module...)} 静态工厂创建实例，
 * 之后调用 {@link #getInstance(Class)} 等方法从容器中解析依赖。
 *
 * <h3>典型用法：</h3>
 * <pre>{@code
 * Injector injector = Injector.createInjector(new MyModule());
 * MyService svc = injector.getInstance(MyService.class);
 * }</pre>
 */
public interface Injector {

    /**
     * 静态工厂方法，对标 Guice 的 {@code Guice.createInjector()}。
     *
     * @param modules 一个或多个 Module
     * @return 已初始化的 Injector
     */
    static Injector createInjector(Module... modules) {
        return new com.zifang.util.ioc.core.DefaultInjector(modules);
    }

    /**
     * 按类型获取实例。
     */
    <T> T getInstance(Class<T> type);

    /**
     * 按类型 + 限定符（{@link javax.inject.Named} 或自定义 {@link javax.inject.Qualifier}）获取实例。
     */
    <T> T getInstance(Class<T> type, Class<? extends Annotation> qualifier);

    /**
     * 按 Bean 名称获取实例。
     */
    <T> T getInstance(Class<T> type, String name);

    /**
     * 获取对应类型的延迟 {@link Provider}，调用 {@link Provider#get()} 时才真正解析。
     */
    <T> Provider<T> getProvider(Class<T> type);

    /**
     * 获取对应类型 + 限定符的延迟 Provider。
     */
    <T> Provider<T> getProvider(Class<T> type, Class<? extends Annotation> qualifier);

    /**
     * 创建一个子 Injector，继承当前 Injector 的所有绑定，并叠加新的模块。
     * <p>
     * 对标 Guice 的 {@code injector.createChildInjector()}。
     * 子 Injector 中的绑定会覆盖父级同名绑定。
     *
     * @param modules 要在子容器中额外安装的模块
     * @return 新的子 Injector
     */
    Injector createChildInjector(Module... modules);
}
