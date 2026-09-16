package com.zifang.util.core.concurrency.packages.executor;

import java.util.concurrent.*;

/**
 * 4.10　在执行器中控制任务的完成
 * <p>
 * FutureTask 类提供了一个名为 done() 的方法，允许在执行器中的任务执行结束之后，还可以执行一些代码。这个方法可以被用来执行一些后期处理操作，比如：产生报表，通过邮件发送结果或释放一些系统资源。当任务执行完成是受 FutureTask 类控制时，这个方法在内部被 FutureTask类调用。在任务结果设置后以及任务的状态已改变为 isDone之后，无论任务是否被取消或者正常结束，done()方法才被调用。
 * 默认情况下，done()方法的实现为空，即没有任何具体的代码实现。我们可以覆盖 FutureTask 类并实现done()方法来改变这种行为。在本节，我们将学习如何覆盖这个方法，并在任务结束后执行这些代码。
 */

/**
 * DoneExecutorDemo类。
 */
public class DoneExecutorDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 10．调用Executors工厂类的newCachedThreadPool()方法创建一个 ExecutorService 执行器对象。
        ExecutorService executor = Executors.newCachedThreadPool();
        // 11．创建一个数组用来存储5个ResultTask对象。
        ResultTask[] resultTasks = new ResultTask[5];
        // 12．初始化ResultTask对象。在数组的每一个位置上，必须创建 ExecutorTask
        // 对象，然后创建ResultTask对象来使用ExecutorTask对象，最后调用submit()方法将
        // ResultTask任务发送给执行器。
        for (int i = 0; i < 5; i++) {
            ExecutableTask executableTask = new ExecutableTask("Task " + i);
            resultTasks[i] = new ResultTask(executableTask);
            executor.submit(resultTasks[i]);
        }
        // 13．将主线程休眠5秒钟。
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 14．取消已经发送给执行器的所有任务。
        for (int i = 0; i < resultTasks.length; i++) {
            resultTasks[i].cancel(true);
        }
        // 15．通过调用ResultTask对象的get()方法，在控制台上输出还没有被取消的任务结果。
        for (int i = 0; i < resultTasks.length; i++) {
            try {
                if (!resultTasks[i].isCancelled()) {
                    System.out.printf("%s\n", resultTasks[i].get());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // 16．调用shutdown()方法结束执行器。
        executor.shutdown();

    }

    /// 1．创建名为ExecutableTask的类，并实现Callable接口，接口的泛型参数为String类型。
    static class ExecutableTask implements Callable<String> {
        /// 2．声明一个名为name的私有String属性，用来存储任务的名称，用 getName() 方法来返回这个属性值。
        private String name;

        /**
         * ExecutableTask方法。
         * * @param name String类型参数
         */
        public ExecutableTask(String name) {
            this.name = name;
        }

        // 3．实现类的构造器，并初始化任务的名称。

        /**
         * getName方法。
         *
         * @return String类型返回值
         */
        public String getName() {
            return name;
        }

        // 4．实现 call() 方法。将任务休眠一段随机时间，并返回带有任务名称的消息。
        @Override
        /**
         * call方法。
         * @return String类型返回值
         */
        public String call() throws Exception {
            try {
                long duration = (long) (Math.random() * 10);
                System.out.printf("%s: Waiting %d seconds for results.\n", this.name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
            }
            return "Hello, world. I'm " + name;
        }
    }

    // 9．实现范例的主类，创建Main主类，然后实现main()方法。

    // 5．实现一个名为ResultTask的类，并继承FutureTask类。FutureTask类的泛型参数为String类型。
    static class ResultTask extends FutureTask<String> {
        /// 6．声明一个名为name的私有String属性，用来存储任务的名称。
        private String name;

        // 7．实现类构造器。它接收一个Callable对象作为参数，调用父类构造器，并用接收到的任务属性来初始化name属性。

        /**
         * ResultTask方法。
         * * @param callable CallableString类型参数
         */
        public ResultTask(Callable<String> callable) {
            super(callable);
            this.name = ((ExecutableTask) callable).getName();
        }

        // 8．覆盖done()方法。检查isCancelled()方法的返回值，然后根据这个返回值在控制台输出不同的信息。
        @Override
        /**
         * done方法。
         */
        protected void done() {
            if (isCancelled()) {
                System.out.printf("%s: Has been canceled\n", name);
            } else {
                System.out.printf("%s: Has finished\n", name);
            }
        }
    }

}

/**
 * 工作原理
 * 当任务执行结束时， FutureTask类就会调用done()方法。在这个范例中，我们实现了一个Callable类、一个ExecutableTask类以及一个FutureTask类的子类ResultTask，这个子类用来控制ExecutableTask对象的执行。
 * 在创建好返回值以及改变任务状态为isDone之后，FutureTask类就会在内部调用 done()方法。虽然我们无法改变任务的结果值，也无法改变任务的状态，但是可以通过任务来关闭系统资源、输出日志信息、发送通知等。
 */
