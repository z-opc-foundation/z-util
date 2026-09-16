package com.zifang.util.proxy;

import com.zifang.util.proxy.aspects.Aspect;
import org.junit.Test;

import java.lang.reflect.Method;


/**
 * ProxyUtilTest类。
 */
public class ProxyUtilTest {

    @Test
    /**
     * proxy方法。
     */
    public void proxy() {
        A a = ProxyUtil.proxy(new A(), new CustomerAspect());
        a.ex();
    }
}

class A {
    /**
     * ex方法。
     */
    public void ex() {
        System.out.println("this is ex");
    }
}

class CustomerAspect implements Aspect {

    @Override
    /**
     * before方法。
     *      * @param target Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @return boolean类型返回值
     */
    public boolean before(Object target, Method method, Object[] args) {
        System.out.println("before");
        return true;
    }

    @Override
    /**
     * after方法。
     *      * @param target Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @param returnVal Object类型参数
     * @return boolean类型返回值
     */
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        return false;
    }

    @Override
    /**
     * afterException方法。
     *      * @param target Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @param e Throwable类型参数
     * @return boolean类型返回值
     */
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        return false;
    }
}