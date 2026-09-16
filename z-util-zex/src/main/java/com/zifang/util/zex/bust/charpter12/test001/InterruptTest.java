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
 * InterruptTest类。
 */
public class InterruptTest {
    volatile boolean isStop = false;

    @Test
    /**
     * test0方法。
     */
    public void test0() throws InterruptedException {
        new Thread(() -> {
            while (!isStop) {
                System.out.println("运行------");
            }
            System.out.println("运行结束------");
        }).start();
        Thread.sleep(1);
        isStop = true;
        Thread.sleep(1);
        System.out.println("主线程退出------");
    }

    @Test
    /**
     * test1方法。
     */
    public void test1() throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                while (!isInterrupted()) {
                    System.out.println("运行------");
                }
                System.out.println("运行结束------");
            }
        };

        thread.start();

        Thread.sleep(1);
        thread.interrupt();
        Thread.sleep(1);

        System.out.println("主线程退出------");
    }

    @Test
    /**
     * test2方法。
     */
    public void test2() throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    System.out.println("开始睡眠------");
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                    System.out.println("监听到中断------");
                    e.printStackTrace();
                }
                System.out.println("睡眠结束------");
            }
        };

        thread.start();

        Thread.sleep(1);
        thread.interrupt();
        Thread.sleep(1);

        System.out.println("主线程退出------");
    }
}
