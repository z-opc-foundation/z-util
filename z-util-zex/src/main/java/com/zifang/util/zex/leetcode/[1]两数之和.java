//Given an array of integers nums and an integer target, return indices of the t
//wo numbers such that they add up to target. 
//
// You may assume that each input would have exactly one solution, and you may n
//ot use the same element twice. 
//
// You can return the answer in any order. 
//
// 
// Example 1: 
//
// 
//Input: nums = [2,7,11,15], target = 9
//Output: [0,1]
//Output: Because nums[0] + nums[1] == 9, we return [0, 1].
// 
//
// Example 2: 
//
// 
//Input: nums = [3,2,4], target = 6
//Output: [1,2]
// 
//
// Example 3: 
//
// 
//Input: nums = [3,3], target = 6
//Output: [0,1]
// 
//
// 
// Constraints: 
//
// 
// 2 <= nums.length <= 104 
// -109 <= nums[i] <= 109 
// -109 <= target <= 109 
// Only one valid answer exists. 
// 
//
// 
//Follow-up: Can you come up with an algorithm that is less than O(n2) time comp
//lexity? Related Topics 数组 哈希表 
// 👍 11173 👎 0

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
class TwoSum {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Solution solution = new TwoSum().new Solution();
        System.out.println(solution);
    }

    class Solution {
        /**
         * twoSum方法。
         * * @param nums int[]类型参数
         *
         * @param target int类型参数
         * @return int[]类型返回值
         */
        public int[] twoSum(int[] nums, int target) {
            int a = 0;
            int b = 0;
            for (int i = 0; i < nums.length; i++) {
                int now = nums[i];
                int less = target - now;
                for (int j = 0; j < nums.length; j++) {
                    if (nums[j] == less && i != j) {
                        b = j;
                        a = i;
                        break;
                    }
                }
            }
            return new int[]{a, b};
        }
    }
}