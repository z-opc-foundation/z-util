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
class Human12 {
    private static Human12Field1 human12Field1 = new Human12Field1();

    static {
        System.out.println("Human12.static{}");
    }

    private Human12Field2 human12Field2 = new Human12Field2();
    private String age;

    {
        System.out.println("Human12.{}");
    }

    /**
     * Human12方法。
     */
    public Human12() {
        System.out.println("Human12.()");
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

class Coder2 extends Human12 {

    private static Coder2Field1 coder2Field1 = new Coder2Field1();

    static {
        System.out.println("Coder2.static{}");
    }

    private Coder2Field2 coder2Field2 = new Coder2Field2();

    {
        System.out.println("Coder2.{}");
    }


    /**
     * Coder2方法。
     */
    public Coder2() {
        System.out.println("Coder2.()");
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Coder2 coder = new Coder2();
        coder.eat();// human的行为
        coder.wirteCode();// 张三的行为
    }

    /**
     * wirteCode方法。
     */
    public void wirteCode() {
        System.out.println("愉快地写代码");
    }
}

class Human12Field1 {
    /**
     * Human12Field1方法。
     */
    public Human12Field1() {
        System.out.println("Human12Field1.()");
    }
}

class Human12Field2 {
    /**
     * Human12Field2方法。
     */
    public Human12Field2() {
        System.out.println("Human12Field2.()");
    }
}


class Coder2Field1 {
    /**
     * Coder2Field1方法。
     */
    public Coder2Field1() {
        System.out.println("Coder2Field1.()");
    }
}

class Coder2Field2 {
    /**
     * Coder2Field2方法。
     */
    public Coder2Field2() {
        System.out.println("Coder2Field2.()");
    }
}