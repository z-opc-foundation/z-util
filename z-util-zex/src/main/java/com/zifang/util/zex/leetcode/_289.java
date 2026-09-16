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
 * _289类。
 */
public class _289 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _289().gameOfLife(new int[][]{new int[]{0, 1, 0}, new int[]{0, 0, 1}, new int[]{1, 1, 1}, new int[]{0, 0, 0}});

        int[][] a = new int[][]{};
        Arrays.sort(a, new Comparator<int[]>() {
            @Override
            /**
             * compare方法。
             *      * @param o1 int[]类型参数
             * @param o2 int[]类型参数
             * @return int类型返回值
             */
            public int compare(int[] o1, int[] o2) {
                return 0;
            }
        });

        List<int[]> list = new ArrayList<>();
        list.toArray(new int[][]{});


        String s = new String();
    }

    /**
     * getValue方法。
     * * @param board int[][]类型参数
     *
     * @param m int类型参数
     * @param n int类型参数
     * @param x int类型参数
     * @param y int类型参数
     * @return int类型返回值
     */
    public int getValue(int[][] board, int m, int n, int x, int y) {
        if (x < 0 || x > n - 1) {
            return 0;
        }

        if (y < 0 || y > m - 1) {
            return 0;
        }

        int val = board[y][x];
        if (val == -1) {
            return 1;
        } else if (val == -2) {
            return 0;
        } else {
            return val;
        }
    }


    // 活 -> 活 ：1
    // 活 -> 死 ：-1
    // 死 -> 活 ：-2
    // 死 -> 死 ：0

    /**
     * getCnt方法。
     * * @param board int[][]类型参数
     *
     * @param m int类型参数
     * @param n int类型参数
     * @param x int类型参数
     * @param y int类型参数
     * @return int类型返回值
     */
    public int getCnt(int[][] board, int m, int n, int x, int y) {
        int cnt = 0;
        cnt = cnt + getValue(board, m, n, x - 1, y - 1);
        cnt = cnt + getValue(board, m, n, x, y - 1);
        cnt = cnt + getValue(board, m, n, x + 1, y - 1);
        cnt = cnt + getValue(board, m, n, x - 1, y);
        cnt = cnt + getValue(board, m, n, x + 1, y);
        cnt = cnt + getValue(board, m, n, x - 1, y + 1);
        cnt = cnt + getValue(board, m, n, x, y + 1);
        cnt = cnt + getValue(board, m, n, x + 1, y + 1);

        return cnt;
    }

    /**
     * calValue方法。
     * * @param originValue int类型参数
     *
     * @param cnt int类型参数
     * @return int类型返回值
     */
    public int calValue(int originValue, int cnt) {
        if (cnt < 2) {
            if (originValue == 0) {
                return 0;
            } else {
                return -1;
            }
        }

        if (cnt == 2 || cnt == 3) {
            if (originValue == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        if (cnt == 3) {
            if (originValue == 1) {
                return 1;
            } else {
                return -2;
            }
        }

        if (cnt > 3) {
            if (originValue == 1) {
                return -1;
            } else {
                return 0;
            }
        }

        return 0;
    }

    /**
     * gameOfLife方法。
     * * @param board int[][]类型参数
     */
    public void gameOfLife(int[][] board) {

        // y方向
        int m = board.length;
        // x方向
        int n = board[0].length;

        for (int y = 0; y < m; y++) {
            for (int x = 0; x < n; x++) {
                // 获取x,y坐标的周边数量
                int cnt = getCnt(board, m, n, x, y);

                // 获得当前的应该赋予的数量
                // 活 -> 活 ：1
                // 活 -> 死 ：-1
                // 死 -> 活 ：-2
                // 死 -> 死 ：0
                int xyValue = calValue(board[y][x], cnt);

                board[y][x] = xyValue;
            }
        }


        for (int y = 0; y < m; y++) {
            for (int x = 0; x < n; x++) {
                if (board[y][x] == -1) {
                    board[y][x] = 0;
                }
                if (board[y][x] == -2) {
                    board[y][x] = 1;
                }
            }
        }
    }
}
