package com.zifang.util.core.concurrency.packages;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * BlockingQueryCommunication类。
 */
public class BlockingQueryCommunication {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        final Business business = new Business();
        new Thread(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                for (int i = 0; i < 5; i++) {
                    business.sub(i);
                }
            }
        }).start();

        for (int i = 0; i < 5; i++) {
            business.main(i);
        }
    }

    //锁不是放在线程里面，而是放在线程索要访问资源中的
    static class Business {
        BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<Integer>(1);
        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<Integer>(1);

        //匿名构造方法，在所有构造方法之前执行
        {
            Collections.synchronizedMap(null);
            try {
                queue2.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * sub方法。
         * * @param i int类型参数
         */
        public void sub(int i) {
            try {
                queue1.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int j = 1; j <= 10; j++) {
                System.out.println("sub: j=" + j + "\t i=" + i);
            }
            try {
                queue2.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * main方法。
         * * @param i int类型参数
         */
        public void main(int i) {
            try {
                queue2.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int j = 1; j <= 2; j++) {
                System.out.println("main: j=" + j + "\t i=" + i);
            }
            try {
                queue1.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

