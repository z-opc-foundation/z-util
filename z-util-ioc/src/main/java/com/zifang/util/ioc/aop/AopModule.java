package com.zifang.util.ioc.aop;

import com.zifang.util.aop.Advise;
import com.zifang.util.ioc.binder.AbstractModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用于注册 AOP 拦截器的 Module，对标 Guice 的 {@code AbstractModule.configure()} 中调用 {@code bindInterceptor()}。
 *
 * <h3>典型用法：</h3>
 * <pre>{@code
 * Injector injector = Injector.createInjector(new AopModule()
 *     .bindInterceptor(
 *         ClassMatcher.subclassOf(MyService.class),
 *         MethodMatcher.annotatedWith(Loggable.class),
 *         LogAdvise.class));
 * }</pre>
 *
 * <p>被拦截的 Bean 在创建后会被自动包装为 JDK 动态代理，原方法调用会先经过 advise 链。
 *
 * <h3>已知限制：</h3>
 * <ul>
 *   <li>仅支持接口代理（依赖 {@link com.zifang.util.aop.ProxyFactory}）。</li>
 *   <li>若 Bean 类型未实现任何接口，则跳过代理（不抛错）。</li>
 * </ul>
 */
public class AopModule extends AbstractModule {

    private final List<InterceptorBinding> bindings = new ArrayList<>();

    @SafeVarargs
    public final AopModule bindInterceptor(ClassMatcher classMatcher,
                                           Class<? extends Advise>... advises) {
        bindings.add(InterceptorBinding.of(classMatcher, advises));
        return this;
    }

    @SafeVarargs
    public final AopModule bindInterceptor(ClassMatcher classMatcher,
                                           MethodMatcher methodMatcher,
                                           Class<? extends Advise>... advises) {
        bindings.add(InterceptorBinding.of(classMatcher, methodMatcher, advises));
        return this;
    }

    public List<InterceptorBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    /**
     * 由于该 Module 已绑定到具体拦截规则，{@link #configure()} 不再做额外绑定操作。
     */
    @Override
    protected void configure() {
        // no-op：拦截规则通过 fluent API 注册
    }
}
