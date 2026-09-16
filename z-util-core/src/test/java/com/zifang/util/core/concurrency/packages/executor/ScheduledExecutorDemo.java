package com.zifang.util.core.concurrency.packages.executor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 4.8　在执行器中周期性执行任务
 * <p>
 * 执行器框架（Executor Framework）提供了 ThreadPoolExecutor 类，通过线程池来执行并发任务从而避免了所有线程的创建操作。当发送一个任务给执行器后，根据执行器的配置，它将尽快地执行这个任务。当任务执行结束后，这个任务就会从执行器中删除；如果想再次执行这个任务，则需要再次发送这个任务到执行器。
 * 但是，执行器框架提供了 ScheduledThreadPoolExecutor 类来执行周期性的任务。在本节，我们将学习如何使用这个类的功能来计划执行周期性的任务。
 */

/**
 * ScheduledExecutorDemo类。
 */
public class ScheduledExecutorDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 6．通过调用Executors工厂类的newScheduledThreadPool()方法创建ScheduledThreadPoolExecutor
        /// 执行器对象，传递 1 作为这个方法的参数。
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 7．在控制台输出实际时间。
        System.out.printf("Main: Starting at: %s\n", new Date());
        // 8．创建一个新的Task对象。
        Task task = new Task("Task");
        // 9．调用scheduledAtFixRate()方法将这个任务发送给执行器。传递给这个方法的参数分别为上一步创建的task对象、数字1、数字2，以及TimeUnit.SECONDS常量。这个方法返回一个用来控制任务状态的ScheduledFuture对象。
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(task, 1, 2, TimeUnit.SECONDS);
        // 10．创建一个10步的循环，在控制台输出任务下一次将要执行的剩余时间。在循环体内，用ScheduledFuture类的getDelay()方法来获取任务下一次将要执行的毫秒数，然后将线程休眠500毫秒。
//		for (int i = 0; i < 10; i++) {
//			System.out.printf("Main: Delay: %d\n", result.getDelay(TimeUnit.MILLISECONDS));
//			// Sleep the thread during 500 milliseconds.
//			try {
//				TimeUnit.MILLISECONDS.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
        // 11．调用 shutdown() 方法结束执行器。
//		executor.shutdown();
        // 12．将线程休眠 5 秒，等待周期性的任务全部执行完成。
//		try {
//			TimeUnit.SECONDS.sleep(5);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
        // 13．在控制台输出信息表示程序结束。
        System.out.printf("Main: Finished at: %s\n", new Date());
    }

    // 5．实现范例的主类，创建 Main 主类，并实现 main() 方法。

    // 1．创建一个名为 Task 的类，并实现 Runnable 接口。
    static class Task implements Runnable {
        /// 2．声明一个名为 name 的私有 String 属性，用来存储任务的名称。
        private String name;

        // 3．实现类的构造器，用来初始化类的属性。

        /**
         * Task方法。
         * * @param name String类型参数
         */
        public Task(String name) {
            this.name = name;
        }

        // 4．实现 run() 方法。在控制台输出实际的时间，用来检验任务将在指定的一段时间内执行。
        @Override
        /**
         * run方法。
         */
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("%s: Starting at : %s\n", name, new Date());
        }
    }
}
/**
 * 工作原理
 * 想要通过执行器框架来执行一个周期性任务时，需要一个ScheduledExecutorService 对象。同创建执行器一样，在Java中推荐使用Executors工厂类来创建 ScheduledExecutorService对象。
 * Executors类就是执行器对象的工厂。在这个例子中，可以使用newScheduledThreadPool()方法来创建一个ScheduledExecutorService对象。这个方法接收一个表示线程池中的线程数来作为参数。在这个范例中，因为仅有一个任务，所以只需要传递数字 1 作为参数即可。
 * 一旦有了可以执行周期性任务的执行器，就可以发送任务给这个执行器。在范例中，我们使用scheduledAtFixedRate()方法发送任务。这个方法接收4个参数，分别为将被周期性执行的任务，任务第一次执行后的延时时间，两次执行的时间周期，以及第2个和第3个参数的时间单位。
 * 这个单位是TimeUnit枚举的常量。TimeUnit是一个枚举类，有如下的常量：DAYS、HOURS、MICROSECONDS、MILLISECONDS、MINUTES、NANOSECONDS和SECONDS。
 * 另一个需要注意的是，两次执行之间的周期是指任务在两次执行开始时的时间间隔。如果有一个周期性的任务需要执行 5 秒钟，但是却让它每 3 秒钟执行一次，那么，在任务执行的过程中将会有两个任务实例同时存在。
 * scheduleAtFixedRate()方法返回一个ScheduledFuture对象，ScheduledFuture接口则扩展了Future接口，于是它带有了定时任务的相关操作方法。ScheduledFuture是一个泛型参数化的接口。
 * 在这个示例中，任务是Runnable对象，并没有泛型参数化，必须通过 ? 符号作为参数来泛型化它们。
 * 我们已经使用过 ScheduledFuture 接口中的一个方法。getDelay()方法返回任务到下一次执行时所要等待的剩余时间。这个方法接收一个TimeUnit 常量作为时间单位。
 * 下面的截图显示了范例的部分运行结果。
 * Java Concurrency Cook Book 4.6
 * 通过控制上面的信息，可以看到任务是每 2 秒执行一次；剩余的延迟时间会每隔 500 毫秒在控制台上输出，这个 500 毫秒则是主线程将被休眠的时间。当关闭执行器时，定时任务将结束执行，然后在控制台上也看不到更多的信息了。
 * 更多信息
 * ScheduledThreadPoolExecutor 类还提供了其他方法来安排周期性任务的运行，比如，scheduleWithFixedRate()方法。这个方法与scheduledAtFixedRate() 方法具有相同的参数，
 * 但是略有一些不同需要引起注意。在 scheduledAtFixedRate() 方法中，第 3 个参数表示任务两次执行开始时间的间隔，而在 schedulledWithFixedDelay () 方法中，第 3 个参数则是表示任务上一次执行结束的时间与任务下一次开始执行的时间的间隔。
 * 也可以配置ScheduledThreadPoolExecutor实现shutdown()方法的行为，默认行为是当调用shutdown()方法后，定时任务就结束了。可以通过ScheduledThreadPoolExecutor类的
 * setContinueExistingPeriodicTasksAfterShutdownPolicy() 方法来改变这个行为，传递参数true给这个方法，这样调用shutdown()方法后，周期性任务仍将继续执行。
 */
