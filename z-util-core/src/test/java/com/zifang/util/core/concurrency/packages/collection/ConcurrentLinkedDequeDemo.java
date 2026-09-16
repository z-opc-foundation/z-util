package com.zifang.util.core.concurrency.packages.collection;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 6.2 使用非阻塞式线程安全列表
 * <p>
 * 最基本的集合类型是列表（List）。一个列表包含的元素数量不定，可以在任何位置添加、读取或移除元素。并发列表允许不同的线程在同一时间添加或移除列表中的元素，而不会造成数据不一致。
 * 在本节，将会学到如何在并发程序中使用非阻塞式列表。非阻塞式列表提供了一些操作，如果被执行的操作不能够立即运行（例如，在列表为空时，从列表取出一个元素），方法会抛出异常或返回null。Java 7引入了ConcurrentLinkedDeque类来实现非阻塞式并发列表。
 * 将要实现的范例包括以下两个不同的任务：
 * 1.添加大量的数据到一个列表中；
 * 2.从同一个列表中移除大量的数据。
 */

/**
 * ConcurrentLinkedDequeDemo类。
 */
public class ConcurrentLinkedDequeDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 10．创建ConcurrentLinkedDeque对象，并指定它的泛型参数是String型的。
        ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();
        // 11．创建线程数组threads，它包含100个线程。
        Thread[] threads = new Thread[100];
        // 12．创建100个AddTask对象及其对应的运行线程。将每个线程存放到上一步创建的数组中，然后启动线程。
        for (int i = 0; i < threads.length; i++) {
            AddTask task = new AddTask(list);
            threads[i] = new Thread(task);
            threads[i].start();
        }
        System.out.printf("Main: %d AddTask threads have beenlaunched\n", threads.length);
        // 13．使用join()方法等待线程完成。
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 14．将列表的元素数量打印到控制台。
        System.out.printf("Main: Size of the List: %d\n", list.size());
        // 15．创建100个PollTask对象及其对应的运行线程。将每个线程存放到上一步创建的数组中，然后启动线程。
        for (int i = 0; i < threads.length; i++) {
            PollTask task = new PollTask(list);
            threads[i] = new Thread(task);
            threads[i].start();
        }
        System.out.printf("Main: %d PollTask threads have beenlaunched\n", threads.length);
        // 16．使用join()方法等待线程完成。
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 17．将列表的元素数量打印到控制台。
        System.out.printf("Main: Size of the List: %d\n", list.size());
    }

    // 1．创建一个名为AddTask的类，实现Runnable接口。
    static class AddTask implements Runnable {
        // 2．声明一个私有的ConcurrentLinkedDeque属性list，并指定它的泛型参数是String型的。
        private ConcurrentLinkedDeque<String> list;

        // 3．实现类的构造器来初始化属性。

        /**
         * AddTask方法。
         * * @param list ConcurrentLinkedDequeString类型参数
         */
        public AddTask(ConcurrentLinkedDeque<String> list) {
            this.list = list;
        }

        // 4．实现run()方法。这个方法将10,000个字符串存放到列表中，这些字符串由当前执行任务的线程的名称和数字组成。
        @Override
        /**
         * run方法。
         */
        public void run() {
            String name = Thread.currentThread().getName();
            for (int i = 0; i < 10000; i++) {
                list.add(name + ": Element " + i);
            }
        }
    }

    // 9．创建范例的主类Main，并添加main()方法。

    // 5．创建名为PollTask的类，并实现Runnable接口。
    static class PollTask implements Runnable {
        /// 6．声明一个私有的ConcurrentLinkedDeque属性list，并指定它的泛型参数是String型的。
        private ConcurrentLinkedDeque<String> list;

        // 7．实现类的构造器来初始化属性。

        /**
         * PollTask方法。
         * * @param list ConcurrentLinkedDequeString类型参数
         */
        public PollTask(ConcurrentLinkedDeque<String> list) {
            this.list = list;
        }

        // 8．实现run()方法。这个方法将列表中的10,000个字符串取出，总共取5,000次，每次取两个元素。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < 5000; i++) {
                list.pollFirst();
                list.pollLast();
            }
        }
    }
}
/**
 * 工作原理:
 * 本节使用的泛型参数是String类的ConcurrentLinkedDeque对象，用来实现一个非阻塞式并发数据列表。下面的截屏显示了程序的运行结果。
 * 首先，执行100个AddTask任务将元素添加到ConcurrentLinkedDeque对象list中。每个任务使用add()方法向这个列表中插入10,000个元素。add()方法将新元素添加到列表尾部。当所有任务运行完毕，列表中的元素数量将被打印到控制台。在这一刻，列表中有1,000,000个元素。
 * 接下来，执行100个PollTask任务将元素从列表中移除。每个任务使用pollFirst()和pollLast()方法从列表中移除10,000个元素。pollFirst()方法返回并移除列表中的第一个元素，pollLast()方法返回并移除列表中的最后一个元素。如果列表为空，这些方法返回null。当所有任务运行完毕，列表中的元素数量将被打印到控制台。在这一刻，列表中有0个元素。
 * 使用size()方法输出列表中的元素数量。需要注意的是，这个方法返回的值可能不是真实的，尤其当有线程在添加数据或移除数据时，这个方法需要遍历整个列表来计算元素数量，而遍历过的数据可能已经改变。仅当没有任何线程修改列表时，才能保证返回的结果是准确的。
 */