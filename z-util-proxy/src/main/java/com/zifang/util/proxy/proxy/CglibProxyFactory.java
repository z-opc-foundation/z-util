package com.zifang.util.proxy.proxy;

import com.zifang.util.proxy.aspects.Aspect;
import com.zifang.util.proxy.interceptor.CglibInterceptor;
import net.sf.cglib.proxy.Enhancer;

/**
 * 基于Cglib的切面代理工厂
 */
public class CglibProxyFactory extends ProxyFactory {
    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    /**
     * proxy方法。
     *      * @param target T类型参数
     * @param aspect Aspect类型参数
     * @return <T> T类型返回值
     */
    public <T> T proxy(T target, Aspect aspect) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new CglibInterceptor(target, aspect));
        return (T) enhancer.create();
    }

}
