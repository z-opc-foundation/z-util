package com.zifang.util.zex.sort;


/**
 * 这个算法是Arrays.java中给基本类型的数据排序使用的具体实现。它针对每种基本类型都做了实现，实现的方式有稍微的差异，但是思路都是相同的，所以这里只挑了int类型的排序来看。
 * 整个实现中的思路是 首先检查数组的长度，比一个阈值小的时候直接使用双轴快排。其它情况下，先检查数组中数据的顺序连续性。把数组中连续升序或者连续降序的信息记录下来，顺便把连续降序的部分倒置。这样数据就被切割成一段段连续升序的数列。
 * 如果顺序连续性好，直接使用TimSort算法。这个我们之前介绍过，TimSort算法的核心在于利用数列中的原始顺序，所以可以提高很多效率。这里的TimSort算法是之前介绍的TimSort算法的精简版，剪掉了动态阈值的那一部分。
 * 顺序连续性不好的数组直接使用了 双轴快排 + 成对插入排序。成对插入排序是插入排序的改进版，它采用了同时插入两个元素的方式调高效率。双轴快排是从传统的单轴快排到3-way快排演化过来的，网上之前已经有很多博客介绍这种算法。这里推荐 国外一篇文章，它的3张图和下面的代码帮助我理解了快排，3-way和双轴快排之间的关系。
 * 代码风格来看感觉不如之前TimSort的代码风格好。代码中的变量命名大部分都是a, b, i, k, j, t这种，让人不好理解。所以建议大家日常写代码也不要使用这种不明含义的命名。最好能做到让其它人一看就懂，比如说用index代替i, 用 temp代替t等等。好在它的核心代码部分注释很全，看起来到不麻烦。
 * *
 */
public class DualPivotQuicksort {


    /**
     * 待合并的序列的最大数量
     */
    private static final int MAX_RUN_COUNT = 67;
    /**
     * 待合并的序列的最大长度
     */
    private static final int MAX_RUN_LENGTH = 33;
    /**
     * 如果参与排序的数组长度小于这个值，优先使用快速排序而不是归并排序
     */
    private static final int QUICKSORT_THRESHOLD = 286;
    /**
     * 如果参与排序的数组长度小于这个值，有限考虑插入排序，而不是快速排序
     */
    private static final int INSERTION_SORT_THRESHOLD = 47;

    /**
     * 保护这个类不被实例化
     */
    private DualPivotQuicksort() {
    }

    /**
     * 给指定数组排序
     *
     * @param a 指定的数组
     */
    public static void sort(int[] a) {
        sort(a, 0, a.length - 1);
    }

