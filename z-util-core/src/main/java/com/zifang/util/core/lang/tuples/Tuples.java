package com.zifang.util.core.lang.tuples;

/**
 * 元组工厂类。
 * <p>
 * 提供创建各种元组对象的静态方法。
 *
 * @author zifang
 * @see Unit
 * @see Pair
 * @see Triplet
 */
public class Tuples {

    /**
     * of方法。
     * * @param a A类型参数
     *
     * @return static <A> Unit<A>类型返回值
     */
    public static <A> Unit<A> of(A a) {
        return new Unit<>(a);
    }

    /**
     * of方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @return static <A,B> Pair<A,B>类型返回值
     */
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    /**
     * of方法。
     * * @param a A类型参数
     *
     * @param b B类型参数
     * @param c C类型参数
     * @return static <A,B,C> Triplet<A,B,C>类型返回值
     */
    public static <A, B, C> Triplet<A, B, C> of(A a, B b, C c) {
        return new Triplet<>(a, b, c);
    }
}
