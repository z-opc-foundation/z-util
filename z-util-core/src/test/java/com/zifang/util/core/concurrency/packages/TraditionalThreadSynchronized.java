package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TraditionalThreadSynchronized类。
 */
public class TraditionalThreadSynchronized {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        TraditionalThreadSynchronized t = new TraditionalThreadSynchronized();
        t.init();
    }

    private void init() {
        final Outputer outputer = new Outputer();
        new Thread() {
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("infcn.com.cn");
                }
            }
        }.start();

        new Thread() {
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output2("laodahahaha");
                }
            }
        }.start();
    }

    static class Outputer {
        Lock lock = new ReentrantLock();

        private void output(String name) {
            int len = name.length();
            try {
                lock.lock();
                for (int i = 0; i < len; i++) {
                    System.out.print(name.charAt(i));
                }
            } finally {
                lock.unlock();
            }
            System.out.println();
        }

        private void output2(String name) {
            int len = name.length();
            try {
                lock.lock();
                for (int i = 0; i < len; i++) {
                    System.out.print(name.charAt(i));
                }
            } finally {
                lock.unlock();
            }
            System.out.println();
        }

    }
}
