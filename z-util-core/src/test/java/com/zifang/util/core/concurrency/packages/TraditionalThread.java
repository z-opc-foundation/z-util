package com.zifang.util.core.concurrency.packages;

/**
 * TraditionalThread类。
 */
public class TraditionalThread {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        Thread thread = new Thread() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("1:" + Thread.currentThread().getName());
                    System.out.println("2:" + this.getName());
                }
            }
        };
        thread.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("3:" + Thread.currentThread().getName());
                }
            }
        });
        thread2.start();


        new Thread(new Runnable() {

            //此runnable不会运行，因为 thread的run方法被重写了，掉用不了runnable的方法
            @Override
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("run:" + Thread.currentThread().getName());
                }

            }
        }) {
            /**
             * run方法。
             */
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread:" + Thread.currentThread().getName());
                }
            }

        }.start();
    }

}
