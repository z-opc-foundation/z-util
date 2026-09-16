package com.zifang.util.zex.bust.chapter3;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public class LoopMarkTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        for (int i = 0; i < 3; i++) {
            outer:
            for (int j = 0; j < 3; j++) {
                for (int z = 0; z < 3; z++) {
                    System.out.println("i:" + i + "|j:" + j + "|z:" + z);
                    if (z == 1) {
                        break outer;
                    }
                }
            }
        }
    }
}
