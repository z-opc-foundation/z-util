package com.zifang.util.core.concurrency.packages;

/**
 * ThreadLocalTest3类。
 */
public class ThreadLocalTest3 {
    ThreadLocal<String> a = new ThreadLocal<>();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ThreadLocalTest3 threadLocalTest3 = new ThreadLocalTest3();
        threadLocalTest3.a();
        threadLocalTest3.b();
    }

    /**
     * a方法。
     */
    public void a() {
        a.set("dsadadasdas");
    }

    /**
     * b方法。
     */
    public void b() {
        System.out.println(a.get());
    }
}
