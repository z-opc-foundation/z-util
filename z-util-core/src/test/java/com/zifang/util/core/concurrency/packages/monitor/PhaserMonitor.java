package com.zifang.util.core.concurrency.packages.monitor;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * 监控Phaser类
 * <p>
 * Java 并发 API 提供的其中一个最复杂且强大的功能是使用 Phaser 类来执行同步phased任务。当有些任务可以分成步骤执行时，此机制是很有用的。Phaser类提供的同步线程机制是在每个步骤的末端， 所以全部的线程都完成第一步后，才能开始执行第二步。
 * 在这个指南，你将学习如何从Phaser类获取其状态信息。
 */

/**
 * PhaserMonitor类。
 */
public class PhaserMonitor {

    // 1. 创建一个类，名为 Task ，实现 Runnable 接口.
    static class Task implements Runnable {
        // 2. 声明一个私有 int 属性，名为 time。
        private int time;
        // 3. 声明私有 Phaser 属性，名为 phaser.
        private Phaser phaser;

        // 4. 实现类的构造函数，初始其属性值。

        /**
         * Task方法。
         * * @param time int类型参数
         *
         * @param phaser Phaser类型参数
         */
        public Task(int time, Phaser phaser) {
            this.time = time;
            this.phaser = phaser;
        }

        /**
         * main方法。
         * * @param args String[]类型参数
         *
         * @return static void类型返回值
         */
        public static void main(String[] args) throws Exception {
            // 9. 创建新的有3个参与者的 Phaser 对象，名为 phaser。
            Phaser phaser = new Phaser(3);
            // 10. 创建并运行3个线程来执行3个task对象。
            for (int i = 0; i < 3; i++) {
                Task task = new Task(i + 1, phaser);
                Thread thread = new Thread(task);
                thread.start();
            }

            // 11.创建迭代10次的for循环，来学关于phaser对象的信息。
            for (int i = 0; i < 10; i++) {
                // 12. 写关于 registered parties 的信息，phaser的phase，到达的parties,
                // 和未到达的parties 的信息。
                System.out.printf("********************\n");
                System.out.printf("Main: Phaser Log\n");
                System.out.printf("Main: Phaser: Phase: %d\n", phaser.getPhase());
                System.out.printf("Main: Phaser: Registered Parties:%d\n", phaser.getRegisteredParties());
                System.out.printf("Main: Phaser: Arrived Parties:%d\n", phaser.getArrivedParties());
                System.out.printf("Main: Phaser: Unarrived Parties:%d\n", phaser.getUnarrivedParties());
                System.out.printf("********************\n");

                // 13. 让线程休眠1秒，并合上类的循环。
                TimeUnit.SECONDS.sleep(1);
            }
        }

        // 8. 创建例子的主类通过创建一个类，名为 Main 并添加 main()方法。

        // 5. 实现 run() 方法。首先，使用 arrive() 方法指示 phaser 属性任务开始执行了。
        @Override
        /**
         * run方法。
         */
        public void run() {
            phaser.arrive();
            // 6. 写信息到操控台表明阶段一开始，把线程放入休眠几秒，使用time属性来表明，再写信息到操控台表明阶段一结束，并使用
            // phaser 属性的 arriveAndAwaitAdvance() 方法来与剩下的任务同步。
            System.out.printf("%s: Entering phase 1.\n", Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("%s: Finishing phase 1.\n", Thread.currentThread().getName());
            phaser.arriveAndAwaitAdvance();

            // 7. 为第二和第三阶段重复第一阶段的行为。在第三阶段的末端使用 arriveAndDeregister()方法代替
            // arriveAndAwaitAdvance() 方法。
            System.out.printf("%s: Entering phase 2.\n", Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("%s: Finishing phase 2.\n", Thread.currentThread().getName());
            phaser.arriveAndAwaitAdvance();

            System.out.printf("%s: Entering phase 3.\n", Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("%s: Finishing phase 3.\n", Thread.currentThread().getName());
            phaser.arriveAndDeregister();

        }
    }

}

/**
 * 它是如何工作的…
 * 在这个指南，我们在 Task 类实现了 phased 任务。此 phased 任务有3个phases，并使用Phaser接口来与其他Task对象同步。当这些任务正在执行他们的phases时候，主类运行3个任务并打印关于phaser对象的状态信息到操控台。 我们使用以下的方法来获取phaser对象的状态：
 * 1.getPhase():此方法返回phaser 任务的 actual phase
 * 2.getRegisteredParties(): 此方法返回使用phaser对象作为同步机制的任务数
 * 3.getArrivedParties(): 此方法返回已经到达actual phase末端的任务数
 * 4.getUnarrivedParties(): 此方法返回还没到达actual phase末端的任务数
 */