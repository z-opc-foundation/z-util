package com.zifang.util.core.concurrency.packages.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * RecursiveTaskExceptionDemo类。
 */
public class RecursiveTaskExceptionDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 11．创建一个名为array并能容纳100个整数的int数组。
        int[] array = new int[100];
        // 12．创建一个Task对象来处理这个数组。
        Task task = new Task(array, 0, 100);
        // 13．通过默认的构造器创建ForkJoinPool对象。
        ForkJoinPool pool = new ForkJoinPool();
        // 14．调用execute()方法在线程池中执行任务。
        pool.execute(task);
        // 15．调用shutdown()方法关闭线程池。
        pool.shutdown();
        // 16．
        // 调用awaitTermination()方法等待任务执行结束。如果想一直等待到任务执行完成，那就传递值1和TimeUnit.DAYS作为参数给这个方法。
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 17.
        // 调用isCompletedAbnormally()方法来检查主任务或者它的子任务之一是否抛出了异常。在这个示例中，在控制台输出信息就表示有异常抛出。调用ForkJoinTask类的getException()方法来获取异常信息。
        if (task.isCompletedAbnormally()) {
            System.out.printf("Main: An exception has ocurred\n");
            System.out.printf("Main: %s\n", task.getException());
        }
    }

    // 1．创建名为Task的类，并继承RecursiveTask类，RecursiveTask类的泛型参数为Integer 类型。
    static class Task extends RecursiveTask<Integer> {
        // 2．声明一个名为array的私有int数组。用来模拟在这个范例中即将处理的数据数组。
        private int[] array;
        // 3．声明两个分别名为start和end的私有int属性。这些属性将决定任务要处理的数组元素。
        private int start, end;

        // 4．实现类的构造器，用来初始化类的属性。

        /**
         * Task方法。
         * * @param array int[]类型参数
         *
         * @param start int类型参数
         * @param end   int类型参数
         */
        public Task(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        // 5．实现任务的compute()方法。由于指定了Integer类型作为RecursiveTask的泛型类型，因此这个方法必须返回一个Integer对象。在控制台输出start和end属性。
        @Override
        /**
         * compute方法。
         * @return int类型返回值
         */
        protected Integer compute() {

            System.out.printf("Task: Start from %d to %d\n", start, end);
            // 6．如果由start和end属性所决定的元素块规模小于10，那么直接检查元素，当碰到元素块的第4个元素（索引位为3）时，就抛出RuntimeException异常。然后将任务休眠1秒钟。
            if (end - start < 10) {
                if ((3 > start) && (3 < end)) {
                    throw new RuntimeException("This task throws an" + "Exception: Task from " + start + " to " + end);
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 7．如果要处理的元素块规模大于或等于10，就拆分这个元素块为两部分，并创建
                // 两个Task对象来处理这两部分，然后调用invokeAll()方法在线程池中执行这两个Task对象。
            } else {

                int mid = (end + start) / 2;
                Task task1 = new Task(array, start, mid);
                Task task2 = new Task(array, mid, end);
                invokeAll(task1, task2);
            }
            // 8．在控制台输出信息，表示任务结束，并输出start和end属性值。
            System.out.printf("Task: End form %d to %d\n", start, end);
            // 9．返回数字0作为任务的结果。
            return 0;
        }
        // 10．实现范例的主类，创建Main主类，并实现main()方法。
    }

}

/**
 * 工作原理
 * 在本节，我们实现的Task类用来处理一个数字数组。它检查要处理的数字块规模是否包含有10个或更多个元素。在这个情况下，Task类拆分这个数字块为两部分，然后创建两个新的Task对象用来处理这两部分。否则，它将寻找位于数组中第4个位置（索引位为3）的元素。如果这个元素位于任务处理块中，它将抛出一个RuntimeException异常。
 * 虽然运行这个程序时将抛出异常，但是程序不会停止。在Main主类中，调用原始任务ForkJoinTask类的isCompletedAbnormally()方法，如果主任务或者它的子任务之一抛出了异常，这个方法将返回true。也可以使用getException()方法来获得抛出的Exception对象。
 * 当任务抛出运行时异常时，会影响它的父任务（发送到ForkJoinPool类的任务），以及父任务的父任务，以此类推。查阅程序的输出结果，将会发现有一些任务没有结束的信息。那些任务的开始信息如下：
 * 1	Task: Starting form 0 to 100
 * 2
 * <p>
 * 3	Task: Starting form 0 to 50
 * 4
 * <p>
 * 5	Task: Starting form 0 to 25
 * 6
 * <p>
 * 7	Task: Starting form 0 to 12
 * 8
 * <p>
 * 9	Task: Starting form 0 to 6
 * 这些任务是那些抛出异常的任务和它的父任务。所有这些任务都是异常结束的。记住一点：在用ForkJoinPool对象和ForkJoinTask对象开发一个程序时，它们是会抛出异常的，如果不想要这种行为，就得采用其他方式。
 */