package com.zifang.util.zex.interview.demo2;

import java.lang.reflect.Proxy;

/**
 * 动态代理测试类。
 * <p>
 * 此类用于测试Java动态代理功能。
 * 展示了如何使用ProxyUtil创建动态代理实例。
 *
 * @author zifang
 * @version 1.0
 */
public class Test {


    private static Object createObject(String str) {

        IAImplement iaImplement = new IAImplement(str);
        Object o = Proxy.newProxyInstance(Test.class.getClassLoader(), IAImplement.class.getInterfaces(), new IAInvocationHandler(iaImplement, str));
        return o;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        IA ia = ProxyUtil.newProxyInstance((proxy, method, args1) -> {
            System.out.println(method.getName());
            return null;
        }, IA.class);
        ia.getName();
    }
}
