package com.zifang.util.core.concurrency.packages.executor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 4.2　创建线程执行器
 * <p>
 * 使用执行器框架（Executor Framework）的第一步是创建 ThreadPoolExecutor 对象。可以 ThreadPoolExecutor类提供的四个构造器或者使用Executors工厂类来创建 ThreadPoolExecutor 对象。一旦有了执行器，就可以将Runnable或Callable对象发送给它去执行了。
 * 在本节，我们将学习如何使用两种操作来实现一个范例，这个范列将模拟一个Web服务器来应对来自不同客户端的请求。
 */

/**
 * ThreadPoolExecutorDemo类。
 */
public class ThreadPoolExecutorDemo {
    /// 1．实现将被Web服务器执行的任务。创建一个名为 Task 的类，并实现 Runnable 接口。
    static class Task implements Runnable {
        // 2．声明一个名为 initDate 的私有 Date 属性，用来存储任务的创建时间，然后创建一个名为 name 的私有 String
        // 属性，用来存储任务的名称。
        private Date initDate;
        private String name;

        // 3．实现类的构造器，用来初始化这两个属性。

        /**
         * Task方法。
         * * @param name String类型参数
         */
        public Task(String name) {
            initDate = new Date();
            this.name = name;
        }

        /**
         * main方法。
         * * @param args String[]类型参数
         *
         * @return static void类型返回值
         */
        public static void main(String[] args) {
            Server server = new Server();
            for (int i = 0; i < 100; i++) {
                Task task = new Task("Task " + i);
                server.executeTask(task);
            }
            server.endServer();
        }

