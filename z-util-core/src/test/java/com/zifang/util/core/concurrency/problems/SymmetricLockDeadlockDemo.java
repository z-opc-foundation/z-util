package com.zifang.util.core.concurrency.problems;

/**
 * @author zifang Lee (oldratlee at gmail dot com)
 */

/**
 * SymmetricLockDeadlockDemo类。
 */
public class SymmetricLockDeadlockDemo {
    static final Object lock1 = new Object();
    static final Object lock2 = new Object();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new ConcurrencyCheckTask1());
        thread1.start();
        Thread thread2 = new Thread(new ConcurrencyCheckTask2());
        thread2.start();
    }

    private static class ConcurrencyCheckTask1 implements Runnable {
        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println("ConcurrencyCheckTask1 started!");
            while (true) {
                synchronized (lock1) {
                    synchronized (lock2) {
                        System.out.println("Hello1");
                    }
                }
            }
        }
    }

    private static class ConcurrencyCheckTask2 implements Runnable {
        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println("ConcurrencyCheckTask2 started!");
            while (true) {
                synchronized (lock2) {
                    synchronized (lock1) {
                        System.out.println("Hello2");
                    }
                }
            }
        }
    }
}
