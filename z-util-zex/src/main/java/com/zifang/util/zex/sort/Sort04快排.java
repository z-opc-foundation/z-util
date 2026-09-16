package com.zifang.util.zex.sort;

/**
 * 排序算法实现类。
 * <p>
 * 此类实现了经典的快速排序算法，用于对数据进行排序。
 * 快速排序是一种不稳定排序算法。
 *
 * @author zifang
 * @version 1.0
 */
public class Sort04快排 {

    /**
     * func方法。
     * * @param a int[]类型参数
     *
     * @param start int类型参数
     * @param end   int类型参数
     * @return static void类型返回值
     */
    public static void func(int[] a, int start, int end) {
        // 支点
        int point = (start + end) / 2;
        int point_value = a[point];

        int l = start;
        int r = end;
        while (l <= r) {
            while (point_value > a[l]) {
                l++;
            }
            while (point_value <= a[r]) {
                r++;
            }
            if (l <= r) {
                int temp = a[l];
                a[l] = a[r];
                a[r] = temp;
                l++;
                r--;
            }
        }
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        int[] a = new int[]{4, 2, 6, 5, 1, 3, 10};
        func(a, 0, a.length - 1);
    }

}
