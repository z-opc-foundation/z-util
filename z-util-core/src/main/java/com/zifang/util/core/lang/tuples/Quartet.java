package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Quartet<A, B, C, D> extends Triplet<A, B, C> {
    protected D d;

    /**
     * Quartet方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @param d D类型参数
     */
    public Quartet(A a, B b, C c, D d) {
        super(a, b, c);
        this.d = d;
    }

    /**
     * getD方法。
     *
     * @return D类型返回值
     */
    public D getD() {
        return d;
    }

    /**
     * setD方法。
     * * @param d D类型参数
     */
    public void setD(D d) {
        this.d = d;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Quartet{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + "}";
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
        Quartet<?, ?, ?, ?> quartet = (Quartet<?, ?, ?, ?>) o;
        return java.util.Objects.equals(d, quartet.d);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), d);
    }
}