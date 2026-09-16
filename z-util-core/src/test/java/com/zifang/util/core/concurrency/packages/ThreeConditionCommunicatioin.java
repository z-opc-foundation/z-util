package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition的功能类是在传统线程技术中的Object.wait和Object.notify的功能。在等待Condition时，
 * 允许发生“虚假唤醒”，这同城作为基础平台语义的让步。对于大多数应用程序，这带来的实际影响很小，因为Condition
 * 应该总是在一个循环中被等待，并测试正被等待的状态声明。某个实现可以随意移除可能的虚假唤醒，但建议应用程序程序员总是假定这些虚假唤醒的可能发生，
 * 隐藏总是一个循环中等待。
 * <p>
 * 三个线程交替执行
 */

/**
 * ThreeConditionCommunicatioin类。
 */
public class ThreeConditionCommunicatioin {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        final Business business = new Business();
        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                for (int i = 0; i < 5; i++) {
                    business.sub2(i);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                for (int i = 0; i < 5; i++) {
                    business.sub3(i);
                }
            }
        }).start();

        for (int i = 0; i < 5; i++) {
            business.main(i);
        }
    }

    //锁不是放在线程里面，而是放在线程索要访问资源中的
    static class Business {
        Lock lock = new ReentrantLock();
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();
        Condition condition3 = lock.newCondition();
        int flag = 1;

        /**
         * sub2方法。
         * * @param i int类型参数
         */
        public void sub2(int i) {
            lock.lock();
            try {
                while (flag != 2) {
                    try {
                        condition2.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 1; j <= 5; j++) {
                    System.out.println("sub2: j=" + j + "\t i=" + i);
                }
                flag = 3;
                condition3.signal();
            } finally {
                lock.unlock();
            }
        }

        /**
         * sub3方法。
         * * @param i int类型参数
         */
        public void sub3(int i) {
            lock.lock();
            try {
                while (flag != 3) {
                    try {
                        condition3.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 1; j <= 10; j++) {
                    System.out.println("sub3: j=" + j + "\t i=" + i);
                }
                flag = 1;
                condition1.signal();
            } finally {
                lock.unlock();
            }
        }

        /**
         * main方法。
         * * @param i int类型参数
         */
        public void main(int i) {
            try {
                lock.lock();
                while (flag != 1) {
                    try {
                        condition1.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 1; j <= 2; j++) {
                    System.out.println("main: j=" + j + "\t i=" + i);
                }
                flag = 2;
                condition2.signal();
            } finally {
                lock.unlock();
            }
        }
    }

}


