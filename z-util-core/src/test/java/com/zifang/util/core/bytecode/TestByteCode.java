package com.zifang.util.core.bytecode;

import java.util.ArrayList;
import java.util.List;

/**
 * TestByteCode类。
 */
public class TestByteCode {

    private int int_2_2_1 = 1;

    /**
     * test_2_2_1方法。
     *
     * @return static void类型返回值
     */
    public static void test_2_2_1() {
        int i = 0;
        for (int j = 0; j < 50; j++) {
            i = i++;
        }
        System.out.println(i);
    }

    /**
     * test_2_2_2方法。
     *
     * @return static void类型返回值
     */
    public static void test_2_2_2() {
        int i = 0;
        for (int j = 0; j < 50; j++) {
            i = ++i;
        }
        System.out.println(i);
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new TestByteCode().test2_1_2_1();
        test_2_2_2();
        new TestByteCode().test2_5_1();

        int test2_5_3_1 = new TestByteCode().test2_5_3_1();
        int test2_5_3_2 = new TestByteCode().test2_5_3_2();
        int test2_5_3_3 = new TestByteCode().test2_5_3_3();
        String test2_5_3_4 = new TestByteCode().test2_5_3_4();
        System.out.println(test2_5_3_1);
        System.out.println(test2_5_3_2);
        System.out.println(test2_5_3_3);
        System.out.println(test2_5_3_4);


    }

    /**
     * test2_1_1_1方法。
     * * @param i int类型参数
     *
     * @param j int类型参数
     */
    public void test2_1_1_1(int i, int j) {
        int k = int_2_2_1 + i + j;
        System.out.println(k);
    }

    /**
     * test2_1_1_2方法。
     */
    public void test2_1_1_2() {
        int i = 1;
        int j = 2;
        int k = int_2_2_1 + i + j;
        System.out.println(k);
    }

    /**
     * test2_1_2_1方法。
     *
     * @return int类型返回值
     */
    public int test2_1_2_1() {
        int i = 1;
        int j = 2;
        int k = int_2_2_1 + i + j;
        return k;
    }

    /**
     * test2_3_1方法。
     * * @param n int类型参数
     *
     * @return int类型返回值
     */
    public int test2_3_1(int n) {
        if (n > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * test_2_3_2方法。
     * * @param c int[]类型参数
     */
    public void test_2_3_2(int[] c) {
        for (int i = 0; i < c.length; i++) {
            System.out.println(i);
        }
    }

    /**
     * test_2_3_2_1_1方法。
     */
    public void test_2_3_2_1_1() {
        int[] numbers = new int[]{1, 2, 3};
        for (int number : numbers) {
            System.out.println(number);
        }
    }

    /**
     * test_2_3_2_1_2方法。
     */
    public void test_2_3_2_1_2() {
        List<String> a = new ArrayList<>();
        a.add("a");
        a.add("b");
        a.add("c");
        for (String item : a) {
            System.out.println(item);
        }
    }

    /**
     * test_2_3_3_1_1方法。
     * * @param i int类型参数
     *
     * @return int类型返回值
     */
    public int test_2_3_3_1_1(int i) {
        switch (i) {
            case 100:
                return 0;
            case 101:
                return 1;
            case 104:
                return 4;
            default:
                return -1;
        }
    }

    /**
     * test_2_3_3_1_2方法。
     * * @param i int类型参数
     *
     * @return int类型返回值
     */
    public int test_2_3_3_1_2(int i) {
        switch (i) {
            case 1:
                return 0;
            case 10:
                return 1;
            case 100:
                return 4;
            default:
                return -1;
        }
    }

    /**
     * test_2_3_3_2方法。
     * * @param name String类型参数
     *
     * @return int类型返回值
     */
    public int test_2_3_3_2(String name) {
        switch (name) {
            case "吃饭1":
                return 100;
            case "吃饭2":
                return 200;
            default:
                return -1;
        }
    }

    /**
     * test2_5_1_exception方法。
     */
    public void test2_5_1_exception() {
        throw new RuntimeException();
    }

    /**
     * test2_5_1_handler方法。
     * * @param e Exception类型参数
     */
    public void test2_5_1_handler(Exception e) {
        System.out.println("捕获到异常");
    }

    /**
     * test2_5_finally方法。
     */
    public void test2_5_finally() {
        System.out.println("finally语句块");
    }

    /**
     * test2_5_1方法。
     */
    public void test2_5_1() {
        try {
            test2_5_1_exception();
        } catch (RuntimeException e) {
            test2_5_1_handler(e);
        }
    }

    /**
     * test2_5_2方法。
     */
    public void test2_5_2() {
        try {
            test2_5_1_exception();
        } catch (NullPointerException e) {
            test2_5_1_handler(e);
        } catch (RuntimeException e) {
            test2_5_1_handler(e);
        }
    }

    /**
     * test2_5_3方法。
     */
    public void test2_5_3() {
        try {
            test2_5_1_exception();
        } catch (NullPointerException e) {
            test2_5_1_handler(e);
        } finally {
            test2_5_finally();
        }
    }

    /**
     * test2_5_3_1方法。
     *
     * @return int类型返回值
     */
    public int test2_5_3_1() {
        try {
            int a = 1 / 0;
            return 0;
        } catch (Exception e) {
            int b = 1 / 0;
            return 1;
        } finally {
            return 2;
        }
    }

    /**
     * test2_5_3_2方法。
     *
     * @return int类型返回值
     */
    public int test2_5_3_2() {
        int i = 100;
        try {
            return i;
        } finally {
            ++i;
        }
    }

    /**
     * test2_5_3_3方法。
     *
     * @return int类型返回值
     */
    public int test2_5_3_3() {
        int i = 100;
        try {
            return i;
        } finally {
            i++;
        }
    }

    /**
     * test2_5_3_4方法。
     *
     * @return String类型返回值
     */
    public String test2_5_3_4() {
        String s = "hello";
        try {
            return s;
        } finally {
            s = "xyz";
        }
    }

    /**
     * test2_7_1方法。
     */
    public void test2_7_1() {
        Runnable r1 = new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("hello, inner class");
            }
        };
        r1.run();
    }

    /**
     * test2_7_2方法。
     */
    public void test2_7_2() {
        Runnable r1 = () -> System.out.println("hello, inner class");
        r1.run();
    }
}
