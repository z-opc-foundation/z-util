package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于实现两个人直接的数据交换，每个人在完成一定的事务后想与对方交换数据，第一个先拿出数据的人
 * 将一直等待第二个人拿着数据来时，才能彼此交换数据
 */

/**
 * ExchangerTest类。
 */
public class ExchangerTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        final Exchanger<String> exchanger = new Exchanger<String>();
        service.execute(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    String data1 = "zxx";
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "正在把数据" + data1 + "换出去");
                    Thread.sleep((long) (Math.random() * 10000));
                    String data2 = exchanger.exchange(data1);
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "换回的数据为" + data2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        service.execute(new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    String data1 = "lhm";
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "正在把数据" + data1 + "换出去");
                    Thread.sleep((long) (Math.random() * 10000));
                    String data2 = exchanger.exchange(data1);
                    System.out.println("线程" + Thread.currentThread().getName()
                            + "换回的数据为" + data2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        service.shutdown();
    }
}
