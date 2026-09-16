package com.zifang.util.zex.sort;

/**
 * 冒泡排序
 * 冒泡排序是稳定排序
 * 冒泡排序空间复杂度O(1)
 * 最优时间复杂度O(n)，当序列已经排序好时，时间复杂度为O(n)
 * 最坏时间复杂度O(n^2)，当序列是倒序时，时间复杂度为O(n^2)
 * 冒泡排序是一种交换排序
 * <p>
 * 冒泡排序原理：
 * 在序列中对当前还未排序的数，自上而下对相邻的两个数依次进行比较和调整，让较大的数往下沉，较小的往上冒。
 * 即：每当两相邻的数比较后发现它们的排序与排序要求相反时，就将它们互换。
 */
public class BubbleSort2 {
    /**
     * sort方法。
     * * @param arr int[]类型参数
     */
    public void sort(int[] arr) {
        int i, j, tmp, len = arr.length;

        //判断是否有交换  
        boolean swap;

        for (i = 0; i < len; i++) {
            swap = false;

            //在arr[i] ~ arr[len - 1]中找出最小的记录  
            //并把最小的记录从后面交换到arr[i]  
            for (j = len - 1; j > i; j--) {
                if (arr[j] < arr[j - 1]) {
                    swap = true;
                    tmp = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = tmp;
                }
            }

            if (!swap) {
                //如果没有交换，则序列已经有序，跳出循环  
                break;
            }
        }
    }

    /**
     * 冒泡排序改进
     *
     * @param arr 待排序数组
     */
    public void sortAdv(int[] arr) {
        int len = arr.length;
        int i = 0, j, tmp, pos;

        while (i < len) {
            //用pos来记录最后交换的位置  
            pos = 0;

            //在arr[i] ~ arr[len - 1]中找出最小的记录  
            //并把最小的记录从后面交换到arr[i]  
            for (j = len - 1; j > i; j--) {
                if (arr[j] < arr[j - 1]) {
                    pos = j;
                    tmp = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = tmp;
                }
            }

            if (pos == 0) {
                //如果pos为0，表示没有交换，此时序列已经有序，跳出循环  
                break;
            } else {
                //下一次循环从最后一次交换的地方开始  
                i = pos;
            }
        }
    }
}  