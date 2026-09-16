package com.zifang.util.zex.source;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * SynchronousQueue同步队列示例类。
 * <p>
 * SynchronousQueue是一种特殊的BlockingQueue，其特点是：
 * 每个插入操作必须等待另一个线程的移除操作，反之亦然。
 * 即put和take操作是同步的，适用于线程间直接传递数据的场景。
 *
 * @author zifang
 * @version 1.0
 */
public class SynchronousQueueExample {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        BlockingQueue<String> q = new SynchronousQueue<>();

        SynchronousQueueProducer producer = new SynchronousQueueProducer(q);
        new Thread(producer).start();

        SynchronousQueueConsumer consumer1 = new SynchronousQueueConsumer(q);
        new Thread(consumer1).start();

        SynchronousQueueConsumer consumer2 = new SynchronousQueueConsumer(q);
        new Thread(consumer2).start();

    }

    /**
     * SynchronousQueue生产者线程。
     */
    static class SynchronousQueueProducer implements Runnable {

        final Random random = new Random();
        protected BlockingQueue<String> blockingQueue;

        /**
         * SynchronousQueueProducer方法。
         * * @param queue BlockingQueueString类型参数
         */
        public SynchronousQueueProducer(BlockingQueue<String> queue) {
            this.blockingQueue = queue;
        }

        @Override
        /**
         * run方法。
         */
        public void run() {
            while (true) {
                try {
                    String data = UUID.randomUUID().toString();
                    System.out.println("Put: " + data);
                    blockingQueue.put(data);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * SynchronousQueue消费者线程。
     */
    static class SynchronousQueueConsumer implements Runnable {

        protected BlockingQueue<String> blockingQueue;

        /**
         * SynchronousQueueConsumer方法。
         * * @param queue BlockingQueueString类型参数
         */
        public SynchronousQueueConsumer(BlockingQueue<String> queue) {
            this.blockingQueue = queue;
        }

        @Override
        /**
         * run方法。
         */
        public void run() {
            while (true) {
                try {
                    String data = blockingQueue.take();
                    System.out.println(Thread.currentThread().getName()
                            + " take(): " + data);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}