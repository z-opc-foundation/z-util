package com.zifang.util.core.concurrency.packages;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer类运用
 * 更复杂的日期用 quartz
 */

/**
 * TraditionalTimerTest类。
 */
public class TraditionalTimerTest {

    static int count = 0;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        TraditionalTimerTest t = new TraditionalTimerTest();
        t.test2();
    }

    /**
     * test1方法。
     */
    public void test1() {
        new Timer().schedule(new TimerTask() {

            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("bombing!");
            }

        }, 10000, 3000);

        while (true) {
            System.out.println(new Date().getSeconds());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * test2方法。
     */
    public void test2() {

        //两秒和四秒交替打印 bombing
        class MyTimerTask extends TimerTask {
            @Override
            /**
             * run方法。
             */
            public void run() {
                count = (count + 1) % 2;

                System.out.println("bombing!");
                new Timer().schedule(new MyTimerTask(), 2000 + 2000 * count);
            }
        }

        new Timer().schedule(new MyTimerTask(), 2000);

        while (true) {
            System.out.println(new Date().getSeconds());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
