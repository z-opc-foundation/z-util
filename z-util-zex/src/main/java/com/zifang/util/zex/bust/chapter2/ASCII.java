package com.zifang.util.zex.bust.chapter2;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public class ASCII {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (char i = 0; i < 256; i++) {
            System.out.println((int) i + "->" + Integer.toBinaryString(i) + "->" + i);
        }
    }
}
