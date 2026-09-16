package com.zifang.util.core.concurrency.packages.atomic;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 当你实现一个多个线程共享一个或者多个对象的并发应用时，你就要使用像锁或者同步关键词（例如synchronized）来对他们的属性的访问进行保护，来避免并发造成的数据不一致的错误。
 * 但是这些机制会有以下一些缺点：
 * 死锁(dead lock)：例如：当一个线程等待一个锁的时候，会被阻塞，而这个锁被其他线程占用并且永不释放。这种情况就是死锁，程序在这种情况下永远都不会往下执行。
 * 即使只有一个线程在访问共享对象，它也要执行必要的获取锁和释放锁的代码。
 * CAS(compare-and-swap)操作为并发操作对象的提供更好的性能，CAS操作通过以下3个步骤来实现对变量值得修改：
 * 1.获取当前内存中的变量的值
 * 2.用一个新的临时变量(temporal variable)保存改变后的新值
 * 3.如果当前内存中的值等于变量的旧值，则将新值赋值到当前变量；否则不进行任何操作
 * 对于这个机制，你不需要使用任何同步机制，这样你就避免了 deadlocks，也获得了更好的性能。这种机制能保证多个并发线程对一个共享变量操作做到最终一致。
 * Java 在原子类中实现了CAS机制。这些类提供了compareAndSet() 方法；这个方法是CAS操作的实现和其他方法的基础。
 * Java 中还引入了原子Array，用来实现Integer类型和Long类型数组的操作。在这个指南里，你将要学习如何使用AtomicIntegerArray 类来操作原子 arrays。
 */

/**
 * AtomicIntegerArrayDemo类。
 */
public class AtomicIntegerArrayDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        // 10.声明一个常量，名为 THREADS，分配它的值为 100。创建一个有1，000个元素的 AtomicIntegerArray 对象。
        final int THREADS = 100;
        AtomicIntegerArray vector = new AtomicIntegerArray(1000);
        vector.set(5, 100);

        // 11. 创建一个 Incrementer 任务来操作之前创建的原子 array。
        Incrementer incrementer = new Incrementer(vector);

        // 12.创建一个 Decrementer 任务来操作之前创建的原子 array。
        Decrementer decrementer = new Decrementer(vector);

        // 13.创建2个array 分别存储 100 个Thread 对象。
        Thread[] threadIncrementer = new Thread[THREADS];
        Thread[] threadDecrementer = new Thread[THREADS];

        // 14.创建并运行 100 个线程来执行 Incrementer 任务和另外 100 个线程来执行 Decrementer
        // 任务。把线程储存入之前创建的arrays内。
        for (int i = 0; i < THREADS; i++) {
            threadIncrementer[i] = new Thread(incrementer);
            threadDecrementer[i] = new Thread(decrementer);

            threadIncrementer[i].start();
            threadDecrementer[i].start();
        }
        // 15.使用 join() 方法来等待线程的完结。
        for (int i = 0; i < 100; i++) {
            try {
                threadIncrementer[i].join();
                threadDecrementer[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 16.把原子array里非0的元素写入操控台。使用 get() 方法来获取原子 array 元素。
        for (int i = 0; i < vector.length(); i++) {
            if (vector.get(i) != 0) {
                System.out.println("Vector[" + i + "] : " + vector.get(i));
            }
        }

        // 17.在操控台写个信息表明例子结束。
        System.out.println("Main: End of the example");
    }

    // 1.创建一个类，名为 Incrementer，并实现 Runnable 接口。
    static class Incrementer implements Runnable {

        // 2.声明一个私有 AtomicIntegerArray 属性，名为 vector，用来储存一个整数 array。
        private AtomicIntegerArray vector;

        // 3.实现类的构造函数，初始化它的属性值。

        /**
         * Incrementer方法。
         * * @param vector AtomicIntegerArray类型参数
         */
        public Incrementer(AtomicIntegerArray vector) {
            this.vector = vector;
        }

        // 4.实现 run() 方法。使用 getAndIncrement() 方操作array里的所有元素。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < vector.length(); i++) {
                vector.getAndIncrement(i);
            }
        }
    }

    // 9.我们创建一个示例来进行示范，创建一个类，名为 Main 并添加 main()方法。

    // 5.创建一个类，名为 Decrementer，并实现 Runnable 接口。
    static class Decrementer implements Runnable {

        // 6.声明一个私有 AtomicIntegerArray 属性，名为 vector，用来储存一个整数 array。
        private AtomicIntegerArray vector;

        // 7.实现类的构造函数，初始化它的属性值。

        /**
         * Decrementer方法。
         * * @param vector AtomicIntegerArray类型参数
         */
        public Decrementer(AtomicIntegerArray vector) {
            this.vector = vector;
        }

        // 8.实现 run() 方法。使用 getAndDecrement() 方法操作array里的所有元素。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < vector.length(); i++) {
                vector.getAndDecrement(i);
            }
        }
    }
}

/**
 * 它是怎么工作的…
 * 在这个例子里，你实现了2个不同的任务来操作 AtomicIntegerArray 对象：
 * Incrementer task: 这个类使用getAndIncrement()方法array里的全部元素 +1
 * Decrementer task: 这个类使用getAndDecrement()方法array里的全部元素 -1
 * 在 Main 类，你创建了有1000个元素的 AtomicIntegerArray，然后你执行了100次 Incrementer 和100次 Decrementer 任务。在任务结束后，如果没有出现任何数据不一致错误，那么array的全部元素的值都为0。如果你运行这个任务，由于全部元素都是0，你只会看到程序在操控台只写了结束信息。
 * 更多…
 * 如今，Java仅提供了另一个原子 array类。它是 AtomicLongArray 类，与 AtomicIntegerArray 类提供了相同的方法。
 * 这些类的一些其他有趣的方法有：
 * get(int i): 返回array中第i个位置上的值
 * set(int I, int newValue): 设置array中第i个位置上的值为newValue
 */