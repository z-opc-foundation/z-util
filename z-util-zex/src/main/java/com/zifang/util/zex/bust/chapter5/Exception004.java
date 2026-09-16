package com.zifang.util.zex.bust.chapter5;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public class Exception004 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        int re = bar();
        System.out.println(re);
    }

    private static int bar() {
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new Exception("我将被忽略，因为下面的finally中使用了return");
        } finally {
            return 2;
        }
    }
}
