package com.zifang.util.zex.sort;

/**
 * 排序算法实现类。
 * <p>
 * 此类实现了经典的插入排序算法，用于对数据进行排序。
 * 插入排序是一种稳定排序算法。
 *
 * @author zifang
 * @version 1.0
 */

import java.util.Arrays;

/**
 * Sort03插入类。
 */
public class Sort03插入 {

    /**
     * insert方法。
     * * @param a int[]类型参数
     *
     * @param index   int类型参数
     * @param replace int类型参数
     * @return static void类型返回值
     */
    public static void insert(int[] a, int index, int replace) {
        System.arraycopy(a, index, a, index + 1, a.length - index - 1);
        a[index] = replace;
    }

    // end 是0位开始

    /**
     * index方法。
     * * @param a int[]类型参数
     *
     * @param end     int类型参数
     * @param compare int类型参数
     * @return static int类型返回值
     */
    public static int index(int[] a, int end, int compare) {
        if (compare <= a[0]) {
            return 0;
        }
        if (compare >= a[end]) {
            return end + 1;
        }
        for (int i = 1; i < end; i++) {
            if (compare >= a[i - 1] && compare <= a[i + 1]) {
                if (compare >= a[i]) {
                    return i + 1;
                } else {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        int[] a = new int[]{4, 6, 5, 1, 3, 7, 11, 22, 15, 12, 2};

        for (int i = 1; i < a.length - 1; i++) {

            int x_value = a[a.length - 1]; //末尾的值
            int x = index(a, i, a[a.length - 1]);// 末尾的只需要插到的位置
            insert(a, x, x_value);
            System.out.println(Arrays.toString(a));

        }

    }
}
