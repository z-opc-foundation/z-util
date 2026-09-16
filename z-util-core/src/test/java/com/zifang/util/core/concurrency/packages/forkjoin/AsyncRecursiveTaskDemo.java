package com.zifang.util.core.concurrency.packages.forkjoin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * AsyncRecursiveTaskDemo类。
 */
public class AsyncRecursiveTaskDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 18．通过默认的构造器创建ForkJoinPool线程池。
        ForkJoinPool pool = new ForkJoinPool();
        // 19．创建3个FolderProcessor任务，并使用不同的文件夹路径来初始化这些任务。
        FolderProcessor system = new FolderProcessor("C:\\Windows", "log");
        FolderProcessor apps = new FolderProcessor("C:\\Program Files", "log");
        FolderProcessor documents = new FolderProcessor("C:\\Documents And Settings", "log");
        // 20．调用execute()方法执行线程池里的3个任务。
        pool.execute(system);
        pool.execute(apps);
        pool.execute(documents);
        // 21．在控制台上每隔1秒钟输出线程池的状态信息，直到这3个任务执行结束。
        do {
            System.out.printf("******************************************\n");
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
            System.out.printf("******************************************\n");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while ((!system.isDone()) || (!apps.isDone()) || (!documents.

                isDone()));
        // 22．调用shutdown()方法关闭ForkJoinPool线程池。
        pool.shutdown();
        // 23．在控制台输出每一个任务产生的结果的大小。
        List<String> results;
        results = system.join();
        System.out.printf("System: %d files found.\n", results.size());
        results = apps.join();
        System.out.printf("Apps: %d files found.\n", results.size());
        results = documents.join();
        System.out.printf("Documents: %d files found.\n", results.size());
    }

    // 17．实现范例的主类，创建Main主类，并实现main()方法。

    // 1．创建名为FolderProcessor的类，并继承RecursiveTask类，RecursiveTask类的泛型参数为List<String>类型。
    static class FolderProcessor extends RecursiveTask<List<String>> {
        // 2．声明类的serialVersionUID属性。这个元素是必需的，因为RecursiveTask类的父类ForkJoinTask实现了Serializable接口。
        private static final long serialVersionUID = 1L;
        // 3．声明一个名为path的私有String属性，用来存储任务将要处理的文件夹的完整路径。
        private String path;
        // 4．声明一个名为extension的私有String属性，用来存储任务将要查找的文件的扩展名。
        private String extension;

        // 5．实现类的构造器，用来初始化这些属性。

        /**
         * FolderProcessor方法。
         * * @param path String类型参数
         *
         * @param extension String类型参数
         */
        public FolderProcessor(String path, String extension) {
            this.path = path;
            this.extension = extension;
        }

        // 6．实现compute()方法。既然指定了RecursiveTask类泛型参数为List<String>类型，那么，这个方法必须返回一个同样类型的对象。
        @Override
        /**
         * compute方法。
         * @return List<String>类型返回值
         */
        protected List<String> compute() {
            // 7．声明一个名为list的String对象列表，用来存储文件夹中文件的名称。
            List<String> list = new ArrayList<>();
            // 8．声明一个名为tasks的FolderProcessor任务列表，用来存储子任务，这些子任务将处理文件夹中的子文件夹。
            List<FolderProcessor> tasks = new ArrayList<>();
            // 9．获取文件夹的内容。
            File file = new File(path);

            File[] content = file.listFiles();
            // 10．对于文件夹中的每一个元素，如果它是子文件夹，就创建一个新的FolderProcessor对象，然后调用fork()方法采用异步方式来执行它。
            if (content != null) {
                for (int i = 0; i < content.length; i++) {
                    if (content[i].isDirectory()) {
                        FolderProcessor task = new FolderProcessor(content[i].getAbsolutePath(), extension);
                        task.fork();
                        tasks.add(task);
                        // 11．否则，调用checkFile()方法来比较文件的扩展名。如果文件的扩展名与将要搜索的扩展名相同，就将文件的完整路径存储到第7步声明的列表中。
                    } else {
                        if (checkFile(content[i].getName())) {
                            list.add(content[i].getAbsolutePath());
                        }
                    }
                }
                // 12．如果FolderProcessor子任务列表超过50个元素，那么就在控制台输出一条信息表示这种情景。
                if (tasks.size() > 50) {
                    System.out.printf("%s: %d tasks ran.\n", file.getAbsolutePath(), tasks.size());
                }
            }
            // 13．调用addResultsFromTask()辅助方法。它把通过这个任务而启动的子任务返回的结果增加到文件列表中。传递两个参数给这个方法，一个是字符串列表list，一个是FolderProcessor子任务列表tasks。
            addResultsFromTasks(list, tasks);
            // 14．返回字符串列表。
            return list;
        }

        // 15．实现addResultsFromTasks()方法。遍历任务列表中存储的每一个任务，调用join()方法等待任务执行结束，并且返回任务的结果。然后，调用addAll()方法将任务的结果增加到字符串列表中。
        private void addResultsFromTasks(List<String> list, List<FolderProcessor> tasks) {
            for (FolderProcessor item : tasks) {
                list.addAll(item.join());
            }
        }

        // 16．实现checkFile()方法。这个方法检查作为参数而传递进来的文件名，如果是以正在搜索的文件扩展名为结尾，那么方法就返回true，否则就返回false。
        private boolean checkFile(String name) {
            return name.endsWith(extension);
        }
    }
}
/**
 * 工作原理
 * 这个范例的重点在于FolderProcessor类。每一个任务处理一个文件夹中的内容。文件夹中的内容有以下两种类型的元素：
 * 文件；
 * 其他文件夹。
 * 如果主任务发现一个文件夹，它将创建另一个Task对象来处理这个文件夹，调用fork()方法把这个新对象发送到线程池中。fork()方法发送任务到线程池时，如果线程池中有空闲的工作者线程（WorkerThread）或者将创建一个新的线程，那么开始执行这个任务，fork()方法会立即返回，因此，主任务可以继续处理文件夹里的其他内容。对于每一个文件，任务开始比较它的文件扩展名，如果与要搜索的扩展名相同，那么将文件的完整路径增加到结果列表中。
 * 一旦主任务处理完指定文件夹里的所有内容，它将调用join()方法等待发送到线程池中的所有子任务执行完成。join()方法在主任务中被调用，然后等待任务执行结束，并通过compute()方法返回值。主任务将所有的子任务结果进行合并，这些子任务发送到线程池中时带有自己的结果列表，然后通过调用compute()方法返回这个列表并作为主任务的返回值。
 * ForkJoinPool类也允许以异步的方式执行任务。调用execute()方法发送3个初始任务到线程池中。在Main主类中，调用shutdown()方法结束线程池，并在控制台输出线程池中任务的状态及其变化的过程。ForkJoinPool类包含了多个方法可以实现这个目的。参考8.5节来查阅这些方法的详细列表。
 * 更多信息
 * 本范例使用join()方法来等待任务的结束，然后获取它们的结果。也可以使用get()方法以下的两个版本来完成这个目的。
 * get()：如果ForkJoinTask类执行结束，或者一直等到结束，那么get()方法的这个版本则返回由compute()方法返回的结果。
 * get(long timeout, TimeUnit unit)：如果任务的结果未准备好，那么get()方法的  这个版本将等待指定的时间。如果超过指定的时间了，任务的结果仍未准备好，那么这    个方法将返回 null值。TimeUnit是一个枚举类，有如下的常量：DAYS、HOURS、MICROSECONDS、MILLISECONDS、MINUTES、NANOSECONDS和SECONDS。
 * get()方法和join()方法还存在两个主要的区别：
 * join()方法不能被中断，如果中断调用join()方法的线程，方法将抛出InterruptedException异常；
 * 如果任务抛出任何运行时异常，那么 get()方法将返回ExecutionException异常，但是join()方法将返回RuntimeException异常。
 */