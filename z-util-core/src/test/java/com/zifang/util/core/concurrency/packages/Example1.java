package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Example1类。
 */
public class Example1 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(16);
        for (int i = 0; i < 4; i++) {
            new Thread(new Runnable() {

                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    while (true) {
                        try {
                            String log = queue.take();
                            parseLog(log);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
        }
        for (int i = 0; i < 16; i++) {
            final String log = "" + (i + 1);
            try {
                queue.put(log);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * parseLog方法。
     * * @param log String类型参数
     *
     * @return static void类型返回值
     */
    public static void parseLog(String log) {
        System.out.println(log + ":" + (System.currentTimeMillis()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
