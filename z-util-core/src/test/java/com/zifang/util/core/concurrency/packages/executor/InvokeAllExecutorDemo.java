package com.zifang.util.core.concurrency.packages.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * InvokeAllExecutorDemo类。
 */
public class InvokeAllExecutorDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 15．通过Executors工厂类的newCachedThreadPool()方法创建一个ThreadPoolExecutor
        /// 执行器对象。
        ExecutorService executor = Executors.newCachedThreadPool();
        // 16．创建一个Task类型的任务列表taskList。创建3个Task任务并将它们添加到任务列表taskList中。
        List<Task> taskList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Task task = new Task(String.valueOf(i));
            taskList.add(task);
        }
        // 17．创建一个 Future 类型的结果列表 resultList。这些对象泛型参数为 Result 类型。
        List<Future<Result>> resultList = null;
        // 18．调用 ThreadPoolExecutor 类的 invokeAll() 方法。这个方法将返回上一步所创建的 Future
        // 类型的列表。
        try {
            resultList = executor.invokeAll(taskList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 19．调用shutdown()方法结束执行器。
        executor.shutdown();
        // 20．在控制台输出任务处理的结果，即Future类型列表中的Result结果。
        System.out.println("Main: Printing the results");

        for (int i = 0; i < resultList.size(); i++) {
            Future<Result> future = resultList.get(i);
            try {
                Result result = future.get();
                System.out.println(result.getName() + ": " + result.getValue());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    // 1．创建一个名为 Result 的类，用来存储范例中并发任务产生的结果。
    static class Result {
        // 2．声明两个私有属性。一个名为 name 的 String 属性，一个名为 value 的 int 属性。
        private String name;
        private int value;

        // 3．实现对应的 get() 和 set() 方法来设置和返回 name 和 value 属性。

        /**
         * getName方法。
         *
         * @return String类型返回值
         */
        public String getName() {
            return name;
        }

        /**
         * setName方法。
         * * @param name String类型参数
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * getValue方法。
         *
         * @return int类型返回值
         */
        public int getValue() {
            return value;
        }

        /**
         * setValue方法。
         * * @param value int类型参数
         */
        public void setValue(int value) {
            this.value = value;
        }
    }

    // 14．实现范例的主类，创建Main主类，并实现main()方法。

    // 4．创建一个名为Task的类，并实现Callable接口，接口的泛型参数为Result类型。
    static class Task implements Callable<Result> {
        /// 5．声明一个名为 name 的私有 String 属性。
        private String name;

        // 6．实现类的构造器，用来初始化类的属性。

        /**
         * Task方法。
         * * @param name String类型参数
         */
        public Task(String name) {
            this.name = name;
        }

        // 7．实现call()方法。在这个范例中，这个方法将返回一个Result类型的对象。
        @Override
        /**
         * call方法。
         * @return Result类型返回值
         */
        public Result call() throws Exception {
            /// 8．在控制台输出表示任务开始的信息。
            System.out.printf("%s: Staring\n", this.name);
            // 9．等待一段随机时间。
            try {
                long duration = (long) (Math.random() * 10);
                System.out.printf("%s: Waiting %d seconds for results.\n", this.name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 10．生成一个int值，准备作为返回Result对象中的int属性，这个int值为5个随机数的总和。
            int value = 0;
            for (int i = 0; i < 5; i++) {
                value += (int) (Math.random() * 100);
            }
            // 11．创建一个Result对象，并用任务的名称和上一步计算的int值来对其进行初始化。
            Result result = new Result();
            result.setName(this.name);
            result.setValue(value);
            // 12．在控制台输出信息表示任务执行结束。
            System.out.println(this.name + ": Ends");
            // 13．返回Result对象。
            return result;
        }
    }
}
