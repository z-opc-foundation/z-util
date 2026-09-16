package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Pair<A, B> extends Unit<A> {
    protected B b;

    /**
     * Pair方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     */
    public Pair(A a, B b) {
        super(a);
        this.b = b;
    }

    /**
     * getB方法。
     *
     * @return B类型返回值
     */
    public B getB() {
        return b;
    }

    /**
     * setB方法。
     * * @param b B类型参数
     */
    public void setB(B b) {
        this.b = b;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return a + ":" + b;
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this.toString().equals(o.toString())) {
            return true;
        }
        return false;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(a, b);
    }
}