package com.zifang.util.zex.bust.chapter4.enumCase;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public class HungryLevelEnumTest2 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        System.out.println(HungryLevelEnum.HUNGRY_LEVEL_1.ordinal());
        System.out.println(HungryLevelEnum.HUNGRY_LEVEL_2.ordinal());
        System.out.println(HungryLevelEnum.HUNGRY_LEVEL_3.ordinal());
    }
}
