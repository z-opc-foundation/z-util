package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Triplet<A, B, C> extends Pair<A, B> {

    protected C c;

    /**
     * Triplet方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     */
    public Triplet(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    /**
     * getC方法。
     *
     * @return C类型返回值
     */
    public C getC() {
        return c;
    }

    /**
     * setC方法。
     * * @param c C类型参数
     */
    public void setC(C c) {
        this.c = c;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Triplet{a=" + a + ", b=" + b + ", c=" + c + "}";
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
        if (!super.equals(o)) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return java.util.Objects.equals(c, triplet.c);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), c);
    }
}