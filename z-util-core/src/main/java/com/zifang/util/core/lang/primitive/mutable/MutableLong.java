package com.zifang.util.core.lang.primitive.mutable;

import com.zifang.util.core.lang.NumberUtil;

/**
 * @author: zifang
 * @time: 2022-06-08 10:51:55
 * @description: A mutable {@code long} wrapper. Note that as MutableLong does not extend Long, it
 * is not treated by String.format() as a Long parameter.
 * @version: JDK 1.8
 */
public class MutableLong extends Number implements Comparable<MutableLong>, Mutable<Number>,
        Cloneable {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 4499260596013738447L;

    /**
     * The mutable value.
     */
    private long value;

    /**
     * Constructs a new MutableLong with the default value of zero.
     */
    public MutableLong() {
    }

    /**
     * Constructs a new MutableLong with the specified value.
     *
     * @param value the initial value to store
     */
    public MutableLong(final long value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableLong with the specified value.
     *
     * @param value the initial value to store, not null
     * @throws NullPointerException if the object is null
     */
    public MutableLong(final Number value) {
        this.value = value.longValue();
    }

    /**
     * Constructs a new MutableLong parsing the given string.
     *
     * @param value the string to parse, not null
     * @throws NumberFormatException if the string cannot be parsed into a long
     */
    public MutableLong(final String value) {
        this.value = Long.parseLong(value);
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the value as a Long instance.
     *
     * @return the value as a Long, never null
     */
    @Override
    /**
     * getValue方法。
     * @return long类型返回值
     */
    public Long getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(final long value) {
        this.value = value;
    }

    /**
     * Sets the value from any Number instance.
     *
     * @param value the value to set, not null
     * @throws NullPointerException if the object is null
     */
    @Override
    /**
     * setValue方法。
     *      * @param value final类型参数
     */
    public void setValue(final Number value) {
        this.value = value.longValue();
    }

    //-----------------------------------------------------------------------

    /**
     * Increments the value.
     */
    public void increment() {
        value++;
    }

    /**
     * Increments this instance's value by 1; this method returns the value associated with the
     * instance immediately prior to the increment operation. This method is not thread safe.
     *
     * @return the value associated with the instance before it was incremented
     */
    public long getAndIncrement() {
        final long last = value;
        value++;
        return last;
    }

    /**
     * Increments this instance's value by 1; this method returns the value associated with the
     * instance immediately after the increment operation. This method is not thread safe.
     *
     * @return the value associated with the instance after it is incremented
     */
    public long incrementAndGet() {
        value++;
        return value;
    }

    /**
     * Decrements the value.
     */
    public void decrement() {
        value--;
    }

    /**
     * Decrements this instance's value by 1; this method returns the value associated with the
     * instance immediately prior to the decrement operation. This method is not thread safe.
     *
     * @return the value associated with the instance before it was decremented
     */
    public long getAndDecrement() {
        final long last = value;
        value--;
        return last;
    }

    /**
     * Decrements this instance's value by 1; this method returns the value associated with the
     * instance immediately after the decrement operation. This method is not thread safe.
     *
     * @return the value associated with the instance after it is decremented
     */
    public long decrementAndGet() {
        value--;
        return value;
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a value to the value of this instance.
     *
     * @param operand the value to add, not null
     */
    public void add(final long operand) {
        this.value += operand;
    }

    /**
     * Adds a value to the value of this instance.
     *
     * @param operand the value to add, not null
     * @throws NullPointerException if the object is null
     */
    public void add(final Number operand) {
        this.value += operand.longValue();
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand the value to subtract, not null
     */
    public void subtract(final long operand) {
        this.value -= operand;
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand the value to subtract, not null
     * @throws NullPointerException if the object is null
     */
    public void subtract(final Number operand) {
        this.value -= operand.longValue();
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated
     * with the instance immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance after adding the operand
     */
    public long addAndGet(final long operand) {
        this.value += operand;
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated
     * with the instance immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance after adding the operand
     * @throws NullPointerException if {@code operand} is null
     */
    public long addAndGet(final Number operand) {
        this.value += operand.longValue();
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated
     * with the instance immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance immediately before the operand was added
     */
    public long getAndAdd(final long operand) {
        final long last = value;
        this.value += operand;
        return last;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated
     * with the instance immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance immediately before the operand was added
     * @throws NullPointerException if {@code operand} is null
     */
    public long getAndAdd(final Number operand) {
        final long last = value;
        this.value += operand.longValue();
        return last;
    }

    //-----------------------------------------------------------------------
    // shortValue and byteValue rely on Number implementation

    /**
     * Returns the value of this MutableLong as an int.
     *
     * @return the numeric value represented by this object after conversion to type int.
     */
    @Override
    /**
     * intValue方法。
     * @return int类型返回值
     */
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the value of this MutableLong as a long.
     *
     * @return the numeric value represented by this object after conversion to type long.
     */
    @Override
    /**
     * longValue方法。
     * @return long类型返回值
     */
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this MutableLong as a float.
     *
     * @return the numeric value represented by this object after conversion to type float.
     */
    @Override
    /**
     * floatValue方法。
     * @return float类型返回值
     */
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableLong as a double.
     *
     * @return the numeric value represented by this object after conversion to type double.
     */
    @Override
    /**
     * doubleValue方法。
     * @return double类型返回值
     */
    public double doubleValue() {
        return value;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets this mutable as an instance of Long.
     *
     * @return a Long instance containing the value from this mutable, never null
     */
    public Long toLong() {
        return longValue();
    }

    //-----------------------------------------------------------------------

    /**
     * Compzifang this object to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is a {@code MutableLong} object that contains the same {@code
     * long} value as this object.
     *
     * @param obj the object to compare with, null returns false
     * @return {@code true} if the objects are the same; {@code false} otherwise.
     */
    @Override
    /**
     * equals方法。
     *      * @param obj final类型参数
     * @return boolean类型返回值
     */
    public boolean equals(final Object obj) {
        if (obj instanceof MutableLong) {
            return value == ((MutableLong) obj).longValue();
        }
        return false;
    }

    /**
     * Returns a suitable hash code for this mutable.
     *
     * @return a suitable hash code
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    //-----------------------------------------------------------------------

    /**
     * Compzifang this mutable to another in ascending order.
     *
     * @param other the other mutable to compare to, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    /**
     * compareTo方法。
     *      * @param other final类型参数
     * @return int类型返回值
     */
    public int compareTo(final MutableLong other) {
        return NumberUtil.compare(this.value, other.value);
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
        return String.valueOf(value);
    }

    @Override
    /**
     * clone方法。
     * @return MutableLong类型返回值
     */
    public MutableLong clone() {
        return new MutableLong(this.value);
    }
}
