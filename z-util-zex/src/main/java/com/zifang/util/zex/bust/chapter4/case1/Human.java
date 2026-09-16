package com.zifang.util.zex.bust.chapter4.case1;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
class Human {
    String sex;
    String age;

    void eat() {
        System.out.println("吃饭");
    }

    void sleep() {
        System.out.println("睡觉");
    }
}

class Main {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Human human = new Human();
        human.eat();
        human.sleep();
    }
}
