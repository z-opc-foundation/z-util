package com.zifang.util.ioc.binder;

import com.zifang.util.ioc.Module;
import com.zifang.util.ioc.metadata.Scope;

import java.lang.annotation.Annotation;

/**
 * 绑定 DSL 入口，对标 Guice 的 {@code Binder}。
 * <p>
 * 通过 {@link Module#configure(Binder)} 拿到此对象，然后链式调用 {@link #bind(Class)} 等。
 */
public interface Binder {

    /**
     * 请求绑定某个类型，后续链式调用 {@link BindingBuilder} 指定实现 / 实例 / Provider / Scope。
     *
     * @param type 要绑定的类型
     * @param <T>  类型参数
     * @return 绑定构建器
     */
    <T> BindingBuilder<T> bind(Class<T> type);

    /**
     * 请求绑定某个类型 + 限定符（{@link javax.inject.Named} 或自定义 {@link javax.inject.Qualifier}）。
     *
     * @param type      要绑定的类型
     * @param qualifier 限定符注解类
     * @param <T>       类型参数
     * @return 绑定构建器
     */
    <T> BindingBuilder<T> bind(Class<T> type, Class<? extends Annotation> qualifier);

    /**
     * 绑定字符串常量，对标 Guice 的 {@code bindConstant().annotatedWith(...).to(...)} 的简写。
     * <p>
     * 后续可使用 {@link ConstantBindingBuilder} 设置限定符与值。
     */
    ConstantBindingBuilder bindConstant();

    /**
     * 将某个作用域注解关联到一个 Scope 实例，对标 Guice 的 {@code bindScope()}。
     *
     * @param scopeAnnotation 作用域注解（如 {@code @Singleton}）
     * @param scope           实际的 Scope 实现
     */
    void bindScope(Class<? extends Annotation> scopeAnnotation, Scope scope);

    /**
     * 在当前 Binder 上安装（嵌套）其他 Module，对标 Guice 的 {@code install()}。
     */
    void install(Module module);
}
