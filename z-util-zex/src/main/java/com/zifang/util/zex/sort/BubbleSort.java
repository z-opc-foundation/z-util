package com.zifang.util.zex.sort;

/**
 * 冒泡排序算法实现类。
 * <p>
 * 冒泡排序是一种稳定的交换排序算法。
 * 参与排序的数据，每两个相邻的数据进行比较，如果前一个数大于后一个数，就进行交换，
 * 否则不进行交换，然后进行下一轮的比较，直至排序完成。
 * <p>
 * 算法复杂度：
 * <ul>
 *   <li>最好时间复杂度：O(n) - 当数组已经有序时</li>
 *   <li>最坏时间复杂度：O(n^2) - 当数组倒序时</li>
 *   <li>平均时间复杂度：O(n^2)</li>
 *   <li>空间复杂度：O(1)</li>
 * </ul>
 * <p>
 * 算法原理：
 * n个数据要进行n-1轮比较，每一轮中每两个相邻数据进行比较，若前者大于后者，则进行交换，否则不进行交换。
 * 本轮比较结束之后，参与本轮比较的最大数排在本轮数据的最后一位。
 * 第j轮进行比较的次数是n-1-j。
 *
 * @author zifang
 * @version 1.0
 */
public class BubbleSort {

    /**
     * 对整数数组进行冒泡排序。
     * <p>
     * 该方法从数组第一个元素开始，相邻元素两两比较，
     * 如果前者大于后者则交换位置，每轮比较后最大元素移至末尾。
     * 如果某轮比较没有发生交换，说明数组已有序，可提前结束。
     *
     * @param arr 待排序的整数数组，不能为null
     * @return 排序后的整数数组（实际为原数组本身，排序在原数组上进行）
     */
    public static int[] bubbleSort(int[] arr) {
        int m = 0;    //循环次数
        int n = 0;    //交换次数
        int l = arr.length;
        boolean flag = true;
        for (int i = 1; i < l; i++) {
            flag = true;
            for (int j = 0; j < l - i; j++) {
                m++;
                if (arr[j] > arr[j + 1]) {
                    n++;
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    flag = false;
                }
            }
            //判断是否已经排好序，如果已经拍好序直接退出循环。判断是否排好序条件：内层循环都没有发生过交换
            if (flag) {
                break;
            }
        }
        System.out.println("循环次数：" + m + "\t交换次数：" + n);
        return arr;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
//		int[] arrays = new int[]{0,1,2,3,8,7,9,6,4,5};
//		int[] arrays = new int[]{0,1,2,3,4,5,6,7,8,9};	//循环9次，交换0次
        int[] arrays = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0};    //循环45次,交换45次

        int[] arrSort = bubbleSort(arrays);
        for (int i = 0; i < arrSort.length; i++) {
            System.out.println(arrSort[i]);
        }
    }
}
