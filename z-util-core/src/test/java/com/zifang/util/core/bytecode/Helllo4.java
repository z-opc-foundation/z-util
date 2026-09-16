package com.zifang.util.core.bytecode;

import java.io.IOException;

/**
 * Helllo4类。
 */
public class Helllo4 {

    public static final String aaaaa = "干饭人万岁！";

    /**
     * foo方法。
     * * @param i int类型参数
     *
     * @param j int类型参数
     * @return static int类型返回值
     */
    public static int foo(int i, int j) throws IOException {
        int c = i + j + 1;
        return c;
    }

    /**
     * testException方法。
     * * @param a String类型参数
     *
     * @param b int类型参数
     * @return static void类型返回值
     */
    public static void testException(String a, Integer b) {
        try {
            foo(1, 2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException exception) {
            System.out.println("干饭人万万岁@@@@!!!!!");
        }
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        testException("吃饭", 100);
    }
}
