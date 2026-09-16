package com.zifang.util.core.concurrency.charpter3;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReenterantLock类。
 */
public class ReenterantLock implements Runnable {

    /**
     * ReentrantLock方法。
     * * @param true Object类型参数
     *
     * @return static ReentrantLock lock = new类型返回值
     */
    public static ReentrantLock lock = new ReentrantLock(true);//重入锁
    public static int i = 0;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        ReenterantLock reenterantLock = new ReenterantLock();
        Thread t1 = new Thread(reenterantLock);
        t1.setName("T1");
        Thread t2 = new Thread(reenterantLock);
        t2.setName("T2");
        t1.start();
//        t2.start();
//        t1.join();
//        t2.join();
        System.out.println(i);
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        for (int j = 0; j < 1000; j++) {
            lock.lock();
            try {
                i++;
            } finally {
                System.out.println(Thread.currentThread().getName() + i);
                lock.unlock();
            }
        }
        System.out.println(i);
    }
}
