package com.zifang.util.ioc.binder;

import com.zifang.util.ioc.Module;
import com.zifang.util.ioc.aop.AopModule;
import com.zifang.util.ioc.aop.InterceptorBinding;
import com.zifang.util.ioc.core.DefaultBeanRegistry;
import com.zifang.util.ioc.exception.BindingException;
import com.zifang.util.ioc.metadata.BeanDefinition;
import com.zifang.util.ioc.metadata.BindingKey;
import com.zifang.util.ioc.metadata.Scope;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Binder} 的默认实现，收集所有绑定规则并写入 {@link DefaultBeanRegistry}。
 * <p>
 * 同时收集 {@link AopModule} 中的拦截规则，注入器在创建 Bean 时会读取它们。
 *
 * <h3>绑定提交语义：</h3>
 * 调用 {@code bind(X.class).to(Y.class)} 时，链尾的 {@code .to()} 会立刻把绑定注册到 registry。
 * 这样无需额外的 commit 调用，DSL 自然结束。
 */
public class DefaultBinder implements Binder {

    private final DefaultBeanRegistry registry;
    private final List<InterceptorBinding> interceptorBindings = new ArrayList<>();
    private final Map<Class<? extends Annotation>, Scope> scopeMappings = new ConcurrentHashMap<>();

    public DefaultBinder(DefaultBeanRegistry registry) {
        this.registry = registry;
    }

    public List<InterceptorBinding> getInterceptorBindings() {
        return interceptorBindings;
    }

    public Map<Class<? extends Annotation>, Scope> getScopeMappings() {
        return scopeMappings;
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> type) {
        return new DefaultBindingBuilder<>(BindingKey.of(type), type);
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> type, Class<? extends Annotation> qualifier) {
        return new BindingBuilderImpl<>(BindingKey.of(type, qualifier), type);
    }

    @Override
    public ConstantBindingBuilder bindConstant() {
        return new DefaultConstantBindingBuilder();
    }

    @Override
    public void bindScope(Class<? extends Annotation> scopeAnnotation, Scope scope) {
        scopeMappings.put(scopeAnnotation, scope);
    }

    @Override
    public void install(Module module) {
        module.configure(this);
        if (module instanceof AopModule) {
            interceptorBindings.addAll(((AopModule) module).getBindings());
        }
    }

    /**
     * 注册一个 BeanDefinition 到 registry。
     */
    void registerBinding(BeanDefinition bd) {
        registry.register(bd);
    }

    /**
     * 内部基类：每次调用 setter 都更新自身字段；调用终结方法（{@code to/toInstance/toProvider}）时立刻注册。
     */
    private class DefaultBindingBuilder<T> extends BindingBuilderImpl<T> {
        DefaultBindingBuilder(BindingKey<T> key, Class<?> declaredType) {
            super(key, declaredType);
        }
    }

    /**
     * 真正实现 BindingBuilder 的类。
     */
    public class BindingBuilderImpl<T> implements BindingBuilder<T> {

        protected final BindingKey<T> key;
        protected final Class<?> declaredType;
        protected Class<?> implClass;
        protected T instance;
        protected Provider<T> provider;
        protected Class<? extends Provider<T>> providerType;
        protected Scope scope = Scope.DEFAULT;
        protected boolean eager = false;
        protected boolean finalized = false;

        protected BindingBuilderImpl(BindingKey<T> key, Class<?> declaredType) {
            this.key = key;
            this.declaredType = declaredType;
        }

        @Override
        public BindingBuilder<T> to(Class<? extends T> impl) {
            ensureNotFinalized();
            if (impl == null || !declaredType.isAssignableFrom(impl)) {
                throw new BindingException("Cannot bind " + declaredType.getName()
                        + " to " + (impl == null ? "null" : impl.getName())
                        + ": impl is not a subtype of the declared type.");
            }
            this.implClass = impl;
            commitAsLinked();
            return this;
        }

        @Override
        public BindingBuilder<T> toInstance(T instance) {
            ensureNotFinalized();
            if (instance == null) {
                throw new BindingException("Cannot bind null instance for " + key);
            }
            this.instance = instance;
            this.scope = Scope.SINGLETON;
            commitAsInstance();
            return this;
        }

        @Override
        public BindingBuilder<T> toProvider(Provider<T> provider) {
            ensureNotFinalized();
            this.provider = provider;
            commitAsProvider();
            return this;
        }

        @Override
        public BindingBuilder<T> toProvider(Class<? extends Provider<T>> providerType) {
            ensureNotFinalized();
            this.providerType = providerType;
            commitAsProviderType();
            return this;
        }

