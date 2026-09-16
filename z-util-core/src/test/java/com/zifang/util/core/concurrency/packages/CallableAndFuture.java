package com.zifang.util.core.concurrency.packages;

import java.util.Random;
import java.util.concurrent.*;

/**
 * 创建有返回值的多线程
 */

/**
 * CallableAndFuture类。
 */
public class CallableAndFuture {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<String> future = threadPool.submit(new Callable<String>() {
            @Override
            /**
             * call方法。
             * @return String类型返回值
             */
            public String call() throws Exception {
                Thread.sleep(2000);
                return "hello";
            }
        });
        System.out.println("等待结果");
        try {
//			String result = future.get(1, TimeUnit.SECONDS);	//超时时间，如果规定时间内没有返回结果则 报错java.spi.concurrent.TimeoutException
            String result = future.get();
            System.out.println("结果是：" + result);
            threadPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
//		} catch (TimeoutException e) {
//			e.printStackTrace();
        }


        ExecutorService threadPool2 = Executors.newFixedThreadPool(10);

        //同时运行多个任务，那个任务先返回数据，就先获取该数据
        CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool2);

        for (int i = 1; i <= 10; i++) {
            final int seq = i;
            completionService.submit(new Callable<Integer>() {
                @Override
                /**
                 * call方法。
                 * @return int类型返回值
                 */
                public Integer call() throws Exception {
                    Thread.sleep(new Random().nextInt(5000));
                    return seq;
                }
            });
        }

        for (int i = 1; i <= 10; i++) {
            try {
                Future<Integer> future2 = completionService.take();
                System.out.println(future2.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        threadPool2.shutdown();
    }
}
