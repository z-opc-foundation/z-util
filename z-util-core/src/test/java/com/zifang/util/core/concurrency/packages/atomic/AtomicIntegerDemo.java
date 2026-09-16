package com.zifang.util.core.concurrency.packages.atomic;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicIntegerDemo类。
 */
public class AtomicIntegerDemo {

    private static AtomicInteger ai = new AtomicInteger();

    //打印出AtomicInteger的valueOffset偏移量 为12

    /**
     * showValueOffset方法。
     * * @param atomicInteger AtomicInteger类型参数
     *
     * @return static void类型返回值
     */
    public static void showValueOffset(AtomicInteger atomicInteger) throws NoSuchFieldException, IllegalAccessException {
        Field field = ai.getClass().getDeclaredField("valueOffset");
        field.setAccessible(true);
        System.out.println("AtomicInteger类内的valueOffset偏移量为：" + field.getLong(ai));
    }

    /**
     * test1方法。
     *
     * @return static void类型返回值
     */
    public static void test1() {

        ai.addAndGet(3);
        ai.incrementAndGet();
        System.out.println(ai.compareAndSet(4, 3));

        ai.getAndDecrement();
        ai.getAndIncrement();
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        test1();

        System.out.println(ai);
    }
}