        @Override
        public BindingBuilder<T> in(Scope scope) {
            this.scope = scope;
            if (finalized) {
                // 已提交的 binding：覆盖 scope（重新注册）
                reRegisterWithNewScope();
            }
            return this;
        }

        @Override
        public BindingBuilder<T> asEagerSingleton() {
            this.eager = true;
            this.scope = Scope.SINGLETON;
            if (finalized) {
                reRegisterWithNewScope();
            }
            return this;
        }

        /**
         * 用最新的 scope/eager 重新注册该绑定。
         * <p>
         * 由于 registry 中已存在同 key 的 binding，这里采用「更新原 BD 的 scope/eager」的方式，
         * 而不是重新注册。
         */
        private void reRegisterWithNewScope() {
            com.zifang.util.ioc.metadata.BeanDefinition existing = registry.get(key);
            if (existing != null) {
                existing.setScope(scope); // scope is final in BeanDefinition; use reflection? Use mutable field
                // Since BeanDefinition.scope is final, we need to create a new BD and remove the old.
                com.zifang.util.ioc.metadata.BeanDefinition replacement = com.zifang.util.ioc.metadata.BeanDefinition
                        .cloneWithScope(existing, scope, eager);
                registry.replace(existing, replacement);
            }
        }

        protected void ensureNotFinalized() {
            if (finalized) {
                throw new BindingException("Binding already committed for " + key);
            }
        }

        protected void commitAsLinked() {
            registerBinding(BeanDefinition.linked(key, implClass, scope, eager));
            finalized = true;
        }

        protected void commitAsInstance() {
            registerBinding(BeanDefinition.instance(key, instance));
            finalized = true;
        }

        protected void commitAsProvider() {
            registerBinding(BeanDefinition.provider(key, provider, scope, eager));
            finalized = true;
        }

        protected void commitAsProviderType() {
            try {
                Provider<T> p = providerType.getDeclaredConstructor().newInstance();
                registerBinding(BeanDefinition.provider(key, p, scope, eager));
                finalized = true;
            } catch (ReflectiveOperationException e) {
                throw new BindingException("Cannot instantiate provider: " + providerType, e);
            }
        }

        /**
         * 若 configure() 结束时该 builder 未被任何 .to*() 终结，则提交为 JIT 绑定（对标 Guice 的 {@code bind(X)} 单独调用）。
         * <p>
         * 注意：eager/scope 由本 builder 的当前字段决定。
         */
        public void commitIfNotFinalized() {
            if (!finalized) {
                com.zifang.util.ioc.metadata.BeanDefinition bd = com.zifang.util.ioc.metadata.BeanDefinition
                        .jit(declaredType, scope);
                // 覆盖 eager
                com.zifang.util.ioc.metadata.BeanDefinition withEager = com.zifang.util.ioc.metadata.BeanDefinition
                        .cloneWithScope(bd, bd.getScope(), eager);
                registerBinding(withEager);
                finalized = true;
            }
        }
    }

    /**
     * 注册常量。
     */
    private class DefaultConstantBindingBuilder implements ConstantBindingBuilder {

        private Class<? extends Annotation> qualifier;
        private String qualifierValue;

        @Override
        public ConstantBindingBuilder annotatedWith(Class<? extends Annotation> qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        @Override
        public ConstantBindingBuilder annotatedWith(Class<? extends Annotation> qualifierType, String qualifierValue) {
            this.qualifier = qualifierType;
            this.qualifierValue = qualifierValue;
            return this;
        }

        @Override
        public ConstantBindingBuilder annotatedWithNamed(String name) {
            this.qualifier = javax.inject.Named.class;
            this.qualifierValue = name;
            return this;
        }

        @Override
        public void to(String value) {
            registerConstant(String.class, value);
        }

        @Override
        public void to(int value) {
            registerConstant(int.class, value);
        }

        @Override
        public void to(long value) {
            registerConstant(long.class, value);
        }

        @Override
        public void to(boolean value) {
            registerConstant(boolean.class, value);
        }

        @Override
        public void to(double value) {
            registerConstant(double.class, value);
        }

        @Override
        public void to(Class<?> value) {
            registerConstant(Class.class, value);
        }

        @Override
        public <E extends Enum<E>> void to(E value) {
            registerConstant(value.getDeclaringClass(), value);
        }

        private <T> void registerConstant(Class<T> type, T value) {
            BindingKey<T> key = BindingKey.of(type, qualifier, qualifierValue);
            registerBinding(BeanDefinition.instance(key, value));
        }
    }
}
