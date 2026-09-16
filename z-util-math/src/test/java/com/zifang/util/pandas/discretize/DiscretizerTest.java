package com.zifang.util.pandas.discretize;

import com.zifang.util.pandas.Series;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Discretizer 数据离散化和分箱测试类
 * 测试等宽分箱(cut)和等频分箱(qcut)功能
 */

/**
 * DiscretizerTest类。
 */
public class DiscretizerTest {

    @Test
    /**
     * testCutBasic方法。
     */
    public void testCutBasic() {
        // 创建测试数据: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Series series = new Series(data);

        // 分成 2 个箱子
        Series result = Discretizer.cut(series, 2);

        assertEquals(10, result.length());

        // 1-5 应该在箱子 0，6-10 应该在箱子 1
        for (int i = 0; i < 5; i++) {
            assertEquals(0.0, result.toArray()[i], 0.001);
        }
        for (int i = 5; i < 10; i++) {
            assertEquals(1.0, result.toArray()[i], 0.001);
        }
    }

    @Test
    /**
     * testCutMultipleBins方法。
     */
    public void testCutMultipleBins() {
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Series series = new Series(data);

        // 分成 5 个箱子，每个箱子宽度为 2
        Series result = Discretizer.cut(series, 5);

        assertEquals(10, result.length());

        // 1-2: 箱子 0, 3-4: 箱子 1, 5-6: 箱子 2, 7-8: 箱子 3, 9-10: 箱子 4
        assertEquals(0.0, result.toArray()[0], 0.001); // 1
        assertEquals(0.0, result.toArray()[1], 0.001); // 2
        assertEquals(1.0, result.toArray()[2], 0.001); // 3
        assertEquals(1.0, result.toArray()[3], 0.001); // 4
        assertEquals(2.0, result.toArray()[4], 0.001); // 5
        assertEquals(2.0, result.toArray()[5], 0.001); // 6
        assertEquals(3.0, result.toArray()[6], 0.001); // 7
        assertEquals(3.0, result.toArray()[7], 0.001); // 8
        assertEquals(4.0, result.toArray()[8], 0.001); // 9
        assertEquals(4.0, result.toArray()[9], 0.001); // 10
    }

    @Test
    /**
     * testCutWithNaN方法。
     */
    public void testCutWithNaN() {
        double[] data = {1, Double.NaN, 3, 4, Double.NaN, 6};
        Series series = new Series(data);

        Series result = Discretizer.cut(series, 2);

        assertEquals(6, result.length());
        assertTrue(Double.isNaN(result.toArray()[1]));
        assertTrue(Double.isNaN(result.toArray()[4]));
    }

    @Test
    /**
     * testQcutBasic方法。
     */
    public void testQcutBasic() {
        // 创建 10 个均匀分布的数据点
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Series series = new Series(data);

        // 分成 2 个分位数箱子
        Series result = Discretizer.qcut(series, 2);

        assertEquals(10, result.length());

        // 每个箱子应该有 5 个数据点
        int count0 = 0, count1 = 0;
        for (int i = 0; i < result.length(); i++) {
            if (!Double.isNaN(result.toArray()[i])) {
                if (result.toArray()[i] == 0.0) count0++;
                else if (result.toArray()[i] == 1.0) count1++;
            }
        }

        assertEquals(5, count0);
        assertEquals(5, count1);
    }

    @Test
    /**
     * testQcutQuartiles方法。
     */
    public void testQcutQuartiles() {
        // 创建 20 个均匀分布的数据点
        double[] data = new double[20];
        for (int i = 0; i < 20; i++) {
            data[i] = i + 1;
        }
        Series series = new Series(data);

        // 分成 4 个分位数箱子（四分位数）
        Series result = Discretizer.qcut(series, 4);

        assertEquals(20, result.length());

        // 每个箱子应该有 5 个数据点
        int[] counts = new int[4];
        for (int i = 0; i < result.length(); i++) {
            if (!Double.isNaN(result.toArray()[i])) {
                int bin = (int) result.toArray()[i];
                if (bin >= 0 && bin < 4) {
                    counts[bin]++;
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            assertEquals(5, counts[i]);
        }
    }

    @Test
    /**
     * testQcutWithDuplicates方法。
     */
    public void testQcutWithDuplicates() {
        // 测试包含重复值的数据
        double[] data = {1, 1, 1, 2, 2, 3, 4, 5, 5, 5};
        Series series = new Series(data);

        Series result = Discretizer.qcut(series, 2);

        assertEquals(10, result.length());

        // 验证所有非NaN值都被分配到了箱子中
        for (int i = 0; i < result.length(); i++) {
            assertFalse(Double.isNaN(result.toArray()[i]));
        }
    }

    @Test
    /**
     * testQcutWithNaN方法。
     */
    public void testQcutWithNaN() {
        double[] data = {1, Double.NaN, 3, 4, Double.NaN, 6};
        Series series = new Series(data);

        Series result = Discretizer.qcut(series, 2);

        assertEquals(6, result.length());
        assertTrue(Double.isNaN(result.toArray()[1]));
        assertTrue(Double.isNaN(result.toArray()[4]));
    }

    @Test
    /**
     * testSingleValue方法。
     */
    public void testSingleValue() {
        // 测试只有一个唯一值的情况
        double[] data = {5, 5, 5, 5, 5};
        Series series = new Series(data);

        // cut 应该将所有值分配到同一个箱子
        Series cutResult = Discretizer.cut(series, 2);
        assertEquals(5, cutResult.length());

        // qcut 可能会出现问题，但应该返回有效的结果
        Series qcutResult = Discretizer.qcut(series, 2);
        assertEquals(5, qcutResult.length());
    }
}
