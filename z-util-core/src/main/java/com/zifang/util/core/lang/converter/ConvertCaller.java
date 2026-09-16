package com.zifang.util.core.lang.converter;

import com.zifang.util.core.lang.PrimitiveUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 执行转换的句柄包装
 */
public class ConvertCaller<F, T> implements IConverter<F, T> {

    private Method method;
    private Object caller;

    private Class<?> from;
    private Class<?> target;

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

    /**
     * getCaller方法。
     *
     * @return Object类型返回值
     */
    public Object getCaller() {
        return caller;
    }

    /**
     * setCaller方法。
     * * @param caller Object类型参数
     */
    public void setCaller(Object caller) {
        this.caller = caller;
    }

    /**
     * getFrom方法。
     *
     * @return Class<?>类型返回值
     */
    public Class<?> getFrom() {
        return from;
    }

    /**
     * setFrom方法。
     * * @param from Class?类型参数
     */
    public void setFrom(Class<?> from) {
        this.from = from;
    }

    /**
     * getTarget方法。
     *
     * @return Class<?>类型返回值
     */
    public Class<?> getTarget() {
        return target;
    }

    /**
     * setTarget方法。
     * * @param target Class?类型参数
     */
    public void setTarget(Class<?> target) {
        this.target = target;
    }

    /**
     * to方法。
     * * @param o F类型参数
     *
     * @return T类型返回值
     */
    public T to(F o) {
        Object defaultValue = null;
        try {
            if (PrimitiveUtil.isGeneralType(target)) {
                defaultValue = target.newInstance();
            } else {
                defaultValue = PrimitiveUtil.defaultValue(target);
            }
            method.setAccessible(true);
            return (T) method.invoke(caller, o, defaultValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * to方法。
     * * @param o Object类型参数
     *
     * @param defaultValue Object类型参数
     * @return T类型返回值
     */
    public T to(Object o, Object defaultValue) {
        if (from == target) {
            return (T) o;
        }
        try {
            return (T) method.invoke(caller, o, defaultValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * copy方法。
     *
     * @return ConvertCaller<F, T>类型返回值
     */
    public ConvertCaller<F, T> copy() {
        ConvertCaller<F, T> convertCaller = new ConvertCaller<>();
        convertCaller.setMethod(method);
        convertCaller.setCaller(caller);
        convertCaller.setFrom(from);
        convertCaller.setTarget(target);
        return convertCaller;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ConvertCaller{method=" + method + ", caller=" + caller +
                ", from=" + from + ", target=" + target + "}";
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
        ConvertCaller<?, ?> that = (ConvertCaller<?, ?>) o;
        return java.util.Objects.equals(method, that.method) &&
                java.util.Objects.equals(caller, that.caller) &&
                java.util.Objects.equals(from, that.from) &&
                java.util.Objects.equals(target, that.target);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(method, caller, from, target);
    }
}