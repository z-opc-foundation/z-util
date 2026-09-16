package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Quintet<A, B, C, D, E> extends Quartet<A, B, C, D> {
    protected E e;

    /**
     * Quintet方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @param d D类型参数
     * @param e E类型参数
     */
    public Quintet(A a, B b, C c, D d, E e) {
        super(a, b, c, d);
        this.e = e;
    }

    /**
     * getE方法。
     *
     * @return E类型返回值
     */
    public E getE() {
        return e;
    }

    /**
     * setE方法。
     * * @param e E类型参数
     */
    public void setE(E e) {
        this.e = e;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Quintet{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + "}";
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
        Quintet<?, ?, ?, ?, ?> quintet = (Quintet<?, ?, ?, ?, ?>) o;
        return java.util.Objects.equals(e, quintet.e);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), e);
    }
}