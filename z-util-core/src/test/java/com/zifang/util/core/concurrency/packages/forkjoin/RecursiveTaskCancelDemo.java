package com.zifang.util.core.concurrency.packages.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * RecursiveTaskCancelDemo类。
 */
public class RecursiveTaskCancelDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 28．用ArrayGenerator类创建一个容量为1,000的数字数组。
        ArrayGenerator generator = new ArrayGenerator();
        int[] array = generator.generateArray(1000);
        // 29．创建一个TaskManager对象。
        TaskManager manager = new TaskManager();
        // 30．通过默认的构造器创建一个ForkJoinPool对象。
        ForkJoinPool pool = new ForkJoinPool();
        // 31．创建一个Task对象用来处理第28步生成的数组。
        SearchNumberTask task = new SearchNumberTask(array, 0, 1000, 5, manager);
        // 32．调用execute()方法采用异步方式执行线程池中的任务。
        pool.execute(task);
        // 33．调用shutdown()方法关闭线程池。
        pool.shutdown();
        // 34．调用ForkJoinPool类的awaitTermination()方法等待任务执行结束。
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 1．创建一个名为ArrayGenerator的类。这个类将生成一个指定大小的随机整数数组。实现generateArray()方法，它将生成数字数组，接收一个int参数表示数组的大小。
    static class ArrayGenerator {
        /**
         * generateArray方法。
         * * @param size int类型参数
         *
         * @return int[]类型返回值
         */
        public int[] generateArray(int size) {
            int[] array = new int[size];
            Random random = new Random();
            for (int i = 0; i < size; i++) {
                array[i] = random.nextInt(10);
            }
            return array;
        }
    }

    // 2．创建一个名为TaskManager的类。本示例将利用这个类来存储在ForkJoinPool中执行的任务。由于ForkJoinPool和ForkJoinTask类的局限性，将利用TaskManager类来取消ForkJoinPool类中所有的任务。
    static class TaskManager {
        // 3．声明一个名为tasks的对象列表，带有ForkJoinTask泛型参数，并且ForkJoinTask带有Integer泛型参数。
        private List<ForkJoinTask<Integer>> tasks;

        // 4．实现类的构造器，用来初始化任务列表。

        /**
         * TaskManager方法。
         */
        public TaskManager() {
            tasks = new ArrayList<>();
        }

        // 5．实现addTask()方法。增加一个ForkJoinTask对象到任务列表中。

        /**
         * addTask方法。
         * * @param task ForkJoinTaskInteger类型参数
         */
        public void addTask(ForkJoinTask<Integer> task) {
            tasks.add(task);
        }

        // 6．实现cancelTasks()方法。遍历存储在列表中的所有ForkJoinTask对象，然后调用cancel()方法取消之。cancelTasks()方法接收一个要取消剩余任务的ForkJoinTask对象作为参数，然后取消所有的任务。

        /**
         * cancelTasks方法。
         * * @param cancelTask ForkJoinTaskInteger类型参数
         */
        public void cancelTasks(ForkJoinTask<Integer> cancelTask) {
            for (ForkJoinTask<Integer> task : tasks) {
                if (task != cancelTask) {
                    task.cancel(true);
                    ((SearchNumberTask) task).writeCancelMessage();
                }
            }
        }
    }

    // 27．实现范例的主类，创建Main主类，并实现main()方法。

    // 7．实现SearchNumberTask类，并继承RecursiveTask类，RecursiveTask类的泛型参数为Integer类型。这个类将寻找在整数数组元素块中的一个数字。
    static class SearchNumberTask extends RecursiveTask<Integer> {
        // 12．声明一个int常量，并初始化其值为-1。当任务找不到数字时将返回这个常量。
        private final static int NOT_FOUND = -1;
        // 8．声明一个名为array的私有int数组。
        private int[] numbers;
        // 9．声明两个分别名为start和end的私有int属性。这两个属性将决定任务所要处理的数组的元素。
        private int start, end;
        // 10．声明一个名为number的私有int属性，用来存储将要寻找的数字。
        private int number;
        // 11．声明一个名为manager的私有TaskManager属性。利用这个对象来取消所有的任务。
        private TaskManager manager;

        // 13．实现类的构造器，用来初始化它的属性。

        /**
         * SearchNumberTask方法。
         * * @param numbers int[]类型参数
         *
         * @param start   int类型参数
         * @param end     int类型参数
         * @param number  int类型参数
         * @param manager TaskManager类型参数
         */
        public SearchNumberTask(int[] numbers, int start, int end, int number, TaskManager manager) {
            this.numbers = numbers;
            this.start = start;
            this.end = end;
            this.number = number;
            this.manager = manager;
        }

        // 14．实现compute()方法。在控制台输出信息表示任务开始，并输出start和end的属性值。
        @Override
        /**
         * compute方法。
         * @return int类型返回值
         */
        protected Integer compute() {
            System.out.println("Task: " + start + ":" + end);
            // 15．如果start和end属性值的差异大于10（任务必须处理大于10个元素的数组），那么，就调用launchTasks()方法将这个任务拆分为两个子任务。
            int ret;
            if (end - start > 10) {
                ret = launchTasks();
                // 16．否则，寻找在数组块中的数字，调用lookForNumber()方法处理这个任务。
            } else {
                ret = lookForNumber();
            }
            // 17．返回任务的结果。
            return ret;
        }

        // 18．实现lookForNumber()方法。
        private int lookForNumber() {
            // 19．遍历任务所要处理的数组块中的所有元素，将元素中存储的数字和将要寻找的数字进行比较。如果它们相等，就在控制台输出信息表示找到了，并用TaskManager对象的cancelTasks()方法取消所有的任务，然后返回已找到的这个元素所在的位置。
            for (int i = start; i < end; i++) {
                if (numbers[i] == number) {
                    System.out.printf("Task: Number %d found in position %d\n", number, i);
                    manager.cancelTasks(this);
                    return i;
                }
                // 20．在循环体中，将任务休眠1秒钟。
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 21．返回-1表示没有找到元素。
            return NOT_FOUND;
        }

        // 22．实现launchTasks()方法。将这个任务要处理的元素块拆分成两部分，然后创建两个Task对象来处理它们。
        private int launchTasks() {

            int mid = (start + end) / 2;

            SearchNumberTask task1 = new SearchNumberTask(numbers, start, mid, number, manager);

            SearchNumberTask task2 = new SearchNumberTask(numbers, mid, end, number, manager);
            // 23．增加任务到TaskManager对象中。
            manager.addTask(task1);
            manager.addTask(task2);
            // 24．调用fork()方法采用异步方式执行这两个任务。
            task1.fork();
            task2.fork();
            // 25．等待任务结束，如果第一个任务返回的结果不为-1，则返回第一个任务的结果；否则返回第二个任务的结果。
            int returnValue;
            returnValue = task1.join();
            if (returnValue != -1) {
                return returnValue;
            }
            returnValue = task2.join();
            return returnValue;
        }

        // 26．实现writeCancelMessage()方法，在控制台输入信息表示任务已经取消了。

        /**
         * writeCancelMessage方法。
         */
        public void writeCancelMessage() {
            System.out.printf("Task: Cancelled task from %d to %d", start, end);
        }
    }
}
/**
 * 工作原理
 * ForkJoinTask类提供的cancel()方法允许取消一个仍没有被执行的任务，这是非常重要的一点。如果任务已经开始执行，那么调用cancel()
 * 方法也无法取消。这个方法接收一个名为mayInterruptIfRunning的boolean值参数。顾名思义，如果传递true值给这个方法，
 * 即使任务正在运行也将被取消。JavaAPI文档指出，ForkJoinTask类的默认实现，这个属性没有起作用。如果任务还没有开始执行，那么这些任务将被取消
 * 。任务的取消对于已发送到线程池中的任务没有影响，它们将继续执行。
 * Fork／Join框架的局限性在于，ForkJoinPool线程池中的任务不允许被取消。为了克服这种局限性，我们实现了TaskManager类，
 * 它存储发送到线程池中的所有任务，可以用一个方法来取消存储的所有任务。如果任务正在运行或者已经执行结束，那么任务就不能被取消，cancel()
 * 方法返回false值，因此可以尝试去取消所有的任务而不用担心可能带来的间接影响。
 * 这个范例实现在数字数组中寻找一个数字。根据Fork／Join框架的推荐，我们将问题拆分为更小的子问题。由于我们仅关心数字的一次出现，因此，当找到它时，
 * 就会取消其他的所有任务。
 */
