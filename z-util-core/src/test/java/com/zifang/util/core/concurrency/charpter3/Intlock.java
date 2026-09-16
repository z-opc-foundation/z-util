package com.zifang.util.core.concurrency.charpter3;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Intlock类。
 */
public class Intlock implements Runnable {

    /**
     * ReentrantLock方法。
     *
     * @return static ReentrantLock lock1 = new类型返回值
     */
    public static ReentrantLock lock1 = new ReentrantLock();
    /**
     * ReentrantLock方法。
     *
     * @return static ReentrantLock lock2 = new类型返回值
     */
    public static ReentrantLock lock2 = new ReentrantLock();
    int lock;

    /**
     * Intlock方法。
     * * @param lock int类型参数
     */
    public Intlock(int lock) {
        this.lock = lock;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        Intlock intlock1 = new Intlock(1);
        Intlock intlock2 = new Intlock(2);
        Thread t1 = new Thread(intlock1);
        Thread t2 = new Thread(intlock2);
        t1.start();
        t2.start();
        Thread.sleep(1000);
        //t2被中断，放弃所有的锁，让他t1成功的执行，但是t2却出了问题
        t2.interrupt();
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            if (lock == 1) {
                lock1.lockInterruptibly();
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                lock2.lockInterruptibly();
            } else {
                lock2.lockInterruptibly();
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                lock1.lockInterruptibly();

            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (lock1.isHeldByCurrentThread()) {
                lock1.unlock();
            }
            if (lock2.isHeldByCurrentThread()) {
                lock2.unlock();
            }
            System.out.println(Thread.currentThread().getName() + "退出");
        }
    }
}
