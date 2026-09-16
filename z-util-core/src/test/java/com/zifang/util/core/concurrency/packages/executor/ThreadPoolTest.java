package com.zifang.util.core.concurrency.packages.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolTest类。
 */
public class ThreadPoolTest {

    /**
     * Executors 线程池
     * 1、创建固定大小的线程池
     * 2、创建缓存线程池
     * 3、创建单一线程池（实现线程死掉后重新创建一个线程代替原线程）
     * <p>
     * 关闭线程
     * shutdown()和shutdownNow()
     * <p>
     * 线程池启动定时器
     * 调用ScheduledExecutorService 的 schedule方法，返回的ScheduleFuture对象可以取消任务。
     * 支持间隔重复任务的定时方式，不直接支持绝对定时方式，需要转换成相对时间方式。
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
        //固定线程数
//		ExecutorService threadPool = Executors.newFixedThreadPool(3);
        //缓存线程，任务多自动增加新线程，任务空闲，自动回收线程
//		ExecutorService threadPool = Executors.newCachedThreadPool();
        //单线程，如果当前线程死掉，会自动创建另一个线程接替原线程
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        for (int i = 1; i <= 10; i++) {
            final int task = i;
            threadPool.execute(new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName()
                                + " is looping is " + j + " for task is " + task);
                    }
                }
            });
        }
        System.out.println("all of 10 tasks have committed");
        threadPool.shutdown();
//		threadPool.shutdownNow();

        //创建三个线程池，定时任务，6秒执行定时线程
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
        scheduledThreadPool.schedule(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                synchronized (ThreadPoolTest.class) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("bombing1");
                }
            }
        }, 6, TimeUnit.SECONDS);

        scheduledThreadPool.schedule(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                synchronized (ThreadPoolTest.class) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("bombing2");
                }
            }
        }, 6, TimeUnit.SECONDS);

        scheduledThreadPool.schedule(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("bombing3");
            }
        }, 6, TimeUnit.SECONDS);

        //6秒后执行线程，然后每隔2秒执行一次
        Executors.newScheduledThreadPool(3).scheduleAtFixedRate(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("bombing");
            }
        }, 6, 2, TimeUnit.SECONDS);
    }
}
