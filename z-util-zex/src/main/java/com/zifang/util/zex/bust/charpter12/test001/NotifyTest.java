package com.zifang.util.zex.bust.charpter12.test001;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

/**
 * NotifyTest类。
 */
public class NotifyTest {
    @Test
    /**
     * test1方法。
     */
    public void test1() {
        Object lock = new Object();
        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("线程A等待获取lock锁");
                synchronized (lock) {
                    try {
                        System.out.println("线程A获取了lock锁");
                        Thread.sleep(1000);
                        System.out.println("线程A将要运行lock.wait()方法进行等待");
                        lock.wait();
                        System.out.println("线程A等待结束");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("线程B等待获取lock锁");
                synchronized (lock) {
                    System.out.println("线程B获取了lock锁");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程B将要运行lock.notify()方法进行通知");
                    lock.notify();
                    while (true) {
                    }
                }
            }
        }).start();

        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
