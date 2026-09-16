package com.zifang.util.zex.bust.chapter4.case4;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
class Human8 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Human8 human = new Human8();
        human.handle(1);
        human.handle(1, 2);
        human.handle(1, 2, 3);
    }

    void handle(int d) {
        System.out.println("handle(int d)");
    }

    void handle(int a, int b) {
        System.out.println("handle(int a ,int b)");
    }

    void handle(int... d) {
        System.out.println("int... d");
    }
}
