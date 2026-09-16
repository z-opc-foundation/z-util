package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * PhaserDemo1类。
 */
public class PhaserDemo1 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Phaser phaser = new Phaser(2);

        Thread t1 = new Thread(new MyThread(phaser, "one"));
        t1.start();
        Thread t2 = new Thread(new MyThread(phaser, "two"));
        t2.start();

    }
}

class MyThread implements Runnable {
    Phaser phaser = null;
    String info = null;

    /**
     * MyThread方法。
     * * @param phaser Phaser类型参数
     *
     * @param info String类型参数
     */
    public MyThread(Phaser phaser, String info) {
        this.phaser = phaser;
        this.info = info;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        phaser.arriveAndAwaitAdvance();
        System.out.println(info + ": start...");
        if (info.equals("one")) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        phaser.arriveAndAwaitAdvance();
        System.out.println(info + ": end..." + phaser.isTerminated());
        phaser.arriveAndDeregister();
        System.out.println("..." + phaser.isTerminated());
    }

}

