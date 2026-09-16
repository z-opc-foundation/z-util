package com.zifang.util.core.concurrency.charpter3;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLockDemo类。
 */
public class ReadWriteLockDemo {

    private static Lock lock = new ReentrantLock();
    private static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Lock readLock = readWriteLock.readLock();
    private static Lock writeLock = readWriteLock.writeLock();

    private int value;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ReadWriteLockDemo readWriteLockDemo = new ReadWriteLockDemo();
        Runnable readRunnable = new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                readWriteLockDemo.handleRead(readLock);
                //readWriteLockDemo.handleRead(lock);
            }
        };

        Runnable writeRunnable = new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                readWriteLockDemo.handleWrite(writeLock, new Random().nextInt());
                //readWriteLockDemo.handleWrite(lock, new Random().nextInt());
            }
        };
        for (int i = 0; i < 18; i++) {
            new Thread(readRunnable).start();
        }
        for (int i = 18; i < 20; i++) {
            new Thread(writeRunnable).start();
        }


    }

    /**
     * handleRead方法。
     * * @param lock Lock类型参数
     *
     * @return Object类型返回值
     */
    public Object handleRead(Lock lock) {
        try {
            lock.lock();
            Thread.sleep(1000);
            return value;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * handleWrite方法。
     * * @param lock Lock类型参数
     *
     * @param index int类型参数
     */
    public void handleWrite(Lock lock, int index) {
        try {
            lock.lock();
            Thread.sleep(1000);
            value = index;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
