package com.zifang.util.zex.interview.demo2;

import java.lang.reflect.Proxy;

/**
 * 动态代理工具类
 */
public class ProxyUtil {

    @SuppressWarnings("unchecked")
    /**
     * newProxyInstance方法。
     *      * @param handler java.lang.reflect.InvocationHandler类型参数
     * @param interfaceClass ClassT类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T newProxyInstance(java.lang.reflect.InvocationHandler handler, Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler
        );
    }
}
