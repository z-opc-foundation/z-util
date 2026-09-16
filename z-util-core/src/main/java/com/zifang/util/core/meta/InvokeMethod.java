package com.zifang.util.core.meta;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * InvokeMethod
 */
public class InvokeMethod {

    /***
     * An instance of the class in which the method is located
     * 方法所在的类的实例
     */
    private Object target;

    private MethodHandle methodHandle;

    private Method method;

    /**
     * InvokeMethod方法。
     */
    public InvokeMethod() {
    }

    /**
     * InvokeMethod方法。
     * * @param target Object类型参数
     *
     * @param methodHandle MethodHandle类型参数
     * @param method       Method类型参数
     */
    public InvokeMethod(Object target, MethodHandle methodHandle, Method method) {
        this.target = target;
        this.methodHandle = methodHandle;
        this.method = method;
    }

    /**
     * getTarget方法。
     *
     * @return Object类型返回值
     */
    public Object getTarget() {
        return target;
    }

    /**
     * setTarget方法。
     * * @param target Object类型参数
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * getMethodHandle方法。
     *
     * @return MethodHandle类型返回值
     */
    public MethodHandle getMethodHandle() {
        return methodHandle;
    }

    /**
     * setMethodHandle方法。
     * * @param methodHandle MethodHandle类型参数
     */
    public void setMethodHandle(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    /**
     * getMethod方法。
     *
     * @return Method类型返回值
     */
    public Method getMethod() {
        return method;
    }

    /**
     * setMethod方法。
     * * @param method Method类型参数
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "InvokeMethod{target=" + target + ", methodHandle=" + methodHandle + ", method=" + method + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeMethod that = (InvokeMethod) o;
        return java.util.Objects.equals(target, that.target) &&
                java.util.Objects.equals(methodHandle, that.methodHandle) &&
                java.util.Objects.equals(method, that.method);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(target, methodHandle, method);
    }
}