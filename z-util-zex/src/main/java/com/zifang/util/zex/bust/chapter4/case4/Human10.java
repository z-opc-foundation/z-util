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
class Human10 {

    public final static String aa = "cc";

    static {

        System.out.println(aa);
    }

    public final String sex = "女的";

    private String age;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Human10 human = new Human10();
        human.eat();
        System.out.println(human.sex);
        //human.sex = "男的";
    }

    /**
     * eat方法。
     */
    public void eat() {
        System.out.println("我要吃饭");
        goWc();
    }

    private void goWc() {
        System.out.println("吃完饭就上个厕所");
    }
}