package com.zifang.util.zex.bust.chapter4.case5;


/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
class Field {

    static {
        System.out.println("Field-initial-static");
    }

    {
        System.out.println("Field-not-initial-static");
    }

    /**
     * Field方法。
     * * @param str String类型参数
     */
    public Field(String str) {
        System.out.println(str + "-Field-construct");
    }
}

class Father {

    static {
        System.out.println("father-initial-static");
    }

    private Field field = new Field("father");

    {
        System.out.println("father-not-initial-static");
    }

    /**
     * Father方法。
     */
    public Father() {
        System.out.println("father-construct");
    }

}

/**
 * Son类。
 */
public class Son extends Father {

    static {
        System.out.println("Son-initial-static");
    }

    private Field field = new Field("son");

    {
        System.out.println("Son-not-initial-static");
    }

    /**
     * Son方法。
     */
    public Son() {
        System.out.println("Son-construct");
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Son son = new Son();
    }
}
