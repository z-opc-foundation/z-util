package com.zifang.util.zex.sort;

import java.util.Arrays;

/**
 * 排序算法工具类。
 * <p>
 * 此类提供了多种排序算法的实现，包括直接选择排序、二分插入排序、冒泡排序、
 * 直接插入排序和希尔排序等。
 * <p>
 * 各排序算法特性总结：
 * <ul>
 *   <li>直接选择排序：不稳定，时间复杂度O(n^2)，空间复杂度O(1)</li>
 *   <li>二分插入排序：不稳定，时间复杂度O(n^2)，空间复杂度O(1)</li>
 *   <li>直接插入排序：稳定，最优O(n)，最坏O(n^2)，空间复杂度O(1)</li>
 *   <li>希尔排序：不稳定，时间复杂度O(n^1.3)，空间复杂度O(1)</li>
 * </ul>
 *
 * @author zifang
 * @version 1.0
 */
public class SortUtils {


    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new SortUtils().directInsertSort(new int[]{2, 1, 4, 3, 9, 5});
    }

    /**
     * 直接选择排序。
     * <p>
     * 直接选择排序是一种不稳定的排序算法。
     * 时间复杂度为O(n^2)，当记录占用字节数较多时，直接选择排序通常比直接插入排序的执行速度快些。
     * 空间复杂度为O(1)。
     * <p>
     * 算法原理：从未排序序列中找到最小元素，存放到已排序序列的末尾，以此类推，直到所有元素均排序完毕。
     *
     * @param arr 待排序的整数数组，排序在原数组上进行
     */
    public void directSelectionSort(int[] arr) {
        int i, j, min, tmp, len = arr.length;
        for (i = 0; i < len; i++) {
            min = i;

            //从arr[i]~arr[len-1]中找出最小的记录
            for (j = i + 1; j < len; j++) {
                if (arr[min] > arr[j]) {
                    min = j;
                }
            }

            //如果最小记录不是arr[i]，把arr[i]和arr[min]交换位置
            //使得arr[0]~arr[i]是有序序列，而且比arr[i+1]~arr[len-1]中任何记录都小
            if (min != i) {
                tmp = arr[i];
                arr[i] = arr[min];
                arr[min] = tmp;
            }
        }
    }

    /**
     * 二分插入排序。
     * <p>
     * 二分插入排序是直接插入排序的改进版，是一种不稳定的排序算法。
     * 用二分法找出新记录在有序序列中的位置，减少了比较次数。
     * <p>
     * 算法原理：
     * 将一个记录插入到已排序好的有序序列中，从而得到一个新记录数增1的有序序列。
     * 二分插入排序用二分法找出新记录在有序序列中的位置。
     *
     * @param arr 待排序的整数数组，排序在原数组上进行
     */
    public void binaryInsertionSort(int[] arr) {
        int i, j, left, right, center, tmp, len = arr.length;

        for (i = 1; i < len; i++) {

            //如果新记录小于有序序列的最大元素，则用二分法找出新纪录在有序序列中的位置
            if (arr[i] < arr[i - 1]) {
                left = 0;
                right = i - 1;
                while (left < right) {
                    //获取中间位置索引，把有序序列分成两个子序列
                    center = (right + left) / 2;

                    if (arr[center] < arr[i]) {
                        //如果新纪录大于中间位置记录，则在右边序列继续进行二分
                        left = center + 1;
                    } else {
                        //如果新纪录小于中间位置记录，则在左边序列继续进行二分
                        right = center - 1;
                    }
                }

                tmp = arr[i];

                //把比arr[i]大的记录往后移
                for (j = i; j > left; j--) {
                    arr[j] = arr[j - 1];
                }

                arr[left] = tmp;
            }
        }
    }

    // 选择

    /**
     * 冒泡排序。
     */
    public void sort1() {
        int[] a = new int[]{4, 2, 6, 5, 1, 3};

        for (int i = 0; i < a.length - 1; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                int a1 = a[j];
                int a2 = a[j + 1];
                if (a1 > a2) {
                    a[j + 1] = a1;
                    a[j] = a2;
                }
            }
        }
        System.out.println(Arrays.toString(a));
    }

    /**
     * sort2方法。
     */
    public void sort2() {
        int[] a = new int[]{4, 2, 6, 5, 1, 3};

        for (int i = 0; i < a.length - 1; i++) {
            int max = 0;
            int max_index = 0;
            for (int j = 0; j < a.length - i; j++) {
                if (a[j] > max) {
                    max = a[j];
                    max_index = j;
                }
            }
            a[max_index] = a[a.length - i - 1];
            a[a.length - i - 1] = max;
        }
        System.out.println(Arrays.toString(a));
    }

    /**
     * 直接插入排序。
     * <p>
     * 直接插入排序属于稳定排序。
     * 直接插入排序空间复杂度O(1)。
     * 最优时间复杂度O(n)，当待排序的数组已经排序好时，直接插入排序的时间复杂度为O(n)。
     * 最坏时间复杂度O(n^2)，当待排序的数组是倒序时，直接插入排序的时间复杂度为O(n^2)。
     * 直接插入排序适用于数量比较小的数组排序。
     * <p>
     * 基本原理：
     * index不断往后移动，index在前面的有序序列内一个一个找，找到该放进去的位置。
     *
     * @param arr 待排序的整数数组，排序在原数组上进行
     */
    public void directInsertSort(int[] arr) {
        int i, j, tmp;

        //从i=1开始遍历，a[0]为有序序列
        for (i = 1; i < arr.length; i++) {
            for (j = i - 1; j >= 0; j--) {
                if (arr[j] <= arr[j + 1]) {
                    break;
                }

                tmp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = tmp;
            }
        }
    }

    /**
     * 希尔排序。
     * <p>
     * 希尔排序又叫缩小增量排序，是直接插入排序算法的一种更高效的改进版本。
     * 希尔排序属于不稳定排序。
     * 希尔排序空间复杂度O(1)。
     * 希尔排序的时间复杂度和其增量序列有关系，平均时间复杂度O(n^1.3)。
     * <p>
     * 基本原理：
     * 先将整个待排序的记录序列分割成为若干子序列分别进行直接插入排序，
     * 待整个序列中的记录"基本有序"时，再对全体记录进行依次直接插入排序。
     *
     * @param arr 待排序的整数数组，排序在原数组上进行
     */
    public void shellSort(int[] arr) {
        int i, j, tmp, len = arr.length;
        //希尔排序步长
        int step = len / 2;

        //缩小步长，直到步长为0
        while (step > 0) {
            for (i = step; i < len; i++) {
                //如果新记录小于有序序列中的最大记录，则进行直接插入排序
                if (arr[i] < arr[i - step]) {
                    tmp = arr[i];
                    j = i - step;
                    while (j >= 0 && arr[j] > tmp) {
                        arr[j + step] = arr[j];
                        j -= step;
                    }
                    arr[j + step] = tmp;
                }
            }

            step /= 2;
        }
    }
}
