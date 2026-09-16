package com.zifang.util.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface ina {
    String ex();
}

/**
 * ProxyTest类。
 */
public class ProxyTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        aa a = new aa();

        ina i = (ina) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), aa.class.getInterfaces(), new InvocationHandler() {
            @Override
            /**
             * invoke方法。
             *      * @param proxy Object类型参数
             * @param method Method类型参数
             * @param args Object[]类型参数
             * @return Object类型返回值
             */
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("invoke before");
                Object returns = method.invoke(a, args);
                System.out.println("invoke after function");
                return returns;
            }
        });
//        ClassUtil.saveClassFile(i.getClass());
        i.ex();
    }
}

class aa implements ina {

    @Override
    /**
     * ex方法。
     * @return String类型返回值
     */
    public String ex() {
        System.out.println("this is com.zifang.util.proxy.aa");
        return "";
    }
}
