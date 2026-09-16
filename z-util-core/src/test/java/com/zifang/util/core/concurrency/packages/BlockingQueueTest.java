package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * BlockingQueueTest类。
 */
public class BlockingQueueTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);

        for (int i = 0; i < 2; i++) {
            new Thread() {
                /**
                 * run方法。
                 */
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep((long) (Math.random() * 1000));
                            System.out.println(Thread.currentThread().getName()
                                    + "准备放数据");
                            queue.put(1);
                            System.out.println(Thread.currentThread().getName()
                                    + "队列目前有" + queue.size() + "个数据");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        new Thread() {
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        //将此处的睡眠时间分别改为100和1000，观察运行结果
                        Thread.sleep((long) (Math.random() * 1000));
                        System.out.println(Thread.currentThread().getName()
                                + "准备取数据");
                        queue.take();
                        System.out.println(Thread.currentThread().getName()
                                + "队列目前有" + queue.size() + "个数据");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
