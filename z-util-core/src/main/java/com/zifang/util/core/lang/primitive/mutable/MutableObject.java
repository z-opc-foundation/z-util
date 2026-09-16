package com.zifang.util.core.lang.primitive.mutable;

import java.io.Serializable;

/**
 * @author: zifang
 * @time: 2022-06-08 10:54:30
 * @description: mutable {@code Object} wrapper.
 * @version: JDK 1.8
 */
public class MutableObject<T> implements Mutable<T>, Serializable, Cloneable {

    /**
     * Required for serialization support.
     *
     * @see Serializable
     */
    private static final long serialVersionUID = -3401651784962632548L;

    /**
     * The mutable value.
     */
    private T value;

    /**
     * Constructs a new MutableObject with the default value of {@code null}.
     */
    public MutableObject() {
    }

    /**
     * Constructs a new MutableObject with the specified value.
     *
     * @param value the initial value to store
     */
    public MutableObject(final T value) {
        this.value = value;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the value.
     *
     * @return the value, may be null
     */
    @Override
    /**
     * getValue方法。
     * @return T类型返回值
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    @Override
    /**
     * setValue方法。
     *      * @param value final类型参数
     */
    public void setValue(final T value) {
        this.value = value;
    }

    //-----------------------------------------------------------------------

    /**
     * <p>
     * Compzifang this object against the specified object. The result is {@code true} if and only if
     * the argument is not {@code null} and is a {@code MutableObject} object that contains the same
     * {@code T} value as this object.
     * </p>
     *
     * @param obj the object to compare with, {@code null} returns {@code false}
     * @return {@code true} if the objects are the same; {@code true} if the objects have equivalent
     * {@code value} fields; {@code false} otherwise.
     */
    @Override
    /**
     * equals方法。
     *      * @param obj final类型参数
     * @return boolean类型返回值
     */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            final MutableObject<?> that = (MutableObject<?>) obj;
            return this.value.equals(that.value);
        }
        return false;
    }

    /**
     * Returns the value's hash code or {@code 0} if the value is {@code null}.
     *
     * @return the value's hash code or {@code 0} if the value is {@code null}.
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    //-----------------------------------------------------------------------

    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return value == null ? "null" : value.toString();
    }

    @Override
    /**
     * clone方法。
     * @return MutableObject<T>类型返回值
     */
    public MutableObject<T> clone() {
        return new MutableObject<>(this.value);
    }
}