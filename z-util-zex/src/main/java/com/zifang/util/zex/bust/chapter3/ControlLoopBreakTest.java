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
public class ControlLoopBreakTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (int i = 0; i < 4; i++) {
            System.out.println("当前i值为" + i);
            if (i == 2) {
                break;
            }
        }
    }
}
