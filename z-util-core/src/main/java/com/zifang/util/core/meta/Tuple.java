package com.zifang.util.core.meta;

/**
 * @author zifang
 */
public class Tuple<A, B> {

    private static final Tuple EMPTY = new Tuple<>();

    private A first;
    private B second;

    private Tuple() {
    }

    /**
     * empty方法。
     *
     * @return static <A, B> Tuple<A, B>类型返回值
     */
    public static <A, B> Tuple<A, B> empty() {
        return EMPTY;
    }

    /**
     * of方法。
     * * @param first A类型参数
     *
     * @param second B类型参数
     * @return static <A, B> Tuple<A, B>类型返回值
     */
    public static <A, B> Tuple<A, B> of(A first, B second) {
        Tuple<A, B> tuple = new Tuple<A, B>();
        tuple.setFirst(first);
        tuple.setSecond(second);
        return tuple;
    }

    /**
     * getFirst方法。
     *
     * @return A类型返回值
     */
    public A getFirst() {
        return first;
    }

    /**
     * setFirst方法。
     * * @param first A类型参数
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * getSecond方法。
     *
     * @return B类型返回值
     */
    public B getSecond() {
        return second;
    }

    /**
     * setSecond方法。
     * * @param second B类型参数
     */
    public void setSecond(B second) {
        this.second = second;
    }
}