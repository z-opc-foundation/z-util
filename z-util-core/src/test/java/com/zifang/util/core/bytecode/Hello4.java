package com.zifang.util.core.bytecode;

/**
 * Hello4类。
 */
public class Hello4 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new Thread(() -> System.out.println("吃饭")).start();
    }
}
