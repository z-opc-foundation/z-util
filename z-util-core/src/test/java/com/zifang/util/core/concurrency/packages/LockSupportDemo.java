package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport类提供了park()和unpark()两个方法来实现线程的阻塞和唤醒,
 * 下面我们就来详解Java多线程编程中LockSupport类的线程阻塞用法: LockSupport是用来创建锁和其他同步类的基本线程阻塞原语。
 * LockSupport中的park() 和 unpark()
 * 的作用分别是阻塞线程和解除阻塞线程，而且park()和unpark()不会遇到“Thread.suspend 和
 * Thread.resume所可能引发的死锁”问题。 因为park() 和 unpark()有许可的存在；调用 park() 的线程和另一个试图将其
 * unpark() 的线程之间的竞争将保持活性。
 */

/**
 * LockSupportDemo类。
 */
public class LockSupportDemo {
    private static Thread mainThread;

    /**
     * LockSupport是可不重入 的，如果一个线程连续2次调用 LockSupport .park()，那么该线程一定会一直阻塞下去。
     * <p>
     * LockSupport 很类似于二元信号量(只有1个许可证可供使用)，如果这个许可还没有被占用，当前线程获取许可并继 续 执行；如果许可已经被占用，当前线 程阻塞，等待获取许可。
     *
     * @param args
     */
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        ThreadA ta = new ThreadA("ta");
        // 获取主线程
        mainThread = Thread.currentThread();

        System.out.println(Thread.currentThread().getName() + " start ta");
        ta.start();

        System.out.println(Thread.currentThread().getName() + " block");
        // 主线程阻塞
        LockSupport.park(mainThread);

        System.out.println(Thread.currentThread().getName() + " continue");
    }

    static class ThreadA extends Thread {

        /**
         * ThreadA方法。
         * * @param name String类型参数
         */
        public ThreadA(String name) {
            super(name);
        }

        /**
         * run方法。
         */
        public void run() {
            System.out.println(Thread.currentThread().getName() + " wakup others");
            // 唤醒“主线程”
            LockSupport.unpark(mainThread);
        }
    }
}

/**
 * LockSupport函数列表
 * // 返回提供给最近一次尚未解除阻塞的 park 方法调用的 blocker 对象，如果该调用不受阻塞，则返回 null。
 * static Object getBlocker(Thread t)
 * // 为了线程调度，禁用当前线程，除非许可可用。
 * static void park()
 * // 为了线程调度，在许可可用之前禁用当前线程。
 * static void park(Object blocker)
 * // 为了线程调度禁用当前线程，最多等待指定的等待时间，除非许可可用。
 * static void parkNanos(long nanos)
 * // 为了线程调度，在许可可用前禁用当前线程，并最多等待指定的等待时间。
 * static void parkNanos(Object blocker, long nanos)
 * // 为了线程调度，在指定的时限前禁用当前线程，除非许可可用。
 * static void parkUntil(long deadline)
 * // 为了线程调度，在指定的时限前禁用当前线程，除非许可可用。
 * static void parkUntil(Object blocker, long deadline)
 * // 如果给定线程的许可尚不可用，则使其可用。
 * static void unpark(Thread thread)
 */
