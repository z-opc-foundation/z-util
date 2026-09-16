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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * _057类。
 */
public class _057 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _057().insert(new int[][]{new int[]{0, 7}, new int[]{8, 8}, new int[]{9, 11}}, new int[]{4, 13});
    }

    /**
     * insert方法。
     * * @param intervals int[][]类型参数
     *
     * @param newInterval int[]类型参数
     * @return int[][]类型返回值
     */
    public int[][] insert(int[][] intervals, int[] newInterval) {

        if (intervals.length == 0) {
            return new int[][]{newInterval};
        }

        Arrays.sort(intervals, new Comparator<int[]>() {
            @Override
            /**
             * compare方法。
             *      * @param a1 int[]类型参数
             * @param a2 int[]类型参数
             * @return int类型返回值
             */
            public int compare(int[] a1, int[] a2) {
                return a1[0] - a2[0];
            }
        });

        List<int[]> list = new ArrayList<>();

        // 1. [ {} ] 在区间内
        // 2. [] {[]} []包含区间
        // 3. [ {] [} ] 跨区间
        // 3. [ {]} [ ] 跨区间
        // 4. [ {] [] [] [}] 跨多区间

        int[] newA = new int[2];

        int targetLeft = newInterval[0];
        int targetRight = newInterval[1];

        boolean flag = false;
        boolean start = false;

        for (int i = 0; i < intervals.length; i++) {

            if (flag) {
                list.add(intervals[i]);
                continue;
            }

            int currentLeft = intervals[i][0];
            int currentRight = intervals[i][1];

            // 区间内 完全包含
            if (currentLeft <= targetLeft && targetRight <= currentRight) {
                return intervals;
            }

            // 完全无关系区间
            if ((currentRight < targetLeft || targetRight < currentLeft)) {
                list.add(intervals[i]);
            }

            // 目标左位置在区间内
            if (currentLeft <= targetLeft && targetLeft <= currentRight) {
                newA[0] = currentLeft;
                start = true;
                if (i == intervals.length - 1) {
                    newA[1] = targetRight;
                    list.add(newA);
                    flag = true;
                }
            }

            // 目标右位置在区间内
            if (currentLeft <= targetRight && targetRight <= currentRight) {
                flag = true;
                newA[1] = currentRight;
                list.add(newA);
            }

            if (targetRight < currentLeft) {
                flag = true;
                newA[1] = targetRight;
                list.add(newA);
            }
        }

        if (!flag) {
            list.add(newInterval);
        }


        intervals = list.toArray(new int[][]{});

        Arrays.sort(intervals, new Comparator<int[]>() {
            @Override
            /**
             * compare方法。
             *      * @param a1 int[]类型参数
             * @param a2 int[]类型参数
             * @return int类型返回值
             */
            public int compare(int[] a1, int[] a2) {
                return a1[0] - a2[0];
            }
        });

        return intervals;
    }
}
