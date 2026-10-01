package com.zifang.util.aop;

import java.lang.reflect.Method;

/**
 * AOP 拦截器 SPI（自研，对标 MethodInterceptor）。
 * <p>
 * 用户实现此接口并通过 {@link Intercept#value} 关联到目标方法。
 * <p>
 * 异常传播：若 advise 抛异常，调用方会收到（advice 可吞掉/转换）。
 *
 * @param <T> 目标对象类型
 */
@FunctionalInterface
public interface Advise<T> {

    /**
     * 在方法调用前后执行。
     *
     * @param target 目标对象
     * @param method 反射方法
     * @param args   方法参数
     * @param chain  责任链，调用 {@link Chain#proceed} 进入下一个 advise 或真实方法
     * @return 方法返回值
     * @throws Throwable 任意异常
     */
    Object around(T target, Method method, Object[] args, Chain chain) throws Throwable;

    /**
     * 责任链。
     */
    interface Chain {
        Object proceed() throws Throwable;
    }
}