    /**
     * 给指定数组的指定范围排序
     *
     * @param a     指定的数组
     * @param left  指定范围的第一个元素(包括)
     * @param right 指定范围的最后一个元素(不包括)
     */
    public static void sort(int[] a, int left, int right) {

        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /**
         * run[i] 意味着第i个有序数列开始的位置，（升序或者降序）
         **/
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0;
        run[0] = left;

        // 检查数组是不是已经接近有序状态
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]) ;
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]) ;
                //如果是降序的，找出k之后，把数列倒置
                for (int lo = run[count], hi = k; ++lo < --hi; ) {
                    int t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    // 数列中有至少MAX_RUN_LENGTH的数据相等的时候，直接使用快排。
                    // 这里为什么这么处理呢？
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /**
             * 数组并非高度有序，使用快速排序,因为数组中有序数列的个数超过了MAX_RUN_COUNT
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        //检查特殊情况
        if (run[count] == right++) { // 最后一个有序数列只有最后一个元素
            run[++count] = right; // 那给最后一个元素的后面加一个哨兵
        } else if (count == 1) { // 整个数组中只有一个有序数列，说明数组已经有序啦，不需要排序了
            return;
        }

        /**
         * 创建合并用的临时数组。
         * 注意： 这里变量right被加了1，它在数列最后一个元素位置+1的位置
         * 这里没看懂，没发现后面的奇数处理和偶数处理有什么不同
         */
        int[] b;
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1) ;

        if (odd == 0) {
            b = a;
            a = new int[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]) ;
        } else {
            b = new int[a.length];
        }

        // 合并
        // 最外层循环，直到count为1，也就是栈中待合并的序列只有一个的时候，标志合并成功
        // a 做原始数组，b 做目标数组
        for (int last; count > 1; count = last) {
            // 遍历数组，合并相邻的两个升序序列
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                // 合并run[k-2] 与 run[k-1]两个序列
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    // 这里我给源码加了一个括号，这样好理解一点。 之前总觉得它会出现数组越界问题，
                    // 后来加了这个括号之后发现是没有问题的
                    if (q >= hi || (p < mi && a[p] <= a[q])) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                // 这里把合并之后的数列往前移动
                run[++last] = hi;
            }
            // 如果栈的长度为奇数，那么把最后落单的有序数列copy过对面
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo; b[i] = a[i]) ;
                run[++last] = right;
            }
            //临时数组，与原始数组对调，保持a做原始数组，b 做目标数组
            int[] t = a;
            a = b;
            b = t;
        }

    }

    /**
     * 使用双轴快速排序给指定数组的指定范围排序
     *
     * @param a        参与排序的数组
     * @param left     范围内最左边的元素的位置(包括该元素)
     * @param right    范围内最右边的元素的位置(包括该元素)
     * @param leftmost 指定的范围是否在数组的最左边
     */
    private static void sort(int[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 小数组使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /**
                 * 经典的插入排序算法，不带哨兵。做了优化，在leftmost情况下使用
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    int ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {

                /**
                 * 首先跨过开头的升序的部分
                 */
                do {
                    if (left > right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);

                /**
                 * 这里用到了成对插入排序方法，它比简单的插入排序算法效率要高一些
                 * 因为这个分支执行的条件是左边是有元素的
                 * 所以可以直接从left开始往前查找。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    int a1 = a[k], a2 = a[left];

                    //保证a1>=a2
                    if (a1 < a2) {
                        a2 = a1;
                        a1 = a[left];
                    }
                    //先把两个数字中较大的那个移动到合适的位置
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k]; //这里每次需要向左移动两个元素
                    }
                    a[++k + 1] = a1;
                    //再把两个数字中较小的那个移动到合适的位置
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k]; //这里每次需要向左移动一个元素
                    }
                    a[k + 1] = a2;
                }
                int last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = last;
                }
                a[right + 1] = last;
            }
            return;
        }

        // length / 7 的一种低复杂度的实现, 近似值(length * 9 / 64 + 1)
        int seventh = (length >> 3) + (length >> 6) + 1;

        // 对5段靠近中间位置的数列排序，这些元素最终会被用来做轴(下面会讲)
        // 他们的选定是根据大量数据积累经验确定的
        int e3 = (left + right) >>> 1; //中间值
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        //这里是手写的冒泡排序，没有for循环
        if (a[e2] < a[e1]) {
            int t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            int t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            int t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            int t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }

        //指针
        int less = left;   // 中间区域的首个元素的位置
        int great = right; //右边区域的首个元素的位置
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用5个元素中的2，4两个位置，他们两个大致处在四分位的位置上。
             * 需要注意的是pivot1 <= pivot2
             */
            int pivot1 = a[e2];
            int pivot2 = a[e4];

            /*
             * The first and the last elements to be sorted are moved to the
             * locations formerly occupied by the pivots. When partitioning
             * is complete, the pivots are swapped back into their final
             * positions, and excluded from subsequent sorting.
             * 第一个和最后一个元素被放到两个轴所在的位置。当阶段性的分段结束后
             * 他们会被分配到最终的位置并从子排序阶段排除
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过一些队首的小于pivot1的值，跳过队尾的大于pivot2的值
             */
            while (a[++less] < pivot1) ;
            while (a[--great] > pivot2) ;

            /*
             * Partitioning:
             *
             *   left part           center part                   right part
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * Invariants:
             *
             *              all in (left, less)   < pivot1
             *    pivot1 <= all in [less, k)     <= pivot2
             *              all in (great, right) > pivot2
             *
             * Pointer k is the first index of ?-part.
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                int ak = a[k];
                if (ak < pivot1) { // Move a[k] to left part
                    a[k] = a[less];
                    /*
                     * 这里考虑的好细致，"a[i] = b; i++"的效率要好过
                     * 'a[i++] = b'
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // Move a[k] to right part
                    while (a[great] > pivot2) {
                        if (great-- == k) { // k遇到great本次分割
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 同上，用"a[i]=b;i--"代替"a[i--] = b"
                     */
                    a[great] = ak;
                    --great;
                }
            } // 分割阶段结束出来的位置,上一个outer结束的位置

            // 把两个放在外面的轴放回他们应该在的位置上
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;

            // 把左边和右边递归排序，跟普通的快速排序差不多
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * If center part is too large (comprises > 4/7 of the array),
             * swap internal pivot values to ends.
             * 如果中心区域太大，超过数组长度的 4/7。就先进行预处理，再参与递归排序。
             * 预处理的方法是把等于pivot1的元素统一放到左边，等于pivot2的元素统一
             * 放到右边,最终产生一个不包含pivot1和pivot2的数列，再拿去参与快排中的递归。
             */
            if (less < e1 && e5 < great) {
                /*
                 * Skip elements, which are equal to pivot values.
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * Partitioning:
                 *
                 *   left part         center part                  right part
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * Invariants:
                 *
                 *              all in (*,  less) == pivot1
                 *     pivot1 < all in [less,  k)  < pivot2
                 *              all in (great, *) == pivot2
                 *
                 * Pointer k is the first index of ?-part.
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    int ak = a[k];
                    if (ak == pivot1) { // Move a[k] to left part
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // Move a[k] to right part
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * Even though a[great] equals to pivot1, the
                             * assignment a[less] = pivot1 may be incorrect,
                             * if a[great] and pivot1 are floating-point zeros
                             * of different signs. Therefore in float and
                             * double sorting methods we have to use more
                             * accurate assignment a[less] = a[great].
                             */
                            a[less] = pivot1;
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                } // outer结束的位置
            }

            // Sort center part recursively
            sort(a, less, great, false);

        } else { // 这里选取的5个元素刚好相等，使用传统的3-way快排

            /*
             * 在5个元素中取中值
             */
            int pivot = a[e3];

            /*
             *
             * Partitioning degenerates to the traditional 3-way
             * (or "Dutch National Flag") schema:
             *
             *   left part    center part              right part
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * Invariants:
             *
             *   all in (left, less)   < pivot
             *   all in [less, k)     == pivot
             *   all in (great, right) > pivot
             *
             * Pointer k is the first index of ?-part.
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                int ak = a[k];
                if (ak < pivot) { // 把a[k]移动到左边去，把center区向右滚动一个单位
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 把a[k]移动到右边
                    while (a[great] > pivot) { // 先找到右边最后一个比pivot小的值
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot ，把他移到左边
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot //如果相等，中心区直接扩展
                        /*
                         * 这里因为是整型值，所以a[k] == a[less] == pivot;
                         */
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * 左右两边还没有完全排序，所以递归解决
             * 中心区只有一个值，不再需要排序
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
}