package com.zifang.util.core.concurrency.packages.executor;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 4.7　在执行器中延时执行任务
 * <p>
 * 执行器框架（Executor Framework）提供了 ThreadPoolExecutor 类并采用线程池来执行 Callable 和 Runnable 类型的任务，采用线程池可以避免所有线程的创建操作而提高应用程序的性能。当发送一个任务给执行器时，根据执行器的相应配置，任务将尽可能快地被执行。但是，如果并不想让任务马上被执行，而是想让任务在过一段时间后才被执行，或者任务能够被周期性地执行。为了达到这个目的，执行器框架提供了ScheduledThreadPoolExecutor 类。
 * 在本节，我们将学习如何创建 ScheduledThreadPoolExecutor 执行器，以及如何使用它在经过一个给定的时间后开始执行任务。
 */

/**
 * ScheduledThreadPoolExecutorDemo类。
 */
public class ScheduledThreadPoolExecutorDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 6．通过Executors工厂类的newScheduledThreadPool()方法创建一个
        /// ScheduledThreadPoolExecutor 执行器，并传递 1 作为参数。
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        // 7．初始化一些任务（在我们的示例中是 5 个），然后通过ScheduledThreadPoolExecutor 实例的
        // schedule() 方法来启动这些任务。
        System.out.printf("Main: Starting at: %s\n", new Date());
        for (int i = 0; i < 5; i++) {
            Task task = new Task("Task " + i);
            executor.schedule(task, i + 1, TimeUnit.SECONDS);
        }
        // 8．调用执行器的 shutdown() 方法来结束执行器。
        executor.shutdown();
        // 9．调用执行器的 awaitTermination() 方法等待所有任务结束。
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 10．在控制台输出信息表示程序执行结束的时间。
        System.out.printf("Main: Ends at: %s\n", new Date());
    }

    // 5．实现范例的主类，创建 Main 主类，并实现 main() 方法。

    // 1．创建一个名为Task的类，并实现Callable接口，接口的泛型参数为String类型。
    static class Task implements Callable<String> {
        /// 2．声明一个名为name的私有String属性，用来存储任务的名称。
        private String name;

        // 3．实现类的构造器，并初始化 name 属性。

        /**
         * Task方法。
         * * @param name String类型参数
         */
        public Task(String name) {
            this.name = name;
        }

        // 4．实现call()方法。在控制台输出实际的时间，并返回一个文本信息，比如“Hello，world”。

        /**
         * call方法。
         *
         * @return String类型返回值
         */
        public String call() throws Exception {
            System.out.printf("%s: Starting at : %s\n", name, new Date());
            return "Hello, world";
        }
    }
}

/**
 * 工作原理
 * 这个范例的关键点在于 Main 主类和 ScheduledThreadPoolExecutor 执行器的管理。虽然可以通过 ThreadPoolExecutor 类来创建定时执行器，但是在Java并发API中则推荐利用 Executors 工厂类来创建。在这个范例中，必须使用 newScheduledThreadPool() 方法，并且传递数字 1 作为方法的参数，这个参数就是线程池里拥有的线程数。
 * 为了在定时执行器中等待一段给定的时间后执行一个任务，需要使用 schedule() 方法。这个方法接收如下的参数：
 * 1.即将执行的任务；
 * 2.任务执行前所要等待的时间；
 * 3.等待时间的单位，由 TimeUnit 类的一个常量来指定。
 * 在这个示例中，每个任务将等待 N 秒（TimeUnit.SECONDS），这个 N 值则等于任务在数组中的位置加 1。
 * 备注：如果想在一个给定的时间点来定时执行任务，那就需要计算这个给定时间点和当前时间的差异值，然后用这个差异值作为任务的延迟值。
 * 通过下面的截图，可以看到范例运行的部分结果。
 * Java Concurrency Cook Book 4.5
 * 从结果可知，每隔 1 秒钟就有一个任务开始执行；这是因为所有的任务被同时发送到执行器，但每个任务都比前一个任务延迟了 1 秒钟。
 * 更多信息
 * 也可以使用Runnable接口来实现任务，因为ScheduledThreadPoolExecutor类的 schedule()方法可以同时接受这两种类型的任务。
 * 虽然ScheduledThreadPoolExecutor 类是 ThreadPoolExecutor 类的子类，因而继承了 ThreadPoolExecutor 类所有的特性。但是，Java推荐仅在开发定时任务程序时采用 ScheduledThreadPoolExecutor 类。
 * 最后，在调用shutdown()方法而仍有待处理的任务需要执行时，可以配置 ScheduledThreadPoolExecutor的行为。默认的行为是不论执行器是否结束，待处理的任务仍将被执行。但是，通过调用ScheduledThreadPoolExecutor类的 setExecuteExisting
 * DelayedTasksAfterShutdownPolicy()方法则可以改变这个行为。传递false参数给这个方法，执行shutdown()方法后，待处理的任务将不会被执行。
 */