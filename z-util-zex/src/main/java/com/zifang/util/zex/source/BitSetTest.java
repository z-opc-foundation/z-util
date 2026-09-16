package com.zifang.util.zex.source;

import java.util.BitSet;

/**
 * BitSet测试类。
 * <p>
 * 此类演示了Java BitSet的基本用法。
 * BitSet是一种位向量，用于高效地存储和操作位信息。
 *
 * @author zifang
 * @version 1.0
 */
public class BitSetTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {


        BitSet bitSet = new BitSet(129);
        bitSet.set(0);
        bitSet.set(64);

        BitSet ass = bitSet.get(0, 65);

        bitSet.nextSetBit(1);

    }
}
