package com.zifang.util.zex.leetcode;

/**
 * LeetCode算法练习类。
 * <p>
 * 此类包含LeetCode题目的解决方案。
 * 练习算法和数据结构知识。
 *
 * @author zifang
 * @version 1.0
 */
public class _0134 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _0134().canCompleteCircuit(new int[]{1, 2, 3, 4, 5}, new int[]{3, 4, 5, 1, 2});
    }

    /**
     * canCompleteCircuit方法。
     * * @param gas int[]类型参数
     *
     * @param cost int[]类型参数
     * @return int类型返回值
     */
    public int canCompleteCircuit(int[] gas, int[] cost) {
        if (gas == null || cost == null) return -1;

        int n = gas.length;

        int min = Integer.MAX_VALUE;
        int resIdx = 0;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += gas[i] - cost[i];
            if (sum < min) {
                min = sum;
                resIdx = (i + 1) % n;
            }
        }

        return sum < 0 ? -1 : resIdx;
    }
}
