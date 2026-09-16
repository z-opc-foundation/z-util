package com.zifang.util.core.concurrency.packages.executor;

import java.util.concurrent.*;

/**
 * 4.11　在执行器中分离任务的启动与结果的处理
 * <p>
 * 通常情况下，使用执行器来执行并发任务时，将Runnable或Callable任务发送给执行器，并获得Future对象来控制任务。此外，还会碰到如下情形，需要在一个对象里发送任务给执行器，然后在另一个对象里处理结果。对于这种情况，Java并发API提供了 CompletionService 类。
 * CompletionService类有一个方法用来发送任务给执行器，还有一个方法为下一个已经执行结束的任务获取Future对象。从内部实现机制来看，CompletionService类使用 Executor对象来执行任务。这个行为的优势是可以共享CompletionService对象，并发送任务到执行器，然后其他的对象可以处理任务的结果。第二个方法有一个不足之处，它只能为已经执行结束的任务获取Future对象，因此，这些Future对象只能被用来获取任务的结果。
 * 在本节，我们将学习如何使用CompletionService类，在执行器中来分离任务的启动与结果的处理。
 */

/**
 * CompletionServiceExecutorDemo2类。
 */
public class CompletionServiceExecutorDemo2 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 19．调用Executors工厂类的newCachedThreadPool()方法创建ThreadPoolExecutor执行器对象。
        ExecutorService executor = Executors.newCachedThreadPool();
        // 20．创建CompletionService对象，并将上一步创建的executor对象作为构造器的参数。
        CompletionService<String> service = new ExecutorCompletionService<>(executor);
        // 21．创建两个ReportRequest对象，然后创建两个线程Thread对象分别来执行它们。
        ReportRequest faceRequest = new ReportRequest("Face", service);
        ReportRequest onlineRequest = new ReportRequest("Online", service);
        Thread faceThread = new Thread(faceRequest);
        Thread onlineThread = new Thread(onlineRequest);
        // 22．创建1个ReportProcessor对象，然后创建1个线程Thread对象来执行它。
        ReportProcessor processor = new ReportProcessor(service);
        Thread senderThread = new Thread(processor);
        // 23．启动这3个线程。
        System.out.printf("Main: Starting the Threads\n");
        faceThread.start();
        onlineThread.start();
        senderThread.start();
        // 24．等待ReportRequest线程的结束。
        try {
            System.out.printf("Main: Waiting for the report	generators.\n");
            faceThread.join();
            onlineThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 25．调用shutdown() 方法结束执行器，然后调用awaitTermination()方法等待所有的任务执行结束。
        System.out.printf("Main: Shutting down the executor.\n");
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 26．调用 setEnd() 方法，设置 end 属性为 true，来结束 ReportSender 的执行。
        processor.setEnd(true);
        System.out.println("Main: Ends");
    }

    // 1．创建名为 ReportGenerator 的类，并实现 Callable 接口，接口的泛型参数为 String 类型。
    static class ReportGenerator implements Callable<String> {
        /// 2．声明两个私有的String属性，分别命名为sender和title，将用来表示报告的数据。
        private String sender;
        private String title;

        // 3．实现类的构造器，用来初始化这两个属性。

        /**
         * ReportGenerator方法。
         * * @param sender String类型参数
         *
         * @param title String类型参数
         */
        public ReportGenerator(String sender, String title) {
            this.sender = sender;
            this.title = title;
        }

        // 4．实现call()方法。让线程休眠一段随机时间。
        @Override

        /**
         * call方法。
         * @return String类型返回值
         */
        public String call() throws Exception {
            try {
                long duration = (long) (Math.random() * 10);
                System.out.printf("%s_%s: ReportGenerator: Generating a report during %d seconds\n", this.sender, this.title, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 5．生成包含了sender和title属性的字符串并返回该字符串。
            String ret = sender + ": " + title;
            return ret;
        }
    }

    // 6．创建一个名为ReportRequest的类，实现Runnable接口。这个类将模拟请求获取报告。
    static class ReportRequest implements Runnable {
        /// 7．声明一个名为name的私有String属性，用来存储ReportRequest的名称。
        private String name;
        // 8．声明一个名为 service 的私有 CompletionService 属性，这个 CompletionService
        // 接口是泛型接口。在这个示例中，我们采用 String 类作为泛型参数。
        private CompletionService<String> service;

        // 9．实现类的构造器，并初始化这两个属性。

        /**
         * ReportRequest方法。
         * * @param name String类型参数
         *
         * @param service CompletionServiceString类型参数
         */
        public ReportRequest(String name, CompletionService<String> service) {
            this.name = name;
            this.service = service;
        }

        // 10．实现run()方法。创建ReportGenerator对象，然后调用CompletionService对象的submit()方法将ReportGenerator对象发送给CompletionService对象。
        @Override
        /**
         * run方法。
         */
        public void run() {
            ReportGenerator reportGenerator = new ReportGenerator(name, "Report");
            service.submit(reportGenerator);
        }
    }
    // 18．实现范例的主类，创建 Main 主类，并实现 main() 方法。

    // 11．创建名为ReportProcessor的类，并实现Runnable接口。这个类将获取到 ReportGenerator任务的结果。
    static class ReportProcessor implements Runnable {
        /// 12．声明一个名为 service
        /// 的私有CompletionService属性。由于CompletionService接口是一个泛型接口，在这个示例中，我们采用String类作为泛型参数。
        private CompletionService<String> service;
        // 13．声明一个名为end的私有boolean属性。
        private boolean end;

        // 14．实现类的构造器，并初始化这两个属性。

        /**
         * ReportProcessor方法。
         * * @param service CompletionServiceString类型参数
         */
        public ReportProcessor(CompletionService<String> service) {
            this.service = service;
            end = false;
        }

        // 15．实现run()方法。如果end属性值为false，则调用CompletionService接口的poll()
        // 方法，来获取下一个已经完成任务的Future对象；当然，这个任务是采用 CompletionService来完成的。
        @Override
        /**
         * run方法。
         */
        public void run() {
            while (!end) {
                try {
                    Future<String> result = service.poll(20, TimeUnit.SECONDS);
                    // 16．通过调用Future对象的get()方法来获取任务的结果，并在控制台输出这些结果。
                    if (result != null) {
                        String report = result.get();
                        System.out.printf("ReportReceiver: Report Received: %s\n", report);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("ReportSender: End\n");
        }

        // 17．实现setEnd()设置方法，修改end的属性值。

        /**
         * setEnd方法。
         * * @param end boolean类型参数
         */
        public void setEnd(boolean end) {
            this.end = end;
        }
    }
}


/**
 * 工作原理
 * 在范例的主类中，我们调用Executors工厂类的newCachedThreadPool()方法创建了 ThreadPoolExecutor执行器对象。然后，使用这个对象初始化了CompletionService对象，因为完成服务（Completion Service）使用执行器来执行任务。然后，调用ReportRequest 类中的submit()方法，利用“完成服务”来执行任务。
 * 当“完成服务”任务结束，这些任务中一个任务就执行结束了，“完成服务”中存储着Future对象，用来控制它在队列中的执行。
 * 调用poll()方法访问这个队列，查看是否有任务已经完成，如果有，则返回队列中的第一个元素（即一个任务执行完成后的Future对象）。当poll()方法返回Future对象后，它将从队列中删除这个Future对象。在这个示例中，我们在调用poll()方法时传递了两个参数，表示当队列里完成任务的结果为空时，想要等待任务执行结束的时间。
 * 一旦创建了CompletionService对象，还要创建两个ReportRequest对象，用来执行在CompletionService中的两个ReportGenerator任务。ReportProcessor任务则将处理两个被发送到执行器里的ReportRequest 任务所产生的结果。
 * 更多信息
 * CompletionService类可以执行Callable或Runnable类型的任务。在这个范例中，使用的是Callable类型的任务，但是，也可以发送Runnable对象给它。由于Runnable对象不能产生结果，因此CompletionService的基本原则不适用于此。
 * CompletionService类提供了其他两种方法来获取任务已经完成的Future对象。这些方法如下。
 * 1.poll()：无参数的poll()方法用于检查队列中是否有Future对象。如果队列为空，则立即返回null。否则，它将返回队列中的第一个元素，并移除这个元素。
 * 2.take()：这个方法也没有参数，它检查队列中是否有Future对象。如果队列为空，它将阻塞线程直到队列中有可用的元素。如果队列中有元素，它将返回队列中的第一个元素，并移除这个元素。
 */
