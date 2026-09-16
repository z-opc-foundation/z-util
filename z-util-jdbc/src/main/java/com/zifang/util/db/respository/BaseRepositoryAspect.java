package com.zifang.util.db.respository;


import com.zifang.util.proxy.aspects.Aspect;

import java.lang.reflect.Method;

/**
 * 仓储切面基类，提供方法执行前后的拦截能力
 */
public class BaseRepositoryAspect implements Aspect {


    /**
     * 方法执行前拦截
     *
     * @param target 目标对象
     * @param method 被调用的方法
     * @param args   方法参数
     * @return true继续执行，false阻止执行
     */
    @Override
    /**
     * before方法。
     *      * @param target Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @return boolean类型返回值
     */
    public boolean before(Object target, Method method, Object[] args) {
        return false;
    }

    /**
     * 方法执行后拦截
     *
     * @param target    目标对象
     * @param method    被调用的方法
     * @param args      方法参数
     * @param returnVal 返回值
     * @return true正常返回，false阻止返回
     */
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

    /**
     * 方法抛出异常时拦截
     *
     * @param target 目标对象
     * @param method 被调用的方法
     * @param args   方法参数
     * @param e      抛出的异常
     * @return true处理后继续抛出，false阻止抛出
     */
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
