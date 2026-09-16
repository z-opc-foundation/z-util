package com.zifang.util.zex.interview.demo2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * IA接口的动态代理调用处理器。
 * <p>
 * 此类实现了InvocationHandler接口，用于处理IA接口方法的动态代理调用。
 * 当调用代理对象的方法时，会触发此处理器的invoke方法。
 *
 * @author zifang
 * @version 1.0
 */
public class IAInvocationHandler implements InvocationHandler {
    private String s;
    private IAImplement iaImplement;

    /**
     * IAInvocationHandler方法。
     * * @param iaImplement IAImplement类型参数
     *
     * @param s String类型参数
     */
    public IAInvocationHandler(IAImplement iaImplement, String s) {
        this.iaImplement = iaImplement;
        this.s = s;
    }

    @Override
    /**
     * invoke方法。
     *      * @param proxy Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @return Object类型返回值
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returns = null;
        if (method.getName().equals(iaImplement.getMethod())) {
            return iaImplement.getReturns();
        } else {
            throw new RuntimeException("method unkown");
        }
    }
}
