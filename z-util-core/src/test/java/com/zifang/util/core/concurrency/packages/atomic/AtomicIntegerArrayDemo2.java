package com.zifang.util.core.concurrency.packages.atomic;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * AtomicIntegerArrayDemo2类。
 */
public class AtomicIntegerArrayDemo2 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        AtomicIntegerArray vector = new AtomicIntegerArray(10);
        vector.incrementAndGet(9);    //第9个元素+1
        vector.decrementAndGet(8);    //第8个元素-1
        System.out.println(vector.length());
        for (int i = 0; i < vector.length(); i++) {
            System.out.println(vector.get(i));
        }
    }
}
