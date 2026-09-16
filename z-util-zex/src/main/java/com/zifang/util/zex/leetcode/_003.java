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
public class _003 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        new _003().lengthOfLongestSubstring("abada");
    }

    /**
     * lengthOfLongestSubstring方法。
     * * @param s String类型参数
     *
     * @return int类型返回值
     */
    public int lengthOfLongestSubstring(String s) {
        int max = 0;
        int count = 0;
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            for (int j = count; j < i; j++) {
                if (c[i] == c[j]) {
                    count = j + 1;
                    break;
                }
            }
            max = Math.max(max, i - count + 1);
        }
        return max;
    }
}
