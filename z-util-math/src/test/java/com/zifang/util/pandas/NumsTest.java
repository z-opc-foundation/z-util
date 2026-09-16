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
        assert num.size() == 12;
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
