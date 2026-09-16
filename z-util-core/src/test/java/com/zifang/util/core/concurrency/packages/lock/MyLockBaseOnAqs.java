package com.zifang.util.core.concurrency.packages.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.stream.IntStream;

/**
 * MyLockBaseOnAqs类。
 */
public class MyLockBaseOnAqs {

    private static int count = 0;
    // 声明同步器
    private final Sync sync = new Sync();

    // 加锁

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        MyLockBaseOnAqs lock = new MyLockBaseOnAqs();

        CountDownLatch countDownLatch = new CountDownLatch(1000);

        IntStream.range(0, 1000).forEach(i -> new Thread(() -> {
            lock.lock();

            try {
                IntStream.range(0, 10000).forEach(j -> {
                    count++;
                });
            } finally {
                lock.unlock();
            }
//            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "tt-" + i).start());

        countDownLatch.await();

        System.out.println(count);
    }

    // 解锁

    /**
     * lock方法。
     */
    public void lock() {
        sync.acquire(1);
    }

    /**
     * unlock方法。
     */
    public void unlock() {
        sync.release(1);
    }

    // 定义一个同步器，实现AQS类
    private static class Sync extends AbstractQueuedSynchronizer {
        // 实现tryAcquire(acquires)方法
        @Override
        /**
         * tryAcquire方法。
         *      * @param acquires int类型参数
         * @return boolean类型返回值
         */
        public boolean tryAcquire(int acquires) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 实现tryRelease(releases)方法
        @Override
        /**
         * tryRelease方法。
         *      * @param releases int类型参数
         * @return boolean类型返回值
         */
        protected boolean tryRelease(int releases) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }
    }
}