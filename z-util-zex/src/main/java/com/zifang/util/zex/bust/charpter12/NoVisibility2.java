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
public class NoVisibility2 {

    private static int n1 = 0;

    private static int number = 0;

    private static void test1() {
        new Thread() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                number = number + 1;
                int a = number;
                while (number - a != 0) {
                    System.out.println("不一致");
                }
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