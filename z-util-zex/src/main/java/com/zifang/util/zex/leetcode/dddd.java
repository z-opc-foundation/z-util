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

import java.util.HashMap;
import java.util.Map;

/**
 * dddd类。
 */
public class dddd {

    /**
     * removeDuplicates方法。
     * * @param nums int[]类型参数
     *
     * @return static int类型返回值
     */
    public static int removeDuplicates(int[] nums) {
        int k = 0;
        int base = nums[0];

        int time = 1;
        int startIndex = 0;
        int endIndex = 0;

        for (int i = 1; i < nums.length; i++) {

            if (base == nums[i]) {
                if (time < 2) {
                    time = time + 1;
                    endIndex = i;
                }
            } else {

                for (int j = startIndex; j <= endIndex; j++) {
                    nums[k + (j - startIndex)] = nums[j];
                }
                k = k + (endIndex - startIndex + 1);

                time = 1;
                startIndex = i;
                endIndex = i;
            }
            base = nums[i];
        }

        for (int j = startIndex; j <= endIndex; j++) {
            nums[k + (j - startIndex)] = nums[j];
        }
        k = k + (endIndex - startIndex + 1);

        return k;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        removeDuplicates(new int[]{1, 1, 1, 2, 2, 3});
    }

    /**
     * majorityElement方法。
     * * @param nums int[]类型参数
     *
     * @return int类型返回值
     */
    public int majorityElement(int[] nums) {
        Map map = new HashMap();
        for (int i = 0; i < nums.length; i++) {
            if (!map.containsKey(nums[i])) {
                map.put(nums[i], 1);
            } else {
                map.put(nums[i], ((int) map.get(nums[i])) + 1);
            }
        }

        for (Object en : map.entrySet()) {
            Map.Entry xx = (Map.Entry) en;
            if (((int) (xx.getValue())) > nums.length / 2) {
                return (int) xx.getKey();
            }
        }
        return 1;
    }
}