        // 4．实现 run() 方法。
        @Override
        /**
         * run方法。
         */
        public void run() {
            /// 5．在控制台上输出 initDate 属性和实际时间，即任务的开始时间。
            System.out.printf("%s: Task %s: Created on: %s\n", Thread.currentThread().getName(), name, initDate);
            System.out.printf("%s: Task %s: Started on: %s\n", Thread.currentThread().getName(), name, new Date());
            // 6．将任务休眠一段随机时间。
            try {
                Long duration = (long) (Math.random() * 10);

                System.out.printf("%s: Task %s: Doing a task during %d seconds\n", Thread.currentThread().getName(),
                        name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /// 7．在控制台输入任务的完成时间。
            System.out.printf("%s: Task %s: Finished on: %s\n", Thread.currentThread().getName(), name, new Date());
            // 8．创建一个名为 Server的类，它将执行通过执行器接收到的每一个任务。
        }

        static class Server {
            /// 9．声明一个名为executor的ThreadPoolExecutor属性。
            private ThreadPoolExecutor executor;

            // 10．实现类的构造器，通过 Executors 类来初始化 ThreadPoolExecutor 对象。

            /**
             * Server方法。
             */
            public Server() {
                executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            }

            // 11．实现 executeTask() 方法。它接收一个 Task 对象作为参数，并将 Task
            // 对象发送给执行器。在控制台输出一条信息表示新的任务已经到达。

            /**
             * executeTask方法。
             * * @param task Task类型参数
             */
            public void executeTask(Task task) {

                System.out.printf("Server: A new task has arrived\n");
                // 12．调用执行器的 execute() 方法将任务发送给Task。
                executor.execute(task);
                // 13．在控制台输出一些执行器相关的数据来观察执行器的状态。
                System.out.printf("Server: Pool Size: %d\n", executor.getPoolSize());
                System.out.printf("Server: Active Count: %d\n", executor.getActiveCount());
                System.out.printf("Server: Completed Tasks: %d\n", executor.getCompletedTaskCount());
            }

            /// 14．实现 endServer() 方法。在这个方法里，调用执行器的 shutdown() 方法来结束它的执行。
            /**
             * endServer方法。
             */
            public void endServer() {
                executor.shutdown();
            }
            /// 15．实现范例的主类，创建 Main 主类，并实现 main() 方法。
        }

    }

}

/**
 * 工作原理
 * 这个范例的核心在于 Server 类，这个类创建和使用 ThreadPoolExecutor 执行器来执行任务。
 * 第一个关键点是在 Server 类的构造器中创建 ThreadPoolExecutor 对象。ThreadPoolExecutor 类有4个不同的构造器，但是，由于这些构造器在使用上的复杂性，Java并发API提供 Executors 工厂类来构造执行器和其他相关的对象。虽然可以直接通过 ThreadPoolExecutor 其中之一的构造器来创建 ThreadPoolExecutor 对象，但是推荐使用 Executors 工厂类来创建它。
 * 在这个示例中，通过使用 Executors 工厂类的 newCachedThreadPool() 方法创建了一个缓存线程池。这个方法返回一个 ExecutorService 对象，因此它将被强制转换为 ThreadPoolExecutor 类型，并拥有所有的方法。如果需要执行新任务，缓存线程池就会创建新线程；如果线程所运行的任务执行完成后并且这个线程可用，那么缓存线程池将会重用这些线程。线程重用的优点是减少了创建新线程所花费的时间。然而，新任务固定会依赖线程来执行，因此缓存线程池也有缺点，如果发送过多的任务给执行器，系统的负荷将会过载。
 * 备注：仅当线程的数量是合理的或者线程只会运行很短的时间时，适合采用 Executors 工厂类的 newCachedThreadPool() 方法来创建执行器。
 * 一旦创建了执行器，就可以使用执行器的 execute() 方法来发送 Runnable 或 Callable 类型的任务。这个范例发送实现了 Runnable 接口的 Task类型的对象给执行器。
 * 范例中也打印了一些执行器相关的日志信息，专门使用了如下方法。
 * getPoolSize() ：返回执行器线程池中实际的线程数。
 * getActiveCount() ：返回执行器中正在执行任务的线程数。
 * getCompletedTaskCount() ：返回执行器已经完成的任务数。
 * 执行器以及 ThreadPoolExecutor 类一个重要的特性是，通常需要显示地去结束它。如果不这样做，那么执行器将继续执行，程序也不会结束。如果执行器没有任务可执行了，它将继续等待新任务的到来，而不会结束执行。Java应用程序不会结束直到所有非守护线程结束它们的运行，因此，如果有终止执行器，应用程序将永远不会结束。
 * 为了完成执行器的执行，可以使用 ThreadPoolExecutor 类的 shutdown() 方法。当执行器执行完成所有待运行的任务后，它将结束执行。调用shutdown() 方法之后，如果尝试再发送另一个任务给执行器，任务将被拒绝，并且执行器也将抛出 RejectedExecutionException 异常。
 * 下面的截图展示了范例执行的部分结果。
 * Java Concurrency Cook Book 4.1
 * 当最后一个任务到达服务器时，执行器拥有由100项任务和90个活动线程组成的池。
 * 更多信息
 * ThreadPoolExecutor 类提供了许多方法来获取自身状态的信息。在范例中，已经使用了 getPoolSize() 方法来获取线程池的大小，用getActiveCount() 方法来获取线程池中活动线程的数量，用 getCompletedTaskCount() 方法来获取执行器完成的任务数量。也可以使用getLargestPoolSize() 方法来返回曾经同时位于线程池中的最大线程数。
 * ThreadPoolExecutor 类也提供了结束执行器的相关方法。
 * shutdownNow() ：这个方法会立即关闭执行器。执行器将不再执行那些正在等待执行的任务。这个方法将返回等待执行的任务列表。调用时，正在运行的任务将继续运行，但是这个方法并不等待这些任务完成。
 * isTerminated()：如果调用了shutdown()或shutdownNow()方法，并且执行器完成了关闭的过程，那么这个方法将返回 true 。
 * isShutdown()：如果调用了shutdown()方法，那么这个方法将返回true。
 * awaitTermination(long timeout, TimeUnit unit)：这个方法将阻塞所调用的线程，直到执行器完成任务或者达到所指定的 timeout值。
 * TimeUnit是一个枚举类，有如下的常量：DAYS、HOURS、MICROSECONDS、MILLISECONDS、MINUTES、NANOSECONDS和SECONDS。
 * 备注：如果想等待任务的结束，而不管任务的持续时间，可以使用一个大的超时时间，比如DAYS。
 */
