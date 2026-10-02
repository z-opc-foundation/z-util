package com.zifang.util.aop.proxy;

import com.zifang.util.aop.aspects.Aspect;

import java.io.Serializable;

/**
 * 代理工厂<br>
 * 根据用户引入代理库的不同，产生不同的代理对象
 */
public abstract class ProxyFactory implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 创建代理对象
     *
     * @param <T>         切面对象类型
     * @param target      目标对象
     * @param aspectClass 切面对象类
     * @return 代理对象
     */
    public static <T> T createProxy(T target, Class<? extends Aspect> aspectClass) {
        try {
            return createProxy(target, aspectClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建代理对象
     *
     * @param <T>    代理对象类型
     * @param target 被代理对象
     * @param aspect 切面实现
     * @return 代理对象
     */
    public static <T> T createProxy(T target, Aspect aspect) {
        return create().proxy(target, aspect);
    }

    /**
     * 创建代理工厂。
     * <p>
     * 2026-10-02 起 cglib 依赖已移除：类代理走自研 {@link ClassProxyFactory}
     * （SourceProxyFactory 内存编译子类代理，零三方依赖）；接口代理走
     * {@link JdkProxyFactory}。类代理能力是接口代理的超集，默认直接类代理。
     *
     * @return 代理工厂
     */
    public static ProxyFactory create() {
        return new ClassProxyFactory();
    }

    /**
     * 创建代理
     *
     * @param <T>    代理对象类型
     * @param target 被代理对象
     * @param aspect 切面实现
     * @return 代理对象
     */
    public abstract <T> T proxy(T target, Aspect aspect);
}
