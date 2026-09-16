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
public class RecordExample1 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        new Thread(() -> {
            while (true) {
                int a = 1;
                int b = 2;

                try {
                    a = 3;           // A
                    b = 1 / 0;       // B
                } catch (Exception e) {

                } finally {
                    if (a == 2) {
                        System.out.println("a = " + a);
                    }
                }
            }
        }).start();

    }
}