package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.Binder;

/**
 * Guice 风格的可配置模块。
 * <p>
 * 通过实现该接口并在 {@link #configure(Binder)} 中调用链式 DSL 注册绑定，
 * 类似 Google Guice 的 {@code AbstractModule}。
 *
 * <pre>{@code
 * public class MyModule extends AbstractModule {
 *     @Override
 *     protected void configure() {
 *         bind(Service.class).to(ServiceImpl.class);
 *         bindConstant().annotatedWith(Names.named("port")).to(8080);
 *     }
 * }
 * }</pre>
 *
 * @see Injector
 * @see AbstractModule
 */
public interface Module {

    /**
     * 在该方法中使用 {@link Binder} 注册绑定。
     *
     * @param binder 绑定 DSL 入口
     */
    void configure(Binder binder);
}
