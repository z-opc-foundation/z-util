package com.zifang.util.core.concurrency.packages;

/**
 * TraditionalThreadCommunication2类。
 */
public class TraditionalThreadCommunication2 {
    static boolean shouldSub = true;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        TraditionalThreadCommunication2 t = new TraditionalThreadCommunication2();
        t.test();
        t.test2();
    }

    /**
     * test方法。
     */
    public void test() {
        new Thread() {
            /**
             * run方法。
             */
            public void run() {
                for (int i = 0; i < 5; i++) {
                    while (!shouldSub) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int j = 1; j <= 10; j++) {
                        System.out.println("sub: j=" + j + "\t i=" + i);
                    }
                    shouldSub = false;
                    notify();
                }
            }
        }.start();
    }

    /**
     * test2方法。
     */
    public void test2() {
        new Thread() {
            /**
             * run方法。
             */
            public void run() {
                for (int i = 0; i < 5; i++) {
                    while (shouldSub) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int j = 1; j <= 2; j++) {
                        System.out.println("main: j=" + j + "\t i=" + i);
                    }
                    shouldSub = true;
                    notify();
                }
            }
        };
    }
}
