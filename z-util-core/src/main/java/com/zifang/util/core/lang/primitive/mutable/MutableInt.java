package com.zifang.util.core.lang.primitive.mutable;


/**
 * @author: zifang
 * @time: 2021-12-27 18:41:43
 * @description: mutable int
 * @version: JDK 1.8
 */
public class MutableInt extends Number implements Comparable<MutableInt>, Mutable<Number>,
        Cloneable {

    private static final long serialVersionUID = 8347796710252350916L;

    private int value;

    /**
     * MutableInt方法。
     */
    public MutableInt() {
    }

    /**
     * MutableInt方法。
     * * @param value final类型参数
     */
    public MutableInt(final int value) {
        this.value = value;
    }

    /**
     * MutableInt方法。
     * * @param value final类型参数
     */
    public MutableInt(final Number value) {
        this.value = value.intValue();
    }


    /**
     * MutableInt方法。
     * * @param value final类型参数
     */
    public MutableInt(final String value) {
        this.value = Integer.parseInt(value);
    }

    @Override
    /**
     * getValue方法。
     * @return int类型返回值
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(final int value) {
        this.value = value;
    }


    @Override
    /**
     * setValue方法。
     *      * @param value final类型参数
     */
    public void setValue(final Number value) {
        this.value = value.intValue();
    }


    /**
     * increment方法。
     */
    public void increment() {
        value++;
    }


    /**
     * getAndIncrement方法。
     *
     * @return int类型返回值
     */
    public int getAndIncrement() {
        final int last = value;
        value++;
        return last;
    }


    /**
     * incrementAndGet方法。
     *
     * @return int类型返回值
     */
    public int incrementAndGet() {
        value++;
        return value;
    }


    /**
     * decrement方法。
     */
    public void decrement() {
        value--;
    }


    /**
     * getAndDecrement方法。
     *
     * @return int类型返回值
     */
    public int getAndDecrement() {
        final int last = value;
        value--;
        return last;
    }


    /**
     * decrementAndGet方法。
     *
     * @return int类型返回值
     */
    public int decrementAndGet() {
        value--;
        return value;
    }

    /**
     * add方法。
     * * @param operand final类型参数
     */
    public void add(final int operand) {
        this.value += operand;
    }


    /**
     * add方法。
     * * @param operand final类型参数
     */
    public void add(final Number operand) {
        this.value += operand.intValue();
    }


    /**
     * subtract方法。
     * * @param operand final类型参数
     */
    public void subtract(final int operand) {
        this.value -= operand;
    }


    /**
     * subtract方法。
     * * @param operand final类型参数
     */
    public void subtract(final Number operand) {
        this.value -= operand.intValue();
    }


    /**
     * addAndGet方法。
     * * @param operand final类型参数
     *
     * @return int类型返回值
     */
    public int addAndGet(final int operand) {
        this.value += operand;
        return value;
    }


    /**
     * addAndGet方法。
     * * @param operand final类型参数
     *
     * @return int类型返回值
     */
    public int addAndGet(final Number operand) {
        this.value += operand.intValue();
        return value;
    }

    /**
     * getAndAdd方法。
     * * @param operand final类型参数
     *
     * @return int类型返回值
     */
    public int getAndAdd(final int operand) {
        final int last = value;
        this.value += operand;
        return last;
    }

    /**
     * getAndAdd方法。
     * * @param operand final类型参数
     *
     * @return int类型返回值
     */
    public int getAndAdd(final Number operand) {
        final int last = value;
        this.value += operand.intValue();
        return last;
    }

    @Override
    /**
     * intValue方法。
     * @return int类型返回值
     */
    public int intValue() {
        return value;
    }

    @Override
    /**
     * longValue方法。
     * @return long类型返回值
     */
    public long longValue() {
        return value;
    }

    @Override
    /**
     * floatValue方法。
     * @return float类型返回值
     */
    public float floatValue() {
        return value;
    }


    @Override
    /**
     * doubleValue方法。
     * @return double类型返回值
     */
    public double doubleValue() {
        return value;
    }

    /**
     * toInteger方法。
     *
     * @return int类型返回值
     */
    public Integer toInteger() {
        return intValue();
    }


    @Override
    /**
     * equals方法。
     *      * @param obj final类型参数
     * @return boolean类型返回值
     */
    public boolean equals(final Object obj) {
        if (obj instanceof MutableInt) {
            return value == ((MutableInt) obj).intValue();
        }
        return false;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return value;
    }

    @Override
    /**
     * compareTo方法。
     *      * @param other final类型参数
     * @return int类型返回值
     */
    public int compareTo(final MutableInt other) {
        if (this.value == other.value) {
            return 0;
        }
        return this.value < other.value ? -1 : 1;
    }

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
     * @return MutableInt类型返回值
     */
    public MutableInt clone() {
        return new MutableInt(this.value);
    }

}
