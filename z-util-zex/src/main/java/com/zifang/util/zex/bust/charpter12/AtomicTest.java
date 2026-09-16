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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicTest类。
 */
public class AtomicTest {

    /**
     * AtomicInteger方法。
     * * @param 0 Object类型参数
     *
     * @return volatile static AtomicInteger i = new类型返回值
     */
    public volatile static AtomicInteger i = new AtomicInteger(0);

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {

        Thread thread1 = new Thread(new Runnable() {
            int i = 0;

            @Override
            /**
             * run方法。
             */
            public void run() {
                while (i++ < 10000) {
                    AtomicTest.i.incrementAndGet();
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            int i = 0;

            @Override
            /**
             * run方法。
             */
            public void run() {
                while (i++ < 10000) {
                    AtomicTest.i.incrementAndGet();
                }
            }
        });
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        System.out.println(AtomicTest.i);

    }
}
