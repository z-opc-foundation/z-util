package com.zifang.util.proxy.interceptor;

import com.zifang.util.proxy.aspects.Aspect;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Cglib实现的动态代理切面
 */
public class CglibInterceptor implements MethodInterceptor, Serializable {
    private static final long serialVersionUID = 1L;

    private final Object target;
    private final Aspect aspect;

    /**
     * 构造
     *
     * @param target 被代理对象
     * @param aspect 切面实现
     */
    public CglibInterceptor(Object target, Aspect aspect) {
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
     * intercept方法。
     *      * @param obj Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @param proxy MethodProxy类型参数
     * @return Object类型返回值
     */
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        final Object target = this.target;
        final Aspect aspect = this.aspect;

        // 开始前回调，返回 false 则跳过目标方法，也不调用 after
        if (!aspect.before(target, method, args)) {
            return null;
        }

        Object result = null;
        try {
            result = proxy.invokeSuper(obj, args);
        } catch (RuntimeException e) {
            // cglib invokeSuper 直接抛原始异常（非 InvocationTargetException）
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
