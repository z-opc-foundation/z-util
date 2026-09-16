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
public class _209 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _209().minSubArrayLen(15, new int[]{1, 2, 3, 4, 5});
    }

    /**
     * minSubArrayLen方法。
     * * @param target int类型参数
     *
     * @param nums int[]类型参数
     * @return int类型返回值
     */
    public int minSubArrayLen(int target, int[] nums) {
        int startIndex = 0;
        int endIndex = 0;

        int minLength = 0;
        int sum = 0;

        if (minLength == 0) {
            for (int j = startIndex; j < nums.length; j++) {
                endIndex = j;
                sum = sum + nums[j];
                if (sum < target) {
                    continue;
                } else {
                    minLength = endIndex - startIndex + 1;
                    break;
                }
            }
            if (minLength == 0) {
                return 0;
            }
        }

        while (true) {

            // 调整后将削减左标看是否可以削减
            while (true) {
                if (sum - nums[startIndex] >= target) {
                    sum = sum - nums[startIndex];
                    startIndex = startIndex + 1;
                    minLength = endIndex - startIndex + 1;
                }
                // 砍掉左标发现小于，则重新滑动
                else {
                    break;
                }
            }

            // 左标与右标
            if (endIndex <= nums.length - 2) {
                endIndex = endIndex + 1;
                startIndex = startIndex + 1;
                sum = sum + nums[endIndex - 1] - nums[startIndex - 1];
            } else {
                // endIndex 已经划到头了，不继续滑了
                break;
            }

            // 滑动后还是小，则继续滑动
            if (sum <= target) {
                continue;
            }


        }
        return minLength;
    }
}


