package com.zifang.util.zex.source;

/**
 * ThreadLocal测试类。
 * <p>
 * 此类演示了ThreadLocal的用法。
 * ThreadLocal为每个线程提供独立的变量副本，实现线程级别的数据隔离。
 *
 * @author zifang
 * @version 1.0
 */
public class ThreadLocalTest {

    //共享的 ThreadLocal类，里面包裹着线程访问的值
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        ThreadLocalTest test = new ThreadLocalTest();

        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                test.setThreadLocal("1");
                test.getThreadLocal();
            }
        }, "t1").start();

        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                test.setThreadLocal("2");
                test.getThreadLocal();
            }
        }, "t2").start();
    }

    /**
     * 设置ThreadLocal的值。
     *
     * @param value 要设置的值
     */
    public void setThreadLocal(String value) {
        threadLocal.set(value);
    }

    /**
     * 获取ThreadLocal的值。
     */
    public void getThreadLocal() {
        System.out.println(threadLocal.get());
    }
}