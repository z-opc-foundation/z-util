package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Octet<A, B, C, D, E, F, G, H> extends Septet<A, B, C, D, E, F, G> {

    protected H h;

    /**
     * Octet方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @param d D类型参数
     * @param e E类型参数
     * @param f F类型参数
     * @param g G类型参数
     * @param h H类型参数
     */
    public Octet(A a, B b, C c, D d, E e, F f, G g, H h) {
        super(a, b, c, d, e, f, g);
        this.h = h;
    }

    /**
     * getH方法。
     *
     * @return H类型返回值
     */
    public H getH() {
        return h;
    }

    /**
     * setH方法。
     * * @param h H类型参数
     */
    public void setH(H h) {
        this.h = h;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Octet{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + ", g=" + g + ", h=" + h + "}";
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
        Octet<?, ?, ?, ?, ?, ?, ?, ?> octet = (Octet<?, ?, ?, ?, ?, ?, ?, ?>) o;
        return java.util.Objects.equals(h, octet.h);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), h);
    }
}