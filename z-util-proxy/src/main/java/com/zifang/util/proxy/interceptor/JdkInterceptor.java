package com.zifang.util.proxy.interceptor;

import com.zifang.util.proxy.aspects.Aspect;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * JDK实现的动态代理切面
 */
public class JdkInterceptor implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;

    private Object target;
    private Aspect aspect;

    /**
     * 构造
     *
     * @param target 被代理对象
     * @param aspect 切面实现
     */
    public JdkInterceptor(Object target, Aspect aspect) {
        this.target = target;
        this.aspect = aspect;
    }

    /**
     * getTarget方法。
     *
     * @return Object类型返回值
     */
    public Object getTarget() {
        return this.target;
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
        final Object target = this.target;
        final Aspect aspect = this.aspect;

        // 开始前回调，返回 false 则跳过目标方法，也不调用 after
        if (!aspect.before(target, method, args)) {
            return null;
        }

        Object result = null;
        try {
            method.setAccessible(true);
            result = method.invoke(Modifier.isStatic(method.getModifiers()) ? null : target, args);
        } catch (InvocationTargetException e) {
            // 总是抛出原始异常（afterException 返回值只影响 after() 是否调用）
            aspect.afterException(target, method, args, e.getTargetException());
            throw e;
        } catch (IllegalAccessException e) {
            aspect.afterException(target, method, args, e);
            throw e;
        }

        // 结束执行回调
        if (aspect.after(target, method, args, result)) {
            return result;
        }
        return null;
    }
}
