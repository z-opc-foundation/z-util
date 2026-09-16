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
class Father1 {

    protected int money = 100;

    /**
     * fishing方法。
     */
    public void fishing() {
        System.out.println("钓鱼");
    }

}

class Son1 extends Father1 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Son1 son = new Son1();
        son.fishing();
        son.writeCode();
    }

    /**
     * writeCode方法。
     */
    public void writeCode() {
        System.out.println("写代码");
    }
}