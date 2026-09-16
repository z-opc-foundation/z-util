package com.zifang.util.pandas.matrix;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Matrix 类测试
 */

/**
 * MatrixTest类。
 */
public class MatrixTest {

    @Test
    /**
     * testCreateEmptyMatrix方法。
     */
    public void testCreateEmptyMatrix() {
        Matrix matrix = new Matrix();
        assertNotNull(matrix);
    }

    @Test
    /**
     * testSetRow方法。
     */
    public void testSetRow() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testSetMultipleRows方法。
     */
    public void testSetMultipleRows() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);
        matrix.set(4.0, 5.0, 6.0);
        matrix.set(7.0, 8.0, 9.0);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testSetSingleElement方法。
     */
    public void testSetSingleElement() {
        Matrix matrix = new Matrix();
        matrix.set(5.0);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testSetEmptyRow方法。
     */
    public void testSetEmptyRow() {
        Matrix matrix = new Matrix();
        matrix.set();
        assertNotNull(matrix);
    }

    @Test
    /**
     * testMultiplyNotImplemented方法。
     */
    public void testMultiplyNotImplemented() {
        Matrix matrix1 = new Matrix();
        matrix1.set(1.0, 2.0);
        matrix1.set(3.0, 4.0);

        Matrix matrix2 = new Matrix();
        matrix2.set(5.0, 6.0);
        matrix2.set(7.0, 8.0);

        // 当前方法为空实现，调用不应抛出异常
        matrix1.multiply(matrix2);
    }

    @Test
    /**
     * testFormatNotImplemented方法。
     */
    public void testFormatNotImplemented() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);
        matrix.set(4.0, 5.0, 6.0);

        // 当前方法为空实现，调用不应抛出异常
        matrix.format();
    }

    @Test
    /**
     * testShapeNotImplemented方法。
     */
    public void testShapeNotImplemented() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);
        matrix.set(4.0, 5.0, 6.0);

        // 当前方法为空实现，调用不应抛出异常
        matrix.shape();
    }

    @Test
    /**
     * testDtypeNotImplemented方法。
     */
    public void testDtypeNotImplemented() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);

        // 当前方法为空实现，调用不应抛出异常
        matrix.dtype();
    }

    @Test
    /**
     * testNdimNotImplemented方法。
     */
    public void testNdimNotImplemented() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);

        // 当前方法为空实现，调用不应抛出异常
        matrix.ndim();
    }

    @Test
    /**
     * testSliceNotImplemented方法。
     */
    public void testSliceNotImplemented() {
        Matrix matrix = new Matrix();
        matrix.set(1.0, 2.0, 3.0);
        matrix.set(4.0, 5.0, 6.0);

        // 当前方法为空实现
        List<List<Double>> result = matrix.slice();
        assertNull(result);
    }

    @Test
    /**
     * testWithNegativeValues方法。
     */
    public void testWithNegativeValues() {
        Matrix matrix = new Matrix();
        matrix.set(-1.0, -2.0, -3.0);
        matrix.set(-4.0, -5.0, -6.0);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testWithMixedValues方法。
     */
    public void testWithMixedValues() {
        Matrix matrix = new Matrix();
        matrix.set(0.0, -1.5, 2.5);
        matrix.set(-3.7, 4.2, 0.0);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testWithVeryLargeValues方法。
     */
    public void testWithVeryLargeValues() {
        Matrix matrix = new Matrix();
        matrix.set(Double.MAX_VALUE, Double.MIN_VALUE);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testWithVerySmallValues方法。
     */
    public void testWithVerySmallValues() {
        Matrix matrix = new Matrix();
        matrix.set(1e-308, 1e-309);
        assertNotNull(matrix);
    }

    @Test
    /**
     * testMultipleRowsAndColumns方法。
     */
    public void testMultipleRowsAndColumns() {
        Matrix matrix = new Matrix();
        // 创建 4x5 矩阵
        for (int i = 0; i < 4; i++) {
            matrix.set((double) i * 5 + 1, (double) i * 5 + 2, (double) i * 5 + 3,
                    (double) i * 5 + 4, (double) i * 5 + 5);
        }
        assertNotNull(matrix);
    }
}
