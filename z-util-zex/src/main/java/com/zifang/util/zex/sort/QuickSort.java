package com.zifang.util.zex.sort;

import java.util.Stack;

/**
 * 快速排序算法实现类。
 * <p>
 * 快速排序是一种不稳定的交换排序算法，是目前基于比较的内部排序中被认为是最好的方法。
 * 当待排序的关键字是随机分布时，快速排序的平均时间最短。
 * <p>
 * 算法复杂度：
 * <ul>
 *   <li>最佳时间复杂度：O(nlogn)</li>
 *   <li>平均时间复杂度：O(nlogn)</li>
 *   <li>最坏时间复杂度：O(n^2) - 当序列已经有序时</li>
 *   <li>空间复杂度：O(logn)~O(n) - 递归栈空间</li>
 * </ul>
 * <p>
 * 算法原理：
 * 通过一趟排序将要排序的数据分割成独立的两部分，其中一部分的所有数据都比另外一部分的所有数据都要小，
 * 然后再按此方法对这两部分数据分别进行快速排序，整个排序过程可以递归进行，以此达到整个数据变成有序序列。
 *
 * @author zifang
 * @version 1.0
 */
public class QuickSort {

    /**
     * 对整数数组进行快速排序（递归版本）。
     *
     * @param arr 待排序的整数数组
     */
    public void sort(int[] arr) {
        int start = 0, end = arr.length - 1;
        sort(arr, start, end);
    }

    /**
     * 对序列进行快速排序（递归版本）。
     *
     * @param arr   待排序数组
     * @param start 排序开始索引
     * @param end   排序结束索引
     */
    public void sort(int[] arr, int start, int end) {
        int split;
        if (start < end) {
            split = partition(arr, start, end);
            sort(arr, start, split - 1);
            sort(arr, split + 1, end);
        }
    }

    /**
     * 对序列进行划分，返回基准元素的位置。
     *
     * @param arr   待排序数组
     * @param start 排序开始索引
     * @param end   排序结束索引
     * @return 返回用于比较的基准元素的索引
     */
    private int partition(int[] arr, int start, int end) {
        int privot = arr[start];

        while (start < end) {
            while (start < end && arr[end] >= privot) {
                end--;
            }

            swap(arr, start, end);

            while (start < end && arr[start] <= privot) {
                start++;
            }

            swap(arr, start, end);
        }

        return start;
    }

    /**
     * 交换数组中的两个元素。
     *
     * @param arr 数组
     * @param i   元素索引i
     * @param j   元素索引j
     */
    private void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * 非递归快速排序。
     * <p>
     * 非递归快速排序就是用栈来模拟递归方法保存快速排序的边界。
     * 用栈模拟递归实现快速排序比递归快速排序慢。
     *
     * @param arr 待排序的整数数组
     */
    public void sortStack(int[] arr) {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(0);
        stack.push(arr.length - 1);

        int start, end, index;

        while (!stack.empty()) {
            end = stack.pop();
            start = stack.pop();
            index = partition(arr, start, end);

            if (start < index - 1) {
                stack.push(start);
                stack.push(index - 1);
            }

            if (end > index + 1) {
                stack.push(index + 1);
                stack.push(end);
            }
        }
    }
}
