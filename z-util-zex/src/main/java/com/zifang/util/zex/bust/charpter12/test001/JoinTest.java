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
 * JoinTest类。
 */
public class JoinTest {
    @Test
    /**
     * test0方法。
     */
    public void test0() {
        Thread th1 = new Thread(() -> {
            try {
                System.out.println("第一个线程开始");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("第一个线程结束");
        });
        Thread th2 = new Thread(() -> {
            try {
                System.out.println("第二个线程开始");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("第二个线程结束");
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        try {
            th1.join();
            th2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("main函数结束");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    /**
     * test1方法。
     */
    public void test1() {
        Thread th1 = new Thread(() -> {
            try {
                System.out.println("第一个线程开始");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("第一个线程结束");
        });
        Thread th2 = new Thread(() -> {
            try {
                System.out.println("第二个线程开始");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("第二个线程结束");
        });
        // 启动两个线程
        th1.start();
        th2.start();
        System.out.println("main函数结束");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
