package com.zifang.util.pandas.num;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Maths 类测试
 */

/**
 * MathsTest类。
 */
public class MathsTest {

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals(Math.PI, Maths.PI, 1e-10);
        assertEquals(Math.E, Maths.E, 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, Maths.INF, 0);
        assertEquals(Double.NEGATIVE_INFINITY, Maths.NINF, 0);
        assertTrue(Double.isNaN(Maths.NAN));
        assertTrue(Maths.EPSILON > 0);
    }

    @Test
    /**
     * testSin方法。
     */
    public void testSin() {
        Num num = new Num(new double[]{0, Math.PI / 6, Math.PI / 2});
        Num result = Maths.sin(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(0.5, ((double[]) result.data())[1], 1e-5);
        assertEquals(1.0, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testCos方法。
     */
    public void testCos() {
        Num num = new Num(new double[]{0, Math.PI / 3, Math.PI / 2});
        Num result = Maths.cos(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(0.5, ((double[]) result.data())[1], 1e-5);
        assertEquals(0.0, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testTan方法。
     */
    public void testTan() {
        Num num = new Num(new double[]{0, Math.PI / 4});
        Num result = Maths.tan(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-5);
    }

    @Test
    /**
     * testArcsin方法。
     */
    public void testArcsin() {
        Num num = new Num(new double[]{0, 0.5, 1.0});
        Num result = Maths.arcsin(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.PI / 6, ((double[]) result.data())[1], 1e-5);
        assertEquals(Math.PI / 2, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testArccos方法。
     */
    public void testArccos() {
        Num num = new Num(new double[]{1.0, 0.5, 0.0});
        Num result = Maths.arccos(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.PI / 3, ((double[]) result.data())[1], 1e-5);
        assertEquals(Math.PI / 2, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testArctan方法。
     */
    public void testArctan() {
        Num num = new Num(new double[]{0.0, 1.0, Double.MAX_VALUE});
        Num result = Maths.arctan(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.PI / 4, ((double[]) result.data())[1], 1e-5);
        assertEquals(Math.PI / 2, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testArctan2方法。
     */
    public void testArctan2() {
        Num y = new Num(new double[]{1.0, 1.0, -1.0});
        Num x = new Num(new double[]{1.0, 0.0, 1.0});
        Num result = Maths.arctan2(y, x);
        assertEquals(Math.PI / 4, ((double[]) result.data())[0], 1e-5);
        assertEquals(Math.PI / 2, ((double[]) result.data())[1], 1e-5);
        assertEquals(-Math.PI / 4, ((double[]) result.data())[2], 1e-5);
    }

    @Test
    /**
     * testSinh方法。
     */
    public void testSinh() {
        Num num = new Num(new double[]{0.0, 1.0});
        Num result = Maths.sinh(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.sinh(1.0), ((double[]) result.data())[1], 1e-10);
    }

    @Test
    /**
     * testCosh方法。
     */
    public void testCosh() {
        Num num = new Num(new double[]{0.0, 1.0});
        Num result = Maths.cosh(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.cosh(1.0), ((double[]) result.data())[1], 1e-10);
    }

    @Test
    /**
     * testTanh方法。
     */
    public void testTanh() {
        Num num = new Num(new double[]{0.0, 1.0});
        Num result = Maths.tanh(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.tanh(1.0), ((double[]) result.data())[1], 1e-10);
    }

    @Test
    /**
     * testExp方法。
     */
    public void testExp() {
        Num num = new Num(new double[]{0.0, 1.0, 2.0});
        Num result = Maths.exp(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(Math.E, ((double[]) result.data())[1], 1e-10);
        assertEquals(Math.exp(2.0), ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testExpm1方法。
     */
    public void testExpm1() {
        Num num = new Num(new double[]{0.0, 1e-10, 0.5});
        Num result = Maths.expm1(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertTrue(((double[]) result.data())[1] > 0);
        assertEquals(Math.expm1(0.5), ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testLog方法。
     */
    public void testLog() {
        Num num = new Num(new double[]{1.0, Math.E, 10.0});
        Num result = Maths.log(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(Math.log(10.0), ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testLog10方法。
     */
    public void testLog10() {
        Num num = new Num(new double[]{1.0, 10.0, 100.0});
        Num result = Maths.log10(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testLog2方法。
     */
    public void testLog2() {
        Num num = new Num(new double[]{1.0, 2.0, 8.0});
        Num result = Maths.log2(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(3.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testLog1p方法。
     */
    public void testLog1p() {
        Num num = new Num(new double[]{0.0, Math.E - 1, 1.0});
        Num result = Maths.log1p(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-5);
        assertEquals(Math.log1p(1.0), ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testSqrt方法。
     */
    public void testSqrt() {
        Num num = new Num(new double[]{0.0, 1.0, 4.0, 9.0});
        Num result = Maths.sqrt(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[2], 1e-10);
        assertEquals(3.0, ((double[]) result.data())[3], 1e-10);
    }

    @Test
    /**
     * testCbrt方法。
     */
    public void testCbrt() {
        Num num = new Num(new double[]{0.0, 1.0, 8.0, 27.0});
        Num result = Maths.cbrt(num);
        assertEquals(0.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[2], 1e-10);
        assertEquals(3.0, ((double[]) result.data())[3], 1e-10);
    }

    @Test
    /**
     * testPow方法。
     */
    public void testPow() {
        Num num = new Num(new double[]{2.0, 3.0});
        Num result = Maths.pow(num, 2.0);
        assertEquals(4.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(9.0, ((double[]) result.data())[1], 1e-10);
    }

    @Test
    /**
     * testPowWithBase方法。
     */
    public void testPowWithBase() {
        Num exponent = new Num(new double[]{2.0, 3.0, 4.0});
        Num result = Maths.pow(2.0, exponent);
        assertEquals(4.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(8.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(16.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testRound方法。
     */
    public void testRound() {
        Num num = new Num(new double[]{1.4, 1.5, 2.5, 3.5});
        Num result = Maths.round(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(3.0, ((double[]) result.data())[2], 1e-10);
        assertEquals(4.0, ((double[]) result.data())[3], 1e-10);
    }

    @Test
    /**
     * testFloor方法。
     */
    public void testFloor() {
        Num num = new Num(new double[]{1.4, 2.9, -1.5});
        Num result = Maths.floor(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(-2.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testCeil方法。
     */
    public void testCeil() {
        Num num = new Num(new double[]{1.4, 2.9, -1.5});
        Num result = Maths.ceil(num);
        assertEquals(2.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(3.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(-1.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testTrunc方法。
     */
    public void testTrunc() {
        Num num = new Num(new double[]{1.4, 2.9, -1.5});
        Num result = Maths.trunc(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(2.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(-2.0, ((double[]) result.data())[2], 1e-10);
    }

    @Test
    /**
     * testAbs方法。
     */
    public void testAbs() {
        Num num = new Num(new double[]{-1.0, 0.0, 1.0, -5.5});
        Num result = Maths.abs(num);
        assertEquals(1.0, ((double[]) result.data())[0], 1e-10);
        assertEquals(0.0, ((double[]) result.data())[1], 1e-10);
        assertEquals(1.0, ((double[]) result.data())[2], 1e-10);
        assertEquals(5.5, ((double[]) result.data())[3], 1e-10);
    }

    @Test
    /**
     * testSign方法。
     */
    public void testSign() {
        Num num = new Num(new double[]{-3.0, 0.0, 5.0});
        Num result = Maths.sign(num);
        double[] arr = (double[]) result.data();
        assertEquals(-1.0, arr[0], 1e-10);
        assertEquals(0.0, arr[1], 1e-10);
        assertEquals(1.0, arr[2], 1e-10);
    }

    @Test
    /**
     * testPositive方法。
     */
    public void testPositive() {
        Num num = new Num(new double[]{-1.0, 0.0, 1.0});
        Num result = Maths.positive(num);
        double[] arr = (double[]) result.data();
        assertEquals(-1.0, arr[0], 1e-10);
        assertEquals(0.0, arr[1], 1e-10);
        assertEquals(1.0, arr[2], 1e-10);
    }

    @Test
    /**
     * testNegative方法。
     */
    public void testNegative() {
        Num num = new Num(new double[]{-1.0, 0.0, 1.0});
        Num result = Maths.negative(num);
        double[] arr = (double[]) result.data();
        assertEquals(1.0, arr[0], 1e-10);
        assertEquals(0.0, arr[1], 1e-10);
        assertEquals(-1.0, arr[2], 1e-10);
    }

    @Test
    /**
     * testMax方法。
     */
    public void testMax() {
        Num num = new Num(new double[]{1.0, 5.0, 3.0, 9.0, 2.0});
        double result = Maths.max(num);
        assertEquals(9.0, result, 1e-10);
    }

    @Test
    /**
     * testMin方法。
     */
    public void testMin() {
        Num num = new Num(new double[]{1.0, 5.0, 3.0, 9.0, 2.0});
        double result = Maths.min(num);
        assertEquals(1.0, result, 1e-10);
    }

    @Test
    /**
     * testSum方法。
     */
    public void testSum() {
        Num num = new Num(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        double result = Maths.sum(num);
        assertEquals(15.0, result, 1e-10);
    }

    @Test
    /**
     * testMean方法。
     */
    public void testMean() {
        Num num = new Num(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        double result = Maths.mean(num);
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    /**
     * testStd方法。
     */
    public void testStd() {
        Num num = new Num(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        double result = Maths.std(num);
        assertEquals(Math.sqrt(2.0), result, 1e-5);
    }

    @Test
    /**
     * testVar方法。
     */
    public void testVar() {
        Num num = new Num(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        double result = Maths.var(num);
        assertEquals(2.0, result, 1e-10);
    }
}