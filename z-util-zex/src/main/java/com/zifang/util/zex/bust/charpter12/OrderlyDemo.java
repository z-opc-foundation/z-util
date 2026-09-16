package com.zifang.util.zex.bust.charpter12;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.time.LocalDateTime;

/**
 * OrderlyDemo类。
 */
public class OrderlyDemo {

    static int value = 1;
    private static boolean flag = false;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 199; i++) {
            value = 1;
            flag = false;
            Thread thread1 = new DisplayThread();
            Thread thread2 = new CountThread();
            thread1.start();
            thread2.start();
            System.out.println("=========================================================");
            Thread.sleep(10);
        }
    }

    static class DisplayThread extends Thread {
        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println(Thread.currentThread().getName() + " DisplayThread begin, time:" + LocalDateTime.now());
            value = 1024;
            System.out.println(Thread.currentThread().getName() + " change flag, time:" + LocalDateTime.now());
            flag = true;
            System.out.println(Thread.currentThread().getName() + " DisplayThread end, time:" + LocalDateTime.now());
        }
    }

    static class CountThread extends Thread {
        @Override
        /**
         * run方法。
         */
        public void run() {
            if (flag) {
                System.out.println(Thread.currentThread().getName() + " value的值是：" + value + ", time:" + LocalDateTime.now());
                System.out.println(Thread.currentThread().getName() + " CountThread flag is true,  time:" + LocalDateTime.now());
            } else {
                System.out.println(Thread.currentThread().getName() + " value的值是：" + value + ", time:" + LocalDateTime.now());
                System.out.println(Thread.currentThread().getName() + " CountThread flag is false, time:" + LocalDateTime.now());
            }
        }
    }
}
