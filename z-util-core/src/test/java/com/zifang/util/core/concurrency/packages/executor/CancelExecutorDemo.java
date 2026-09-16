package com.zifang.util.core.concurrency.packages.executor;

import java.util.concurrent.*;

/**
 * 4.9　在执行器中取消任务
 * <p>
 * 使用执行器时，不需要管理线程，只需要实现 Runnable 或 Callable 任务并发送任务给执行器即可。执行器负责创建线程，管理线程池中的线程，当线程不再需要时就销毁它们。有时候，我们可能需要取消已经发送给执行器的任务。在这种情况下，可以使用 Future 接口的 cancel() 方法来执行取消操作。在本节，我们将学习如何使用这个方法来取消已经发送给执行器的任务。
 */

/**
 * CancelExecutorDemo类。
 */
public class CancelExecutorDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 3．通过Executors工厂类的newCachedThreadPool()方法创建一个ThreadPoolExecutor执行器对象。
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        // 4．创建一个新的Task对象。
        Task task = new Task();
        // 5．调用submit()方法将任务发送给执行器。
        System.out.printf("Main: Executing the Task\n");
        Future<String> result = executor.submit(task);
        // 6．让主线程休眠2秒。
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 7．执行器的submit()方法返回名为result的Future对象，调用Future对象的cancel()
        // 方法来取消任务的执行。传递参数true给这个cancel()方法。
        System.out.printf("Main: Canceling the Task\n");
        result.cancel(true);
        // 8．在控制台输出调用isCancelled() 方法和isDone()方法的结果，来验证任务已经被取消和已完成。
        System.out.printf("Main: Cancelled: %s\n", result.isCancelled());
        System.out.printf("Main: Done: %s\n", result.isDone());
        // 9．调用shutdown()方法结束执行器，然后在控制台输出信息表示程序执行结束。
        executor.shutdown();
        System.out.printf("Main: The executor has finished\n");
    }

    // 2．实现范例主类，创建 Main 主类，并实现 main() 方法。

    /// 1．创建一个名为Task的类，并实现Callable接口，接口的泛型参数为String类型。接着实现call()方法，构造一个无限循环，先在控制台输出信息，然后休眠100毫秒。
    static class Task implements Callable<String> {

        @Override
        /**
         * call方法。
         * @return String类型返回值
         */
        public String call() throws Exception {
            while (true) {
                System.out.printf("Task: Test\n");
                Thread.sleep(100);
            }
        }
    }
}

/**
 * 工作原理
 * 如果想取消一个已经发送给执行器的任务，可以使用 Future 接口的 cancel() 方法。根据调用 cancel() 方法时所传递的参数以及任务的状态，这个方法的行为有些不同。
 * 1.如果任务已经完成，或者之前已经被取消，或者由于某种原因而不能被取消，那么方法将返回 false 并且任务也不能取消。
 * 2.如果任务在执行器中等待分配 Thread 对象来执行它，那么任务被取消，并且不会开始执行。如果任务已经在运行，那么它依赖于调用 cancel() 方法时所传递的参数。如果传递的参数为 true 并且任务正在运行，那么任务将被取消。如果传递的参数为 false 并且任务正在运行，那么任务不会被取消。
 */