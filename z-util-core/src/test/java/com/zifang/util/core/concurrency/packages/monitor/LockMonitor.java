package com.zifang.util.core.concurrency.packages.monitor;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 监控Lock接口
 * <p>
 * Lock 接口是Java 并发 API提供的最基本的机制来同步代码块。它允许定义临界区。临界区是代码块可以共享资源，但是不能被多个线程同时执行。此机制是通过Lock 接口和 ReentrantLock 类实现的。
 * 在这个指南，你将学习从Lock对象可以获取的信息和如何获取这些信息。
 */

/**
 * LockMonitor类。
 */
public class LockMonitor {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {

        // 11. 创建 MyLock 对象，名为 lock。
        MyLock lock = new MyLock();

        // 12. 创建有5个Thread对象的 array。
        Thread[] threads = new Thread[5];

        // 13. 创建并开始5个线程来执行5个Task对象。
        for (int i = 0; i < 5; i++) {
            Task task = new Task(lock);
            threads[i] = new Thread(task);
            threads[i].start();
        }

        // 14. 创建迭代15次的for循环。
        for (int i = 0; i < 15; i++) {

            // 15. 把锁的拥有者的名字写入操控台。
            System.out.printf("Main: Logging the Lock\n");
            System.out.printf("************************\n");
            System.out.printf("Lock: Owner : %s\n", lock.getOwnerName());

            // 16. 显示锁queued的线程的号码和名字。
            System.out.printf("Lock: Queued Threads: %s\n", lock.hasQueuedThreads()); // 译者注：加上
            // System
            if (lock.hasQueuedThreads()) {
                System.out.printf("Lock: Queue Length: %d\n", lock.getQueueLength());
                System.out.printf("Lock: Queued Threads: ");
                Collection<Thread> lockedThreads = lock.getThreads();
                for (Thread lockedThread : lockedThreads) {
                    System.out.printf("%s ", lockedThread.getName());
                }
                System.out.printf("\n");
            }

            // 17. 显示关于Lock对象的公平性和状态的信息。
            System.out.printf("Lock: Fairness: %s\n", lock.isFair());
            System.out.printf("Lock: Locked: %s\n", lock.isLocked());
            System.out.printf("************************\n");

            // 18. 让线程休眠1秒，并合上类的循环。
            TimeUnit.SECONDS.sleep(1);
        }
    }

    // 1. 创建一个类，名为 MyLock ，扩展 ReentrantLock 类。
    static class MyLock extends ReentrantLock {

        private static final long serialVersionUID = -3923919855732019852L;

        // 2. 实现 getOwnerName() 方法。此方法使用Lock类的保护方法 getOwner()，
        // 返回控制锁的线程（如果存在）的名字。

        /**
         * getOwnerName方法。
         *
         * @return String类型返回值
         */
        public String getOwnerName() {
            if (this.getOwner() == null) {
                return "None";
            }
            return this.getOwner().getName();
        }

        // 3. 实现 getThreads() 方法。此方法使用Lock类的保护方法 getQueuedThreads()，返回在锁里的线程的
        // queued
        // list。

        /**
         * getThreads方法。
         *
         * @return Collection<Thread>类型返回值
         */
        public Collection<Thread> getThreads() {
            return this.getQueuedThreads();
        }
    }

    // 10. 创建例子的主类通过创建一个类，名为 Main 并添加 main()方法。

    // 4. 创建一个类，名为 Task，实现 Runnable 接口.
    static class Task implements Runnable {

        // 5. 声明一个私有 Lock 属性，名为 lock。
        private Lock lock;

        // 6. 实现类的构造函数，初始化它的属性值。

        /**
         * Task方法。
         * * @param lock Lock类型参数
         */
        public Task(Lock lock) {
            this.lock = lock;
        }

        // 7. 实现 run() 方法。创建迭代5次的for循环。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < 5; i++) {

                // 8. 使用lock()方法获取锁，并打印一条信息。
                lock.lock();
                System.out.printf("%s: Get the Lock.\n", Thread.currentThread().getName());

                // 9. 让线程休眠 500 毫秒。使用 unlock() 释放锁并打印一条信息。
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    System.out.printf("%s: Free the Lock.\n", Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}

/**
 * 它是如何工作的…
 * 在这个指南里，你实现的MyLock类扩展了ReentrantLock类来返回信息，除此之外获得不到这些信息 ，因为ReentrantLock 类里的数据都是保护类型的。 通过MyLock类实现的方法：
 * 1.getOwnerName()：只有唯一一个线程可以执行被Lock对象保护的临界区。锁存储了正在执行临界区的线程。此线程会被ReentrantLock类的保护方法 getOwner()返回。 此方法使用 getOwner() 方法来返回线程的名字。
 * 2.getThreads()：当线程正在执行临界区时，其他线程尝试进入临界区就会被放到休眠状态一直到他们可以继续执行为止。ReentrantLock类保护方法getQueuedThreads() 返回 正在等待执行临界区的线程list。此方法返回 getQueuedThreads() 方法返回的结果。
 * 我们还使用了 ReentrantLock 类里实现的其他方法：
 * 1.hasQueuedThreads():此方法返回 Boolean 值表明是否有线程在等待获取此锁
 * 2.getQueueLength(): 此方法返回等待获取此锁的线程数量
 * 3.isLocked(): 此方法返回 Boolean 值表明此锁是否为某个线程所拥有
 * 4.isFair(): 此方法返回 Boolean 值表明锁的 fair 模式是否被激活
 * 更多…
 * ReentrantLock 类还有其他方法也是用来获取Lock对象的信息的：
 * 1.getHoldCount(): 返回当前线程获取锁的次数
 * 2.isHeldByCurrentThread(): 返回 Boolean 值，表明锁是否为当前线程所拥有
 */
