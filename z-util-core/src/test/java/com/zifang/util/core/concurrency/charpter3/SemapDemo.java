package com.zifang.util.core.concurrency.charpter3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * SemapDemo类。
 */
public class SemapDemo implements Runnable {

    /**
     * Semaphore方法。
     * * @param 5 Object类型参数
     *
     * @return Semaphore semaphore = new类型返回值
     */
    public Semaphore semaphore = new Semaphore(5);//允许5个线程对这个参数进行同事的访问

    /**
     * SemapDemo方法。
     */
    public SemapDemo() {
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ExecutorService exec = Executors.newFixedThreadPool(5);
        SemapDemo semapDemo = new SemapDemo();
        for (int i = 0; i < 20; i++) {
            exec.submit(semapDemo);
        }

    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            semaphore.acquire();
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getId() + ":done");
            semaphore.release();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // TODO: handle finally clause
        }
    }

}
