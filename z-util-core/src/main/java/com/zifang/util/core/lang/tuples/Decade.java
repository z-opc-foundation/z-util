package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Decade<A, B, C, D, E, F, G, H, I, J> extends Ennead<A, B, C, D, E, F, G, H, I> {
    protected J j;

    /**
     * Decade方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @param d D类型参数
     * @param e E类型参数
     * @param f F类型参数
     * @param g G类型参数
     * @param h H类型参数
     * @param i I类型参数
     * @param j J类型参数
     */
    public Decade(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) {
        super(a, b, c, d, e, f, g, h, i);
        this.j = j;
    }

    /**
     * getJ方法。
     *
     * @return J类型返回值
     */
    public J getJ() {
        return j;
    }

    /**
     * setJ方法。
     * * @param j J类型参数
     */
    public void setJ(J j) {
        this.j = j;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Decade{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + ", g=" + g + ", h=" + h + ", i=" + i + ", j=" + j + "}";
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
        Decade<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> decade = (Decade<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
        return java.util.Objects.equals(j, decade.j);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), j);
    }
}