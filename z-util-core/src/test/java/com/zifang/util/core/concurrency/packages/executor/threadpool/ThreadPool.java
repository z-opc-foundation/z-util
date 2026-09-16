package com.zifang.util.core.concurrency.packages.executor.threadpool;

import java.util.LinkedList;
import java.util.List;

/**
 * ThreadPool类。
 */
public class ThreadPool {

    private static ThreadPool threadPool;
    List<Runnable> taskList = new LinkedList<Runnable>();
    private List<Worker> threadList = new LinkedList<Worker>();

    /**
     * ThreadPool方法。
     * * @param num int类型参数
     */
    public ThreadPool(int num) {
        for (int i = 0; i < num; i++) {
            threadList.add(new Worker());
        }
        for (Worker thread : threadList) {
            thread.start();
        }
    }

    /**
     * destroy方法。
     */
    public void destroy() {
        while (!taskList.isEmpty()) {// 如果还有任务没执行完成，就先睡会吧
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 工作线程停止工作，且置为null
        for (Worker thread : threadList) {
            thread.setDistroy();
        }
    }

    /**
     * execute方法。
     * * @param run Runnable类型参数
     */
    public void execute(Runnable run) {

        synchronized (taskList) {
            taskList.add(run);
            taskList.notify();
        }
    }

    /**
     * takeTask方法。
     *
     * @return Runnable类型返回值
     */
    public Runnable takeTask() {
        synchronized (taskList) {
            System.out.println(Thread.currentThread().getName() + "is running");

            if (taskList.isEmpty()) {
                return null;
            } else {
                System.out.println("is empty?" + taskList.isEmpty());
                return taskList.remove(0);
            }
        }
    }

    private class Worker extends Thread {
        public volatile boolean hasRun = true;

        private void setDistroy() {
            this.hasRun = false;
        }

        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println(Thread.currentThread().getName() + "is initial");

            while (hasRun) {
                Runnable r = takeTask();

                if (r != null) {
                    r.run();
                }
            }
        }
    }
}