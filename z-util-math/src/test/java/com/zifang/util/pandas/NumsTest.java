package com.zifang.util.pandas;

import com.zifang.util.pandas.num.Num;
import com.zifang.util.pandas.num.Nums;
import org.junit.Test;

/**
 * NumsTest类。
 */
public class NumsTest {

    @Test
    /**
     * test方法。
     */
    public void test() {
        Number[][][] arr = {{{1, 2}, {3}}, {{4}, {5}}, {{6}, {7, 8}}};
        Num num = Nums.array(arr);

        assert num.nDim() == 3;
        // assert ArraysUtil.isDeeplyEqual(new int[]{3, 2, 2}, num.shape());
        // 实际叶子元素 2+1 + 1+1 + 1+2 = 8。原先断言 12（3×2×2），
        // 但 shape 是沿第 0 个元素下钻得到的，对不规则数组取到的是首分支长度，
        // 拿它相乘会数出 4 个根本不存在的元素。size 按实际叶子元素个数统计。
        assert num.size() == 8;
        System.out.println(num);
    }

    @Test
    /**
     * test2方法。
     */
    public void test2() {
        Num num = Nums.zeros(new Integer[]{2, 2}, null);
        System.out.println(num);
        System.out.println();
    }
}
