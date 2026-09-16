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
class Test {
    private static Test1 staticTest1 = new Test1("staticTest1");

    static {
        System.out.println("Test.static{}");
    }

    private Test1 test1 = new Test1("Test1");

    {
        System.out.println("Test.{}");
    }

    /**
     * Test方法。
     */
    public Test() {
        System.out.println("Test()");
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Test test = new Test();
    }
}

class Test1 {
    /**
     * Test1方法。
     * * @param str String类型参数
     */
    public Test1(String str) {
        System.out.println("test1(" + str + ")");
    }
}
