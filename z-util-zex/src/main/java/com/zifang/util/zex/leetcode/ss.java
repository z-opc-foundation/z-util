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
public class ss {
    /**
     * removeElement方法。
     * * @param nums int[]类型参数
     *
     * @param val int类型参数
     * @return static int类型返回值
     */
    public static int removeElement(int[] nums, int val) {
        int k = 0;
        int lastIndex = nums.length;
        for (int i = 0; i < nums.length && i < lastIndex; i++) {
            if (nums[i] == val) {
                for (int t = nums.length - 1; t > 0; t--) {
                    lastIndex = t;
                    if (nums[t] != val) {
                        k = k + 1;
                        int temp = nums[i];
                        nums[i] = nums[t];
                        nums[t] = temp;
                        break;
                    }
                }
            } else {
                k = k + 1;
            }
        }
        return k;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        removeElement(new int[]{2}, 3);
    }
}
