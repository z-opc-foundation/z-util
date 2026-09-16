package com.zifang.util.pandas.stats;

import com.zifang.util.pandas.DataFrame;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Correlation 相关性分析测试类
 * 测试相关系数计算和协方差矩阵功能
 */

/**
 * CorrelationTest类。
 */
public class CorrelationTest {

    @Test
    /**
     * testPerfectPositiveCorrelation方法。
     */
    public void testPerfectPositiveCorrelation() {
        // 完全正相关
        Map<String, double[]> data = new HashMap<>();
        data.put("X", new double[]{1, 2, 3, 4, 5});
        data.put("Y", new double[]{2, 4, 6, 8, 10}); // Y = 2X

        DataFrame df = new DataFrame(data);
        DataFrame corr = Correlation.corr(df);

        // 对角线应该是 1.0
        assertEquals(1.0, corr.get("X").toArray()[0], 0.001);
        assertEquals(1.0, corr.get("Y").toArray()[1], 0.001);

        // X和Y之间应该是完全正相关
        assertEquals(1.0, Math.abs(corr.get("Y").toArray()[0]), 0.001);
    }

    @Test
    /**
     * testPerfectNegativeCorrelation方法。
     */
    public void testPerfectNegativeCorrelation() {
        // 完全负相关
        Map<String, double[]> data = new HashMap<>();
        data.put("X", new double[]{1, 2, 3, 4, 5});
        data.put("Y", new double[]{10, 8, 6, 4, 2}); // Y = -2X + 12

        DataFrame df = new DataFrame(data);
        DataFrame corr = Correlation.corr(df);

        // X和Y之间应该是完全负相关
        assertEquals(-1.0, corr.get("Y").toArray()[0], 0.001);
    }

    @Test
    /**
     * testNoCorrelation方法。
     */
    public void testNoCorrelation() {
        // 无明显相关性
        Map<String, double[]> data = new HashMap<>();
        data.put("X", new double[]{1, 2, 3, 4, 5});
        data.put("Y", new double[]{1, 1, 1, 1, 1}); // Y 是常数

        DataFrame df = new DataFrame(data);
        DataFrame corr = Correlation.corr(df);

        // 当Y为常数时，相关系数应该是NaN
        assertTrue(Double.isNaN(corr.get("Y").toArray()[0]));
    }

    @Test
    /**
     * testCovarianceMatrix方法。
     */
    public void testCovarianceMatrix() {
        Map<String, double[]> data = new HashMap<>();
        data.put("X", new double[]{1, 2, 3, 4, 5});
        data.put("Y", new double[]{2, 4, 6, 8, 10});

        DataFrame df = new DataFrame(data);
        DataFrame cov = Correlation.cov(df);

        assertEquals(2, cov.nCols());
        assertEquals(2, cov.nRows());

        // 对角线应该是方差（正值）
        assertTrue(cov.get("X").toArray()[0] > 0);
        assertTrue(cov.get("Y").toArray()[1] > 0);
    }

    @Test
    /**
     * testMultipleColumns方法。
     */
    public void testMultipleColumns() {
        // 测试多列数据
        Map<String, double[]> data = new HashMap<>();
        data.put("A", new double[]{1, 2, 3, 4, 5});
        data.put("B", new double[]{2, 4, 6, 8, 10});
        data.put("C", new double[]{3, 6, 9, 12, 15});

        DataFrame df = new DataFrame(data);
        DataFrame corr = Correlation.corr(df);

        assertEquals(3, corr.nCols());
        assertEquals(3, corr.nRows());

        // 所有列之间应该都是完全正相关
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1.0, corr.toArray()[i][j], 0.001);
                } else {
                    assertEquals(1.0, Math.abs(corr.toArray()[i][j]), 0.001);
                }
            }
        }
    }
}
