package com.zifang.util.pandas.interpolate;

import com.zifang.util.pandas.DataFrame;
import com.zifang.util.pandas.Series;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Interpolator 插值功能测试类
 * 测试线性插值、前向填充、后向填充等功能
 */

/**
 * InterpolatorTest类。
 */
public class InterpolatorTest {

    @Test
    /**
     * testLinearInterpolation方法。
     */
    public void testLinearInterpolation() {
        // 创建带有缺失值的 Series
        double[] data = {1.0, Double.NaN, 3.0, Double.NaN, 5.0};
        Series series = new Series(data);

        Series result = Interpolator.linear(series);

        assertEquals(5, result.length());
        assertEquals(1.0, result.toArray()[0], 0.001);
        assertEquals(2.0, result.toArray()[1], 0.001); // 插值: (1+3)/2
        assertEquals(3.0, result.toArray()[2], 0.001);
        assertEquals(4.0, result.toArray()[3], 0.001); // 插值: (3+5)/2
        assertEquals(5.0, result.toArray()[4], 0.001);
    }

    @Test
    /**
     * testForwardFill方法。
     */
    public void testForwardFill() {
        double[] data = {1.0, Double.NaN, Double.NaN, 4.0, Double.NaN};
        Series series = new Series(data);

        Series result = Interpolator.forward(series);

        assertEquals(5, result.length());
        assertEquals(1.0, result.toArray()[0], 0.001);
        assertEquals(1.0, result.toArray()[1], 0.001); // 前向填充
        assertEquals(1.0, result.toArray()[2], 0.001); // 前向填充
        assertEquals(4.0, result.toArray()[3], 0.001);
        assertEquals(4.0, result.toArray()[4], 0.001); // 前向填充
    }

    @Test
    /**
     * testBackwardFill方法。
     */
    public void testBackwardFill() {
        double[] data = {Double.NaN, 2.0, Double.NaN, Double.NaN, 5.0};
        Series series = new Series(data);

        Series result = Interpolator.backward(series);

        assertEquals(5, result.length());
        assertEquals(2.0, result.toArray()[0], 0.001); // 后向填充
        assertEquals(2.0, result.toArray()[1], 0.001);
        assertEquals(5.0, result.toArray()[2], 0.001); // 后向填充
        assertEquals(5.0, result.toArray()[3], 0.001); // 后向填充
        assertEquals(5.0, result.toArray()[4], 0.001);
    }

    @Test
    /**
     * testLinearWithNoNaN方法。
     */
    public void testLinearWithNoNaN() {
        // 测试没有缺失值的情况
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        Series series = new Series(data);

        Series result = Interpolator.linear(series);

        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], result.toArray()[i], 0.001);
        }
    }

    @Test
    /**
     * testLinearWithAllNaN方法。
     */
    public void testLinearWithAllNaN() {
        // 测试全部缺失值的情况
        double[] data = {Double.NaN, Double.NaN, Double.NaN};
        Series series = new Series(data);

        Series result = Interpolator.linear(series);

        // 应该保持为 NaN
        for (int i = 0; i < data.length; i++) {
            assertTrue(Double.isNaN(result.toArray()[i]));
        }
    }

    @Test
    /**
     * testDataFrameInterpolate方法。
     */
    public void testDataFrameInterpolate() {
        // 创建带有缺失值的 DataFrame
        java.util.Map<String, double[]> data = new java.util.LinkedHashMap<>();
        data.put("A", new double[]{1.0, Double.NaN, 3.0, Double.NaN, 5.0});
        data.put("B", new double[]{Double.NaN, 2.0, 3.0, 4.0, Double.NaN});

        DataFrame df = new DataFrame(data);
        DataFrame result = df.interpolate();

        assertEquals(5, result.nRows());
        assertEquals(2, result.nCols());

        // 验证 A 列插值结果
        Series colA = result.get("A");
        assertEquals(1.0, colA.toArray()[0], 0.001);
        assertEquals(2.0, colA.toArray()[1], 0.001); // (1+3)/2
        assertEquals(3.0, colA.toArray()[2], 0.001);
        assertEquals(4.0, colA.toArray()[3], 0.001); // (3+5)/2
        assertEquals(5.0, colA.toArray()[4], 0.001);
    }

    @Test
    /**
     * testForwardFillDataFrame方法。
     */
    public void testForwardFillDataFrame() {
        java.util.Map<String, double[]> data = new java.util.LinkedHashMap<>();
        data.put("A", new double[]{1.0, Double.NaN, Double.NaN, 4.0, Double.NaN});

        DataFrame df = new DataFrame(data);
        DataFrame result = df.interpolate("forward");

        Series colA = result.get("A");
        assertEquals(1.0, colA.toArray()[0], 0.001);
        assertEquals(1.0, colA.toArray()[1], 0.001); // 前向填充
        assertEquals(1.0, colA.toArray()[2], 0.001); // 前向填充
        assertEquals(4.0, colA.toArray()[3], 0.001);
        assertEquals(4.0, colA.toArray()[4], 0.001); // 前向填充
    }
}
