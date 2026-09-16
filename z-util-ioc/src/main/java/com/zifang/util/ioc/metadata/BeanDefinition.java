package com.zifang.util.ioc.metadata;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Bean 定义元数据，对应一个待创建 Bean 的完整描述。
 *
 * <h3>支持的绑定类型：</h3>
 * <ul>
 *   <li>{@link BindingType#COMPONENT} — {@code @Component} 类，最普通的场景</li>
 *   <li>{@link BindingType#FACTORY_METHOD} — 配置类中 {@code @Bean} 方法</li>
 *   <li>{@link BindingType#INSTANCE} — {@code bind(X.class).toInstance(x)}</li>
 *   <li>{@link BindingType#PROVIDER} — {@code bind(X.class).toProvider(...)}</li>
 *   <li>{@link BindingType#LINKED} — {@code bind(X.class).to(Y.class)}</li>
 *   <li>{@link BindingType#JIT} — Just-In-Time 自动绑定的具体类</li>
 * </ul>
 */
public class BeanDefinition {

    private final BindingKey<?> key;
    private final Class<?> beanClass;
    private final BindingType bindingType;
    private final Method factoryMethod;
    private final Class<?> configClass;       // FACTORY_METHOD 时配置类的类
    private final javax.inject.Provider<?> provider; // PROVIDER 绑定时携带
    private final boolean isConfiguration;
    private Scope scope;
    private boolean eager;
    private Object instance;
    private long creationTime;

    /**
     * 兼容旧 API 的构造器（按名称构造）。
     */
    public BeanDefinition(String name, Class<?> beanClass) {
        this(BindingKey.of(beanClass, name), beanClass, Scope.DEFAULT, BindingType.COMPONENT,
                false, null, null, null, false);
    }

    public BeanDefinition(String name, Class<?> beanClass, Scope scope,
                          boolean isConfiguration, Method factoryMethod) {
        this(BindingKey.of(beanClass, name), beanClass, scope,
                factoryMethod != null ? BindingType.FACTORY_METHOD : BindingType.COMPONENT,
                isConfiguration, factoryMethod,
                factoryMethod != null ? factoryMethod.getDeclaringClass() : null,
                null, false);
    }

    /**
     * 完整的 BeanDefinition 构造器。
     */
    public BeanDefinition(BindingKey<?> key, Class<?> beanClass, Scope scope,
                          BindingType bindingType, boolean isConfiguration,
                          Method factoryMethod, Class<?> configClass,
                          javax.inject.Provider<?> provider, boolean eager) {
        this.key = key != null ? key : BindingKey.of(beanClass);
        this.beanClass = beanClass;
        this.scope = scope != null ? scope : Scope.DEFAULT;
        this.bindingType = bindingType != null ? bindingType : BindingType.COMPONENT;
        this.isConfiguration = isConfiguration;
        this.factoryMethod = factoryMethod;
        this.configClass = configClass;
        this.provider = provider;
        this.eager = eager;
    }

    /**
     * 构造一个 linked binding（{@code bind(X).to(Y)}）。
     */
    public static <T> BeanDefinition linked(BindingKey<T> key, Class<?> impl, Scope scope, boolean eager) {
        return new BeanDefinition(key, impl, scope, BindingType.LINKED, false, null, null, null, eager);
    }

    /**
     * 构造一个 instance binding（{@code bind(X).toInstance(x)}）。
     */
    public static <T> BeanDefinition instance(BindingKey<T> key, T value) {
        BeanDefinition bd = new BeanDefinition(key, value != null ? (Class<?>) value.getClass() : key.getType(),
                Scope.SINGLETON, BindingType.INSTANCE, false, null, null, null, false);
        bd.instance = value;
        return bd;
    }

    /**
     * 构造一个 provider binding（{@code bind(X).toProvider(...)}）。
     */
    public static <T> BeanDefinition provider(BindingKey<T> key, javax.inject.Provider<T> provider, Scope scope, boolean eager) {
        BeanDefinition bd = new BeanDefinition(key, provider.getClass(), scope, BindingType.PROVIDER,
                false, null, null, provider, eager);
        return bd;
    }

    /**
     * 构造一个 JIT binding（未显式绑定但自动创建的类）。
     */
    public static BeanDefinition jit(Class<?> clazz, Scope scope) {
        return new BeanDefinition(BindingKey.of(clazz), clazz, scope, BindingType.JIT,
                false, null, null, null, false);
    }

    /**
     * 克隆一个 BD，替换 scope 与 eager 字段。
     */
    public static BeanDefinition cloneWithScope(BeanDefinition source, Scope newScope, boolean newEager) {
        BeanDefinition clone = new BeanDefinition(
                source.key, source.beanClass, newScope, source.bindingType,
                source.isConfiguration, source.factoryMethod, source.configClass,
                source.provider, newEager);
        if (source.instance != null) {
            clone.setInstance(source.instance);
        }
        if (source.creationTime != 0L) {
            clone.setCreationTime(source.creationTime);
        }
        return clone;
    }

    public BindingKey<?> getKey() {
        return key;
    }

    public String getName() {
        return key.getName() != null ? key.getName() : beanClass.getName();
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Scope getScope() {
        return scope;
    }

    /**
     * 设置 scope。仅用于运行时调整（如 {@code in(...)} 在 commit 后调用）。
     */
    public void setScope(Scope scope) {
        this.scope = scope != null ? scope : Scope.DEFAULT;
    }

    public boolean isSingleton() {
        return scope == Scope.SINGLETON;
    }

    public boolean isPrototype() {
        return scope == Scope.PROTOTYPE;
    }

    public boolean isConfiguration() {
        return isConfiguration;
    }

    public BindingType getBindingType() {
        return bindingType;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public Class<?> getConfigClass() {
        return configClass;
    }

    @SuppressWarnings("unchecked")
    public <T> javax.inject.Provider<T> getProvider() {
        return (javax.inject.Provider<T>) provider;
    }

    public boolean isEager() {
        return eager;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanDefinition that = (BeanDefinition) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "BeanDefinition{" + "key=" + key + ", scope=" + scope + ", type=" + bindingType + '}';
    }

    /**
     * 绑定类型枚举
     */
    public enum BindingType {
        COMPONENT,
        FACTORY_METHOD,
        INSTANCE,
        PROVIDER,
        LINKED,
        JIT
    }
}
