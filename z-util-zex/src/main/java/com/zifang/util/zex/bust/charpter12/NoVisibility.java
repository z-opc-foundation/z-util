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
public class NoVisibility {

    private static int n1 = 0;

    private static int number;

    private static void test1() {
        new Thread() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                n1 = 1;
            }
        }.start();

        while (n1 == 0) {
        }
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        test1();

    }

}