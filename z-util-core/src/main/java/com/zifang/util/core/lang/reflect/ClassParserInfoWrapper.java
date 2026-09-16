package com.zifang.util.core.lang.reflect;

import java.lang.reflect.Type;

/**
 * ClassParserInfoWrapper
 */
public class ClassParserInfoWrapper {

    private Class<?> clazz;
    private Type type;

    /**
     * ClassParserInfoWrapper方法。
     */
    public ClassParserInfoWrapper() {
    }

    /**
     * ClassParserInfoWrapper方法。
     * * @param clazz Class?类型参数
     *
     * @param type Type类型参数
     */
    public ClassParserInfoWrapper(Class<?> clazz, Type type) {
        this.clazz = clazz;
        this.type = type;
    }

    /**
     * getClazz方法。
     *
     * @return Class<?>类型返回值
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * setClazz方法。
     * * @param clazz Class?类型参数
     */
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * getType方法。
     *
     * @return Type类型返回值
     */
    public Type getType() {
        return type;
    }

    /**
     * setType方法。
     * * @param type Type类型参数
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ClassParserInfoWrapper{clazz=" + clazz + ", type=" + type + "}";
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
        ClassParserInfoWrapper that = (ClassParserInfoWrapper) o;
        return java.util.Objects.equals(clazz, that.clazz) &&
                java.util.Objects.equals(type, that.type);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(clazz, type);
    }
}