package com.zifang.util.core.concurrency.packages.executor.threadpool;

/**
 * Client类。
 */
public class Client {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        ThreadPool pool = new ThreadPool(5);
        pool.execute(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    Thread.sleep(500);
                    System.out.println("执行耗时任务1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        pool.execute(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    Thread.sleep(500);
                    System.out.println("执行耗时任务2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        System.out.println("End");
        pool.destroy();
    }

}
