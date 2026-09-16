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
class Human2 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Human2 human = new Human2();
        human.handle(true);
        human.handle((short) 1);
        human.handle((byte) 1);
        human.handle('c');
        human.handle((char) 99);
        human.handle(1);
        human.handle(1L);
        human.handle(1.1F);
        human.handle(1.1D);
    }

    void handle(boolean a) {
        System.out.println("#boolean:" + a);
    }

    void handle(byte a) {
        System.out.println("#byte:" + a);
    }

    void handle(short a) {
        System.out.println("#short:" + a);
    }

    void handle(char a) {
        System.out.println("#char:" + a);
    }

    void handle(int a) {
        System.out.println("#int:" + a);
    }

    void handle(long a) {
        System.out.println("#long:" + a);
    }

    void handle(float a) {
        System.out.println("#float:" + a);
    }

    void handle(double a) {
        System.out.println("#double:" + a);
    }

    void handle(Boolean a) {
        System.out.println("#Boolean:" + a);
    }

    void handle(Byte a) {
        System.out.println("#Byte:" + a);
    }

    void handle(Short a) {
        System.out.println("#Short:" + a);
    }

    void handle(Character a) {
        System.out.println("#Character:" + a);
    }

    void handle(Integer a) {
        System.out.println("#Integer:" + a);
    }

    void handle(Long a) {
        System.out.println("#Long:" + a);
    }

    void handle(Float a) {
        System.out.println("#Float:" + a);
    }

    void handle(Double a) {
        System.out.println("#Double:" + a);
    }
}
