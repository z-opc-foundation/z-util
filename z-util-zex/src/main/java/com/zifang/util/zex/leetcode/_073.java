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
public class _073 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _073().setZeroes(new int[][]{new int[]{0, 1, 2, 0}, new int[]{3, 4, 5, 2}, new int[]{1, 3, 1, 5}});
    }

    /**
     * setZeroes方法。
     * * @param matrix int[][]类型参数
     */
    public void setZeroes(int[][] matrix) {

        // 行数
        int m = matrix.length;

        // 列数
        int n = matrix[0].length;

        // 行标记
        int mFlag = 1;

        // 列标记
        int nFlag = 1;

        // 先拿到第一行是否清空的标记
        for (int i = 0; i < n; i++) {
            if (matrix[0][i] == 0) {
                mFlag = 0;
            }
        }

        // 拿到第一列是否清空的标记
        for (int i = 0; i < m; i++) {
            if (matrix[i][0] == 0) {
                nFlag = 0;
            }
        }

        // 循环，将首行首列做成标记
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][0] = 0;
                    matrix[0][j] = 0;
                }
            }
        }

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                if (matrix[i][0] == 0 || matrix[0][j] == 0) {
                    matrix[i][j] = 0;
                }
            }
        }

        if (mFlag == 0) {
            for (int i = 0; i < n; i++) {
                matrix[0][i] = 0;
            }
        }
        if (nFlag == 0) {
            for (int i = 0; i < m; i++) {
                matrix[i][0] = 0;
            }
        }
    }
}
