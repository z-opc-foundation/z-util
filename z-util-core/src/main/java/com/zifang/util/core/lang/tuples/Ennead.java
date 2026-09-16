package com.zifang.util.core.lang.tuples;

/**
 * @author zifang
 */
public class Ennead<A, B, C, D, E, F, G, H, I> extends Octet<A, B, C, D, E, F, G, H> {
    protected I i;

    /**
     * Ennead方法。
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
     */
    public Ennead(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        super(a, b, c, d, e, f, g, h);
        this.i = i;
    }

    /**
     * getI方法。
     *
     * @return I类型返回值
     */
    public I getI() {
        return i;
    }

    /**
     * setI方法。
     * * @param i I类型参数
     */
    public void setI(I i) {
        this.i = i;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Ennead{a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + ", g=" + g + ", h=" + h + ", i=" + i + "}";
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
        Ennead<?, ?, ?, ?, ?, ?, ?, ?, ?> ennead = (Ennead<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
        return java.util.Objects.equals(i, ennead.i);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), i);
    }
}