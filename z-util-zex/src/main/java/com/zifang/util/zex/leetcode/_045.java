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
public class _045 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        new _045().jump(new int[]{1, 2, 1, 1, 1});
    }

    /**
     * jump方法。
     * * @param nums int[]类型参数
     *
     * @return int类型返回值
     */
    public int jump(int[] nums) {

        if (nums.length == 1) {
            return 0;
        }

        int n = 0;
        for (int i = 0; i < nums.length - 1; i++) {

            // 如果处在当前位置则直接跳出
            if (i == nums.length - 1) {
                break;
            }

            // 判断当前位置是否可以抵达最后位
            int max = nums[i] + i;
            if (max >= nums.length - 1) {
                n = n + 1;
                break;
            }

            // 不可抵达最后位，选取当前批次返回内最大值,并跳转
            int tempMax = 0;
            int tempIndex = 0;
            for (int j = 0; j < nums[i]; j++) {
                if (j == 0) {
                    tempIndex = i + j + 1;
                    tempMax = nums[i + 1 + j] + j + i + 1;
                } else {
                    if (nums[i + j + 1] + i + j + 1 > tempMax) {
                        tempMax = nums[i + 1 + j] + j + i + 1;
                        tempIndex = i + j + 1;
                    }
                }
            }
            i = tempIndex - 1;
            n = n + 1;

        }
        return n;
    }
}
