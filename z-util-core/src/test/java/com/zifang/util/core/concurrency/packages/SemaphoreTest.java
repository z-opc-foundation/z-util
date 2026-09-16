package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Semaphore可以维护当前访问自身的线程个数，并提供了同步机制。
 * 使用Semaphore可以控制同时访问资源的线程个数，例如实现一个文件允许的并发访问个数。
 * <p>
 * 多个等待的线程可以是随机获取优先机会，也可以是按照先来后到的顺序获得机会，这取决于构造Semaphore对象时
 * 传入的参数选项。
 * <p>
 * 单个信号量的Semaphore对象可以实现互斥锁的功能，并且可以是由一个线程获得了“锁”，
 * 再由另一个线程释放锁，这可应用于死锁恢复的一些场合
 */

/**
 * SemaphoreTest类。
 */
public class SemaphoreTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        final Semaphore sp = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            Runnable runnable = new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    try {
                        sp.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("线程：" + Thread.currentThread().getName()
                            + " 进入，当前已有" + (3 - sp.availablePermits()) + "个");
                    try {
                        Thread.sleep((long) (Math.random() * 10000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "即将离开");
                    sp.release();
                    //下面代码有时候执行不准确，以为其没有和上面的代码合成原子
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "已离开，当前已有" + (3 - sp.availablePermits()));
                }
            };
            service.execute(runnable);
        }
    }
}
