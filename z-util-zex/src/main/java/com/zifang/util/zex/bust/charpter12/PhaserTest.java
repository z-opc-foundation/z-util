package com.zifang.util.zex.bust.charpter12;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.util.concurrent.Phaser;

/**
 * PhaserTest类。
 */
public class PhaserTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        Phaser phaser = new Phaser(2) {
            @Override
            /**
             * onAdvance方法。
             *      * @param phase int类型参数
             * @param registeredParties int类型参数
             * @return boolean类型返回值
             */
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("=======phase: " + phase + " finished=============");
                return super.onAdvance(phase, registeredParties);
            }
        };

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 4; j++) {
                    System.out.println(String.format("%s: phase: %d", Thread.currentThread().getName(), j));
                    phaser.arriveAndAwaitAdvance();
                }
            }, "Thread " + i).start();
        }
    }
}
