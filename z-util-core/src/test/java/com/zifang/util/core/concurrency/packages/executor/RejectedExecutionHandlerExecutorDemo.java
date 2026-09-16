package com.zifang.util.core.concurrency.packages.executor;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 4.12　处理在执行器中被拒绝的任务
 * <p>
 * 当我们想结束执行器的执行时，调用 shutdown() 方法来表示执行器应当结束。但是，执行器只有等待正在运行的任务或者等待执行的任务结束后，才能真正结束。
 * 如果在shutdown()方法与执行器结束之间发送一个任务给执行器，这个任务会被拒绝，因为这个时间段执行器已不再接受任务了。ThreadPoolExecutor类提供了一套机制，当任务被拒绝时调用这套机制来处理它们。
 * 在本节，我们将学习如何处理执行器中被拒绝的任务，这些任务实现了RejectedExecutionHandler 接口。
 */

/**
 * RejectedExecutionHandlerExecutorDemo类。
 */
public class RejectedExecutionHandlerExecutorDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 10．创建 RejectedTaskController 对象来管理被拒绝的任务。
        RejectedTaskController controller = new RejectedTaskController();
        // 11．调用 Executors 工厂类的 newCachedThreadPool() 方法创建 ThreadPoolExecutor
        // 执行器对象。
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        // 12．设置用于被拒绝的任务的处理程序。
        executor.setRejectedExecutionHandler(controller);
        // 13．创建 3 个任务并发送给执行器。
        System.out.printf("Main: Starting.\n");
        for (int i = 0; i < 3; i++) {
            Task task = new Task("Task" + i);
            executor.submit(task);
        }
        // 14．调用 shutdown() 方法关闭执行器。
        System.out.printf("Main: Shutting down the Executor.\n");
        executor.shutdown();
        // 15．创建另一个任务并发送给执行器。
        System.out.printf("Main: Sending another Task.\n");
        Task task = new Task("RejectedTask");
        executor.submit(task);
        // 16．在控制台输出信息表示程序结束。
        System.out.println("Main: End");
        System.out.printf("Main: End.\n");
    }

    // 1．创建一个名为 RejectedTaskController 的类，并实现 RejectedExecutionHandler
    // 接口，然后实现接口的 rejectedExecution() 方法，在控制台输出已被拒绝的任务的名称和执行器的状态。
    static class RejectedTaskController implements RejectedExecutionHandler {

        @Override
        /**
         * rejectedExecution方法。
         *      * @param r Runnable类型参数
         * @param executor ThreadPoolExecutor类型参数
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.printf("RejectedTaskController: The task %s has been rejected\n", r.toString());
            System.out.printf("RejectedTaskController: %s\n", executor.toString());
            System.out.printf("RejectedTaskController: Terminating: %s\n", executor.isTerminating());
            System.out.printf("RejectedTaksController: Terminated: %s\n", executor.isTerminated());
        }
    }

    // 9．实现范例的主类，创建 Main 主类，并实现 main() 方法。

    // 2．创建一个名为Task的类，并实现Runnable接口。
    static class Task implements Runnable {
        /// 3．声明一个名为name的私有String属性，用来存储任务的名称。
        private String name;

        // 4．实现类的构造器，用来初始化类的属性。

        /**
         * Task方法。
         * * @param name String类型参数
         */
        public Task(String name) {
            this.name = name;
        }

        // 5．实现run()方法。在控制台输出信息表示方法开始执行。
        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println("Task " + name + ": Starting");
            // 6．让线程休眠一段随机时间。
            try {
                long duration = (long) (Math.random() * 10);
                System.out.printf("Task %s: ReportGenerator: Generating a report during %d seconds\n", name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 7．在控制台输出信息表示方法执行结束。
            System.out.printf("Task %s: Ending\n", name);
        }

        // 8．覆盖toString()方法，返回任务的名称。

        /**
         * toString方法。
         *
         * @return String类型返回值
         */
        public String toString() {
            return name;
        }
    }
}


/**
 * 工作原理
 * 通过下面的截图，可以看到范例运行的结果。
 * Java Concurrency Cook Book 4.8
 * 我们可以看到被拒绝的任务，当执行已经关闭，RejectecedTaskController 在控制台输出任务和执行器的信息。
 * 为了处理在执行器中被拒绝的任务，需要创建一个实现RejectedExecutionHandler接口的处理类。这个接口有一个rejectedExecution()方法，其中有两个参数：
 * 一个Runnable对象，用来存储被拒绝的任务；
 * 一个Executor对象，用来存储任务被拒绝的执行器。
 * 被执行器拒绝的每一个任务都将调用这个方法。需要先调用Executor类的 setRejectedExecutionHandler()方法来设置用于被拒绝的任务的处理程序。
 * 更多信息
 * 当执行器接收一个任务并开始执行时，它先检查shutdown()方法是否已经被调用了。如果是，那么执行器就拒绝这个任务。首先，执行器会寻找通过setRejectedExecutionHandler()方法设置的用于被拒绝的任务的处理程序，如果找到一个处理程序，执行器就调用其rejectedExecution()方法；否则就抛出 RejectedExecutionExeption异常。这是一个运行时异常，因此并不需要catch语句来对其进行处理。
 */