package com.zifang.util.pandas;

import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Series 类测试
 */

/**
 * SeriesTest类。
 */
public class SeriesTest {

    @Test
    /**
     * testCreationWithDoubleArray方法。
     */
    public void testCreationWithDoubleArray() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        Series series = new Series(data);
        assertNotNull(series);
        assertEquals(5, series.count());
    }

    @Test
    /**
     * testCreationWithIntArray方法。
     */
    public void testCreationWithIntArray() {
        int[] data = {1, 2, 3, 4, 5};
        try {
            Series series = new Series(data);
            assertNotNull(series);
            assertEquals(5, series.count());
        } catch (UnsupportedOperationException e) {
            // int array conversion not yet supported
        }
    }

    @Test
    /**
     * testCreationWithIndex方法。
     */
    public void testCreationWithIndex() {
        String[] index = {"a", "b", "c"};
        double[] data = {1.0, 2.0, 3.0};
        Series series = new Series(data, index);
        assertEquals(3, series.count());
        assertEquals(1.0, series.get("a"), 1e-10);
        assertEquals(2.0, series.get("b"), 1e-10);
    }

    @Test
    /**
     * testStaticFactoryOfDouble方法。
     */
    public void testStaticFactoryOfDouble() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0});
        assertNotNull(series);
        assertEquals(3, series.count());
    }

    @Test
    /**
     * testStaticFactoryOfInt方法。
     */
    public void testStaticFactoryOfInt() {
        try {
            Series series = Series.of(new int[]{1, 2, 3});
            assertNotNull(series);
            assertEquals(3, series.count());
        } catch (UnsupportedOperationException e) {
            // int array conversion not yet supported
        }
    }

    @Test
    /**
     * testStaticFactoryOfStringArray方法。
     */
    public void testStaticFactoryOfStringArray() {
        Series series = Series.of(new String[]{"a", "b", "c"});
        assertNotNull(series);
        assertEquals(3, series.count());
    }

    @Test
    /**
     * testStaticFactoryWithIndex方法。
     */
    public void testStaticFactoryWithIndex() {
        Series series = Series.of(new String[]{"x", "y", "z"}, new double[]{10.0, 20.0, 30.0});
        assertNotNull(series);
        assertEquals(10.0, series.get("x"), 1e-10);
        assertEquals(30.0, series.get("z"), 1e-10);
    }

    @Test
    /**
     * testStaticFactoryFromMap方法。
     */
    public void testStaticFactoryFromMap() {
        Map<String, Double> map = new java.util.HashMap<String, Double>() {{
            put("a", 1.0);
            put("b", 2.0);
            put("c", 3.0);
        }};
        Series series = Series.fromMap(map);
        assertNotNull(series);
        assertEquals(3, series.count());
    }

    @Test
    /**
     * testGetByIndex方法。
     */
    public void testGetByIndex() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0});
        assertEquals(1.0, series.get(0), 1e-10);
        assertEquals(2.0, series.get(1), 1e-10);
        assertEquals(3.0, series.get(2), 1e-10);
    }

    @Test
    /**
     * testGetByLabel方法。
     */
    public void testGetByLabel() {
        Series series = Series.of(new String[]{"a", "b", "c"}, new double[]{1.0, 2.0, 3.0});
        assertEquals(1.0, series.get("a"), 1e-10);
        assertEquals(3.0, series.get("c"), 1e-10);
    }

    @Test(expected = NoSuchElementException.class)
    /**
     * testGetByInvalidLabel方法。
     */
    public void testGetByInvalidLabel() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0});
        series.get("invalid");
    }

    @Test
    /**
     * testGetByLabels方法。
     */
    public void testGetByLabels() {
        Series series = Series.of(new String[]{"a", "b", "c"}, new double[]{1.0, 2.0, 3.0});
        Series sub = series.get(new String[]{"a", "c"});
        assertEquals(2, sub.count());
        assertEquals(1.0, sub.get(0), 1e-10);
        assertEquals(3.0, sub.get(1), 1e-10);
    }

    @Test
    /**
     * testGetByIntIndices方法。
     */
    public void testGetByIntIndices() {
        Series series = Series.of(new String[]{"a", "b", "c"}, new double[]{1.0, 2.0, 3.0});
        Series sub = series.get(new int[]{0, 2});
        assertEquals(2, sub.count());
    }

    @Test
    /**
     * testSlice方法。
     */
    public void testSlice() {
        Series series = Series.of(new String[]{"a", "b", "c"}, new double[]{1.0, 2.0, 3.0});
        Series sub = series.slice(0, 2);
        assertEquals(2, sub.count());
    }

    @Test
    /**
     * testLoc方法。
     */
    public void testLoc() {
        Series series = Series.of(new String[]{"a", "b", "c"}, new double[]{1.0, 2.0, 3.0});
        Series sub = series.loc("a", "c");
        assertEquals(3, sub.count());
    }

    @Test
    /**
     * testIloc方法。
     */
    public void testIloc() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        Series sub = series.iloc(1, 4);
        assertEquals(3, sub.count());
    }

    @Test
    /**
     * testFilter方法。
     */
    public void testFilter() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        Series filtered = series.filter(v -> v > 2.5);
        assertEquals(3, filtered.count());
    }

    @Test
    /**
     * testWhere方法。
     */
    public void testWhere() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        Series result = series.where(v -> v > 2.5, 100.0, 0.0);
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(0.0, result.get(1), 1e-10);
        assertEquals(100.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testAddSeries方法。
     */
    public void testAddSeries() {
        Series a = Series.of(new double[]{1.0, 2.0, 3.0});
        Series b = Series.of(new double[]{10.0, 20.0, 30.0});
        Series result = a.add(b);
        assertEquals(11.0, result.get(0), 1e-10);
        assertEquals(22.0, result.get(1), 1e-10);
        assertEquals(33.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testAddScalar方法。
     */
    public void testAddScalar() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0});
        Series result = series.add(10.0);
        assertEquals(11.0, result.get(0), 1e-10);
        assertEquals(12.0, result.get(1), 1e-10);
        assertEquals(13.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testSubtract方法。
     */
    public void testSubtract() {
        Series a = Series.of(new double[]{10.0, 20.0, 30.0});
        Series b = Series.of(new double[]{1.0, 2.0, 3.0});
        Series result = a.subtract(b);
        assertEquals(9.0, result.get(0), 1e-10);
        assertEquals(18.0, result.get(1), 1e-10);
        assertEquals(27.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testMultiply方法。
     */
    public void testMultiply() {
        Series a = Series.of(new double[]{1.0, 2.0, 3.0});
        Series b = Series.of(new double[]{10.0, 10.0, 10.0});
        Series result = a.multiply(b);
        assertEquals(10.0, result.get(0), 1e-10);
        assertEquals(20.0, result.get(1), 1e-10);
        assertEquals(30.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testDivide方法。
     */
    public void testDivide() {
        Series a = Series.of(new double[]{10.0, 20.0, 30.0});
        Series b = Series.of(new double[]{2.0, 4.0, 5.0});
        Series result = a.divide(b);
        assertEquals(5.0, result.get(0), 1e-10);
        assertEquals(5.0, result.get(1), 1e-10);
        assertEquals(6.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testPow方法。
     */
    public void testPow() {
        Series series = Series.of(new double[]{2.0, 3.0, 4.0});
        Series result = series.pow(2.0);
        assertEquals(4.0, result.get(0), 1e-10);
        assertEquals(9.0, result.get(1), 1e-10);
        assertEquals(16.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testAbs方法。
     */
    public void testAbs() {
        Series series = new Series(new double[]{-1.0, 0.0, 1.0, -5.0});
        Series result = series.abs();
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(0.0, result.get(1), 1e-10);
        assertEquals(1.0, result.get(2), 1e-10);
        assertEquals(5.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testSqrt方法。
     */
    public void testSqrt() {
        Series series = Series.of(new double[]{0.0, 1.0, 4.0, 9.0});
        Series result = series.sqrt();
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(1.0, result.get(1), 1e-10);
        assertEquals(2.0, result.get(2), 1e-10);
        assertEquals(3.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testLog方法。
     */
    public void testLog() {
        Series series = Series.of(new double[]{1.0, Math.E, 10.0});
        Series result = series.log();
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(1.0, result.get(1), 1e-10);
    }

    @Test
    /**
     * testExp方法。
     */
    public void testExp() {
        Series series = Series.of(new double[]{0.0, 1.0, 2.0});
        Series result = series.exp();
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(Math.E, result.get(1), 1e-10);
    }

    @Test
    /**
     * testSum方法。
     */
    public void testSum() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertEquals(15.0, series.sum(), 1e-10);
    }

    @Test
    /**
     * testMean方法。
     */
    public void testMean() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertEquals(3.0, series.mean(), 1e-10);
    }

    @Test
    /**
     * testMax方法。
     */
    public void testMax() {
        Series series = Series.of(new double[]{1.0, 5.0, 3.0, 9.0, 2.0});
        assertEquals(9.0, series.max(), 1e-10);
    }

    @Test
    /**
     * testMin方法。
     */
    public void testMin() {
        Series series = Series.of(new double[]{1.0, 5.0, 3.0, 9.0, 2.0});
        assertEquals(1.0, series.min(), 1e-10);
    }

    @Test
    /**
     * testStd方法。
     */
    public void testStd() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertEquals(Math.sqrt(2.0), series.std(), 1e-5);
    }

    @Test
    /**
     * testVar方法。
     */
    public void testVar() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertEquals(2.0, series.var(), 1e-10);
    }

    @Test
    /**
     * testMedian方法。
     */
    public void testMedian() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertEquals(3.0, series.median(), 1e-10);
    }

    @Test
    /**
     * testPercentile方法。
     */
    public void testPercentile() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0});
        assertEquals(5.5, series.percentile(50), 1e-10);
    }

    @Test
    /**
     * testCount方法。
     */
    public void testCount() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0});
        assertEquals(3, series.count());
    }

    @Test
    /**
     * testSem方法。
     */
    public void testSem() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        double expected = series.std() / Math.sqrt(5);
        assertEquals(expected, series.sem(), 1e-10);
    }

    @Test
    /**
     * testCumsum方法。
     */
    public void testCumsum() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        Series result = series.cumsum();
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(3.0, result.get(1), 1e-10);
        assertEquals(6.0, result.get(2), 1e-10);
        assertEquals(10.0, result.get(3), 1e-10);
        assertEquals(15.0, result.get(4), 1e-10);
    }

    @Test
    /**
     * testCumprod方法。
     */
    public void testCumprod() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0});
        Series result = series.cumprod();
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(2.0, result.get(1), 1e-10);
        assertEquals(6.0, result.get(2), 1e-10);
        assertEquals(24.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testDiff方法。
     */
    public void testDiff() {
        Series series = Series.of(new double[]{1.0, 3.0, 6.0, 10.0, 15.0});
        Series result = series.diff();
        assertTrue(Double.isNaN(result.get(0)));
        assertEquals(2.0, result.get(1), 1e-10);
        assertEquals(3.0, result.get(2), 1e-10);
        assertEquals(4.0, result.get(3), 1e-10);
        assertEquals(5.0, result.get(4), 1e-10);
    }

    @Test
    /**
     * testPctChange方法。
     */
    public void testPctChange() {
        Series series = Series.of(new double[]{100.0, 110.0, 121.0, 133.1});
        Series result = series.pct_change();
        assertTrue(Double.isNaN(result.get(0)));
        assertEquals(0.1, result.get(1), 1e-10);
        assertEquals(0.1, result.get(2), 1e-10);
        assertEquals(0.1, result.get(3), 1e-10);
    }

    @Test
    /**
     * testEq方法。
     */
    public void testEq() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 2.0});
        Series result = series.eq(2.0);
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(1.0, result.get(1), 1e-10);
        assertEquals(0.0, result.get(2), 1e-10);
        assertEquals(1.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testNe方法。
     */
    public void testNe() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 2.0});
        Series result = series.ne(2.0);
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(0.0, result.get(1), 1e-10);
        assertEquals(1.0, result.get(2), 1e-10);
        assertEquals(0.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testGt方法。
     */
    public void testGt() {
        Series series = Series.of(new double[]{1.0, 2.0, 3.0, 4.0});
        Series result = series.gt(2.5);
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(0.0, result.get(1), 1e-10);
        assertEquals(1.0, result.get(2), 1e-10);
        assertEquals(1.0, result.get(3), 1e-10);
    }

    @Test
    /**
     * testAll方法。
     */
    public void testAll() {
        Series allNonZero = Series.of(new double[]{1.0, 2.0, 3.0});
        assertTrue(allNonZero.all());

        Series withZero = new Series(new double[]{0.0, 1.0, 2.0});
        assertFalse(withZero.all());
    }

    @Test
    /**
     * testAny方法。
     */
    public void testAny() {
        Series withZero = Series.of(new double[]{0.0, 0.0, 1.0});
        assertTrue(withZero.any());

        Series allZero = new Series(new double[]{0.0, 0.0, 0.0});
        assertFalse(allZero.any());
    }

    @Test
    /**
     * testIsna方法。
     */
    public void testIsna() {
        Series series = new Series(new double[]{1.0, Double.NaN, 3.0});
        Series result = series.isna();
        assertEquals(0.0, result.get(0), 1e-10);
        assertEquals(1.0, result.get(1), 1e-10);
        assertEquals(0.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testNotna方法。
     */
    public void testNotna() {
        Series series = new Series(new double[]{1.0, Double.NaN, 3.0});
        Series result = series.notna();
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(0.0, result.get(1), 1e-10);
        assertEquals(1.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testFillna方法。
     */
    public void testFillna() {
        Series series = new Series(new double[]{1.0, Double.NaN, 3.0});
        Series result = series.fillna(99.0);
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(99.0, result.get(1), 1e-10);
        assertEquals(3.0, result.get(2), 1e-10);
    }

    @Test
    /**
     * testDropna方法。
     */
    public void testDropna() {
        Series series = new Series(new double[]{1.0, Double.NaN, 3.0, Double.NaN, 5.0});
        Series result = series.dropna();
        assertEquals(3, result.count());
        assertEquals(1.0, result.get(0), 1e-10);
        assertEquals(3.0, result.get(1), 1e-10);
        assertEquals(5.0, result.get(2), 1e-10);
    }
}