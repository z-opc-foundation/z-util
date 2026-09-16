package com.zifang.util.zex.bust.charpter12.test001;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentratTest类。
 */
public class ReentratTest {
    private static final Lock lock_test001 = new ReentrantLock();
    private static final Lock lock_test002 = new ReentrantLock(true);
    private static final Lock lock_test003 = new ReentrantLock(false);
    private static final Lock lock_test004_1 = new ReentrantLock();
    private static final Lock lock_test004_2 = new ReentrantLock();
    private static final Lock lock_test005 = new ReentrantLock();

    /**
     * test001_innerFunction方法。
     *
     * @return static void类型返回值
     */
    public static void test001_innerFunction() {
        try {
            lock_test001.lock();
            System.out.println(Thread.currentThread().getName() + "获得到锁");
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(Thread.currentThread().getName() + "释放了锁");
            lock_test001.unlock();
        }
    }

    /**
     * test002_innerFunction方法。
     *
     * @return static void类型返回值
     */
    public static void test002_innerFunction() {
        while (true) {
            lock_test002.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " get lock");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock_test002.unlock();
            }
        }
    }

    /**
     * test003_innerFunction方法。
     *
     * @return static void类型返回值
     */
    public static void test003_innerFunction() {
        while (true) {
            lock_test003.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " get lock");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock_test003.unlock();
            }
        }
    }

    /**
     * test004_innerFunction方法。
     * * @param lock1 Lock类型参数
     *
     * @param lock2 Lock类型参数
     * @return static void类型返回值
     */
    public static void test004_innerFunction(Lock lock1, Lock lock2) {
        try {
            lock1.lockInterruptibly();
            Thread.sleep(1000);
            lock2.lockInterruptibly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            lock2.unlock();
            System.out.println(Thread.currentThread().getName() + "正常执行");
        }
    }

    /**
     * test005_innerFunction方法。
     * * @param lock Lock类型参数
     *
     * @return static void类型返回值
     */
    public static void test005_innerFunction(Lock lock) {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                System.out.println("等待前，" + Thread.currentThread().getName());
                Thread.sleep(3000);
                System.out.println("等待后," + Thread.currentThread().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Test
    /**
     * test001方法。
     */
    public void test001() throws InterruptedException {
        new Thread(() -> test001_innerFunction(), "线程1").start();
        new Thread(() -> test001_innerFunction(), "线程2").start();
        Thread.sleep(100000L);
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() throws InterruptedException {
        new Thread(() -> test002_innerFunction(), "线程1").start();
        new Thread(() -> test002_innerFunction(), "线程2").start();
        Thread.sleep(100000L);
    }

    @Test
    /**
     * test003方法。
     */
    public void test003() throws InterruptedException {
        new Thread(() -> test003_innerFunction(), "线程1").start();
        new Thread(() -> test003_innerFunction(), "线程2").start();
        Thread.sleep(100000L);
    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {
        Thread thread1 = new Thread(() -> test004_innerFunction(lock_test004_1, lock_test004_2), "线程1");
        Thread thread2 = new Thread(() -> test004_innerFunction(lock_test004_2, lock_test004_1), "线程2");
        thread1.start();
        thread2.start();
        thread1.interrupt();

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    /**
     * test005方法。
     */
    public void test005() {
        Thread thread1 = new Thread(() -> test005_innerFunction(lock_test005), "线程1");
        Thread thread2 = new Thread(() -> test005_innerFunction(lock_test005), "线程2");
        thread1.start();
        thread2.start();
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
