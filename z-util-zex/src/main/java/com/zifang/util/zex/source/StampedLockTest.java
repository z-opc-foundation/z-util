package com.zifang.util.zex.source;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock测试类。
 * <p>
 * StampedLock是Java 8引入的一种锁实现，提供了三种锁模式：
 * 写锁、悲观读锁和乐观读锁。
 * 与ReadWriteLock相比，StampedLock支持乐观读模式，
 * 在读多写少的场景下可以提供更好的性能。
 *
 * @author zifang
 * @version 1.0
 */
public class StampedLockTest {
    final static HashMap<String, String> data = new HashMap<>();
    final static StampedLock lock = new StampedLock();


    /**
     * 写入数据，使用写锁。
     *
     * @param key   键
     * @param value 值
     * @return 旧值
     */
    public static Object write(String key, String value) {
        long stamp = lock.writeLock();
        try {
            System.out.println(new Date() + ": 抢占了写锁，开始写操作");
            return data.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock(stamp);
            System.out.println(new Date() + ": 释放了写锁");
        }
        return null;
    }

    /**
     * 对共享数据进行悲观读操作。
     * <p>
     * 悲观读会阻塞其他写操作，适用于读多写多的场景。
     *
     * @param key 键
     * @return 值
     */
    public static Object pessimisticRead(String key) {
        System.out.println(new Date() + ":  进入过写模式，只能悲观读");
        long stamp = lock.readLock();
        try {
            System.out.println(new Date() + ": 获取了读锁");
            return data.get(key);
        } finally {
            System.out.println(new Date() + ": 释放了读锁");
            lock.unlockRead(stamp);
        }
    }

    /**
     * 对共享数据进行乐观读操作。
     * <p>
     * 乐观读不会阻塞写操作，但如果检测到冲突会退化为悲观读。
     * 适用于读多写少的场景。
     *
     * @param key 键
     * @return 值
     */
    public static Object optimisticRead(String key) {
        String value = null;

        long stamp = lock.tryOptimisticRead();

        if (stamp != 0) {
            System.out.println(new Date() + ":  乐观锁的印戳值获取成功");
            value = data.get(key);
        } else {
            System.out.println(new Date() + ":  乐观锁的印戳值获取失败，开始使用悲观读");
            return pessimisticRead(key);
        }

        if (!lock.validate(stamp)) {
            System.out.println(new Date() + ":  乐观读的印戳值已经过期");
            return pessimisticRead(key);
        } else {
            System.out.println(new Date() + ":  乐观读的印戳值没有过期");
            return value;
        }
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        data.put("initKey", "initValue");

        Thread t1 = new Thread(() -> {
            System.out.println(optimisticRead("initKey"));
        }, "读线程1");

        Thread t2 = new Thread(() -> {
            write("key1", "value1");
        }, "写线程1");

        Thread t3 = new Thread(() -> {
            System.out.println(optimisticRead("initKey"));
        }, "读线程2");

        t1.start();
        t1.join();
        t2.start();
        t3.start();
        Thread.sleep(1000);
    }
}
