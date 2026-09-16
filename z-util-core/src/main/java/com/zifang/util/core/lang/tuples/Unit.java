package com.zifang.util.core.lang.tuples;


import com.zifang.util.core.lang.BeanUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zifang
 */
public class Unit<A> {

    protected A a;

    /**
     * Unit方法。
     * * @param a A类型参数
     */
    public Unit(A a) {
        this.a = a;
    }

    /**
     * getA方法。
     *
     * @return A类型返回值
     */
    public A getA() {
        return a;
    }

    /**
     * setA方法。
     * * @param a A类型参数
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * toMap方法。
     *
     * @return Map<String, Object>类型返回值
     */
    public Map<String, Object> toMap() {
        try {
            return BeanUtil.beanToMap(this);
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return new LinkedHashMap<>();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Unit{a=" + a + "}";
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
        Unit<?> unit = (Unit<?>) o;
        return java.util.Objects.equals(a, unit.a);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(a);
    }
}