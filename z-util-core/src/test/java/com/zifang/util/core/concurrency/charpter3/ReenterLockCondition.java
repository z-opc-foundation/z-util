package com.zifang.util.core.concurrency.charpter3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReenterLockCondition类。
 */
public class ReenterLockCondition implements Runnable {

    /**
     * ReentrantLock方法。
     *
     * @return static ReentrantLock lock = new类型返回值
     */
    public static ReentrantLock lock = new ReentrantLock();
    /**
     * lock.newCondition方法。
     *
     * @return static Condition condition =类型返回值
     */
    public static Condition condition = lock.newCondition();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        ReenterLockCondition reenterLockCondition = new ReenterLockCondition();
        Thread thread1 = new Thread(reenterLockCondition);
        thread1.start();
        Thread.sleep(1000);
        System.out.println("lock");
        lock.lock();//搞不懂这里为什么要加上lock呢？
        condition.signal();
        lock.unlock();

    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            System.out.println("1:" + lock.isLocked());
            lock.lock();
            System.out.println("2:" + lock.isLocked());
            condition.await();
            System.out.println("3:" + lock.isLocked());
            System.out.println("Thread is going on");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
