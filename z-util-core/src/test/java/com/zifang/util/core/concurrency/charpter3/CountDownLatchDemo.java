package com.zifang.util.core.concurrency.charpter3;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CountDownLatchDemo类。
 */
public class CountDownLatchDemo implements Runnable {

    /**
     * CountDownLatch方法。
     * * @param 10 Object类型参数
     *
     * @return static CountDownLatch end = new类型返回值
     */
    public static CountDownLatch end = new CountDownLatch(10);
    /**
     * CountDownLatchDemo方法。
     *
     * @return static CountDownLatchDemo demo = new类型返回值
     */
    public static CountDownLatchDemo demo = new CountDownLatchDemo();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            exec.submit(demo);
        }

        end.await();
        System.out.println("Fire!");
        exec.shutdown();

    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            Thread.activeCount();
            Thread.sleep(new Random().nextInt(10) * 1000);
            System.out.println("check complete");
            end.countDown();
        } catch (Exception e) {

        }
    }

}
