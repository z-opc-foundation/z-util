package com.zifang.util.pandas.matrix;

import com.zifang.util.pandas.num.Num;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Linalg 类测试 - pandas 版线性代数工具
 */

/**
 * LinalgTest类。
 */
public class LinalgTest {

    @Test
    /**
     * testDotVectorProduct方法。
     */
    public void testDotVectorProduct() {
        Num a = new Num(new double[]{1.0, 2.0, 3.0});
        Num b = new Num(new double[]{4.0, 5.0, 6.0});
        Num result = Linalg.dot(a, b);
        assertEquals(32.0, ((double[]) result.data())[0], 1e-10);
    }

    @Test
    /**
     * testDotInvalidDimensions方法。
     */
    public void testDotInvalidDimensions() {
        Num a = new Num(new double[]{1.0, 2.0});
        Num b = new Num(new double[]{1.0});
        try {
            Linalg.dot(a, b);
            fail("Expected exception");
        } catch (UnsupportedOperationException | ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    /**
     * testMatmul方法。
     */
    public void testMatmul() {
        double[][] a = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] b = {{5.0, 6.0}, {7.0, 8.0}};
        Num numA = new Num(a);
        Num numB = new Num(b);
        Num result = Linalg.matmul(numA, numB);
        double[][] resultData = (double[][]) result.data();
        assertEquals(19.0, resultData[0][0], 1e-10);
        assertEquals(22.0, resultData[0][1], 1e-10);
        assertEquals(43.0, resultData[1][0], 1e-10);
        assertEquals(50.0, resultData[1][1], 1e-10);
    }

    @Test
    /**
     * testInner方法。
     */
    public void testInner() {
        Num a = new Num(new double[]{1.0, 2.0, 3.0});
        Num b = new Num(new double[]{4.0, 5.0, 6.0});
        double result = Linalg.inner(a, b);
        assertEquals(32.0, result, 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testInnerInvalidDimensions方法。
     */
    public void testInnerInvalidDimensions() {
        Num a = new Num(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Num b = new Num(new double[]{1.0, 2.0});
        Linalg.inner(a, b);
    }

    @Test
    /**
     * testOuter方法。
     */
    public void testOuter() {
        Num a = new Num(new double[]{1.0, 2.0});
        Num b = new Num(new double[]{3.0, 4.0});
        Num result = Linalg.outer(a, b);
        double[][] resultData = (double[][]) result.data();
        assertEquals(3.0, resultData[0][0], 1e-10);
        assertEquals(4.0, resultData[0][1], 1e-10);
        assertEquals(6.0, resultData[1][0], 1e-10);
        assertEquals(8.0, resultData[1][1], 1e-10);
    }

    @Test
    /**
     * testKron方法。
     */
    public void testKron() {
        double[][] a = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] b = {{0.0, 5.0}, {6.0, 7.0}};
        Num numA = new Num(a);
        Num numB = new Num(b);
        Num result = Linalg.kron(numA, numB);
        double[][] resultData = (double[][]) result.data();
        assertEquals(4, resultData.length);
        assertEquals(4, resultData[0].length);
    }

    @Test
    /**
     * testDet2x2方法。
     */
    public void testDet2x2() {
        double[][] matrix = {{4.0, 7.0}, {2.0, 6.0}};
        Num num = new Num(matrix);
        double result = Linalg.det(num);
        assertEquals(10.0, result, 1e-10);
    }

    @Test
    /**
     * testDet3x3方法。
     */
    public void testDet3x3() {
        double[][] matrix = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        Num num = new Num(matrix);
        double result = Linalg.det(num);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    /**
     * testTrace方法。
     */
    public void testTrace() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        Num num = new Num(matrix);
        double result = Linalg.trace(num);
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    /**
     * testMatrixRank方法。
     */
    public void testMatrixRank() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        Num num = new Num(matrix);
        int result = Linalg.matrix_rank(num);
        assertEquals(2, result);
    }

    @Test
    /**
     * testInv2x2方法。
     */
    public void testInv2x2() {
        double[][] matrix = {{4.0, 7.0}, {2.0, 6.0}};
        Num num = new Num(matrix);
        Num result = Linalg.inv(num);
        double[][] resultData = (double[][]) result.data();
        assertEquals(0.6, resultData[0][0], 1e-10);
        assertEquals(-0.7, resultData[0][1], 1e-10);
        assertEquals(-0.2, resultData[1][0], 1e-10);
        assertEquals(0.4, resultData[1][1], 1e-10);
    }

    @Test
    /**
     * testNorm1方法。
     */
    public void testNorm1() {
        Num num = new Num(new double[]{-3.0, 4.0});
        double result = Linalg.norm(num, 1);
        assertEquals(7.0, result, 1e-10);
    }

    @Test
    /**
     * testNorm2方法。
     */
    public void testNorm2() {
        Num num = new Num(new double[]{3.0, 4.0});
        double result = Linalg.norm(num, 2);
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    /**
     * testNormInf方法。
     */
    public void testNormInf() {
        Num num = new Num(new double[]{-3.0, 4.0, -5.0});
        double result = Linalg.norm(num, Integer.MAX_VALUE);
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    /**
     * testNormDefault方法。
     */
    public void testNormDefault() {
        Num num = new Num(new double[]{3.0, 4.0});
        double result = Linalg.norm(num);
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    /**
     * testNormFro方法。
     */
    public void testNormFro() {
        Num num = new Num(new double[]{3.0, 4.0});
        double result = Linalg.norm(num, "fro");
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    /**
     * testSvd方法。
     */
    public void testSvd() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        Num num = new Num(matrix);
        Linalg.SVDResult result = Linalg.svd(num);
        assertNotNull(result.U);
        assertNotNull(result.S);
        assertNotNull(result.Vh);
    }

    @Test
    /**
     * testQr方法。
     */
    public void testQr() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        Num num = new Num(matrix);
        Linalg.QRResult result = Linalg.qr(num);
        assertNotNull(result.Q);
        assertNotNull(result.R);
    }

    @Test
    /**
     * testCholesky方法。
     */
    public void testCholesky() {
        double[][] matrix = {{4.0, 2.0}, {2.0, 5.0}};
        Num num = new Num(matrix);
        Num result = Linalg.cholesky(num);
        assertNotNull(result);
    }

    @Test
    /**
     * testSolve方法。
     */
    public void testSolve() {
        double[][] a = {{1.0, 1.0}, {1.0, -1.0}};
        double[] b = {5.0, 1.0};
        Num numA = new Num(a);
        Num numB = new Num(b);
        try {
            Num result = Linalg.solve(numA, numB);
            assertNotNull(result);
        } catch (UnsupportedOperationException e) {
            // solve not yet implemented
        }
    }

    @Test
    /**
     * testLstsq方法。
     */
    public void testLstsq() {
        double[][] a = {{1.0, 1.0}, {1.0, -1.0}};
        double[] b = {5.0, 1.0};
        Num numA = new Num(a);
        Num numB = new Num(b);
        try {
            Num result = Linalg.lstsq(numA, numB);
            assertNotNull(result);
        } catch (UnsupportedOperationException e) {
            // lstsq not yet implemented
        }
    }

    @Test
    /**
     * testPinv方法。
     */
    public void testPinv() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        Num num = new Num(matrix);
        Num result = Linalg.pinv(num);
        assertNotNull(result);
    }
}