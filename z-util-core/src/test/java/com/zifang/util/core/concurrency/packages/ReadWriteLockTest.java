package com.zifang.util.core.concurrency.packages;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁：分为读锁和写锁，多个读锁不互斥，读锁与写锁互斥，写锁与写锁互斥，这是由JVM自己控制的，你只要上好
 * 相应的锁即可。如果你的代码中只读数据，可以很多人同时读，但不能同时写，那就上读写锁；如果你的代码修改数据，
 * 只能一个人在写，且不能同时读取，那就上写锁。总之，读的时候上读锁，写的时候上写锁！
 */

/**
 * ReadWriteLockTest类。
 */
public class ReadWriteLockTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        final Queue3 q3 = new Queue3();
        for (int i = 0; i < 3; i++) {
            new Thread() {
                /**
                 * run方法。
                 */
                public void run() {
                    while (true) {
                        q3.get();
                    }
                }
            }.start();

            new Thread() {
                /**
                 * run方法。
                 */
                public void run() {
                    while (true) {
                        q3.put(new Random().nextInt(10000));
                    }
                }
            }.start();
        }
    }

}

class Queue3 {
    private Object data = null;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * get方法。
     */
    public void get() {
        rwl.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to read data!");
            Thread.sleep((long) (Math.random() * 1000));
            System.out.println(Thread.currentThread().getName() + " have read data :" + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * put方法。
     * * @param data Object类型参数
     */
    public void put(Object data) {
        rwl.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to write data!");
            Thread.sleep((long) (Math.random() * 1000));
            this.data = data;
            System.out.println(Thread.currentThread().getName() + " have write data: " + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rwl.writeLock().unlock();
        }
    }
}