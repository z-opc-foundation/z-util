package com.zifang.util.core.lang.tuples;

/**
 * 六元组，包含6个元素的不可变元组。
 *
 * @param <A> 第一个元素类型
 * @param <B> 第二个元素类型
 * @param <C> 第三个元素类型
 * @param <D> 第四个元素类型
 * @param <E> 第五个元素类型
 * @param <F> 第六个元素类型
 * @author zifang
 * @see Quintet
 */
public class Sextet<A, B, C, D, E, F> extends Quintet<A, B, C, D, E> {

    protected F f;

    /**
     * Sextet方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @param d D类型参数
     * @param e E类型参数
     * @param f F类型参数
     */
    public Sextet(A a, B b, C c, D d, E e, F f) {
        super(a, b, c, d, e);
        this.f = f;
    }

    /**
     * getF方法。
     *
     * @return F类型返回值
     */
    public F getF() {
        return f;
    }

    /**
     * setF方法。
     * * @param f F类型参数
     */
    public void setF(F f) {
        this.f = f;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Sextet{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + "}";
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
        Sextet<?, ?, ?, ?, ?, ?> sextet = (Sextet<?, ?, ?, ?, ?, ?>) o;
        return java.util.Objects.equals(f, sextet.f);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), f);
    }
}