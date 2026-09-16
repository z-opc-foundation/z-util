package com.zifang.util.core.concurrency.packages;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ThreadScopeShareData类。
 */
public class ThreadScopeShareData {

    private static int data = 0;
    private static Map<Thread, Integer> threadData = new HashMap<Thread, Integer>();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            new Thread(new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    data = new Random().nextInt();

                    int d = data;
                    threadData.put(Thread.currentThread(), d);
                    System.out.println(Thread.currentThread().getName()
                            + " has put data of :" + data + "---d:" + d);
                    new A().get();
                    new B().get();
                }
            }).start();
        }
    }

    static class A {
        /**
         * get方法。
         *
         * @return int类型返回值
         */
        public int get() {

            System.out.println("A from " + Thread.currentThread().getName()
                    + " get put data of :" + data + "---d:" + threadData.get(Thread.currentThread()));
            return data;
        }
    }

    static class B {
        /**
         * get方法。
         *
         * @return int类型返回值
         */
        public int get() {
            System.out.println("B from " + Thread.currentThread().getName()
                    + " get put data of :" + data + "---d:" + threadData.get(Thread.currentThread()));
            return data;
        }
    }
}
