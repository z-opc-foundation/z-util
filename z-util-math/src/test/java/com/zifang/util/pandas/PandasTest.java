package com.zifang.util.pandas;

import com.zifang.util.pandas.matrix.Linalg;
import com.zifang.util.pandas.num.Num;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Pandas 工具类测试
 */

/**
 * PandasTest类。
 */
public class PandasTest {

    @Test
    /**
     * testArrayCreation方法。
     */
    public void testArrayCreation() {
        // 测试 array 创建
        double[] data = {1.0, 2.0, 3.0};
        Num arr = Pandas.array(data);
        assertNotNull(arr);
        assertEquals(3, arr.size());
    }

    @Test
    /**
     * testZeros方法。
     */
    public void testZeros() {
        Num zeros = Pandas.zeros(3, 4);
        assertNotNull(zeros);
        assertEquals(12, zeros.size());
        assertArrayEquals(new int[]{3, 4}, zeros.shape());

        // 验证所有元素为 0
        double sum = zeros.sum();
        assertEquals(0.0, sum, 1e-10);
    }

    @Test
    /**
     * testOnes方法。
     */
    public void testOnes() {
        Num ones = Pandas.ones(2, 2);
        assertNotNull(ones);
        assertEquals(4, ones.size());

        // 验证所有元素为 1
        double sum = ones.sum();
        assertEquals(4.0, sum, 1e-10);
    }

    @Test
    /**
     * testEye方法。
     */
    public void testEye() {
        Num eye = Pandas.eye(3);
        assertNotNull(eye);
        assertEquals(9, eye.size());
        assertArrayEquals(new int[]{3, 3}, eye.shape());

        // 验证对角线为 1
        double trace = Linalg.trace(eye);
        assertEquals(3.0, trace, 1e-10);
    }

    @Test
    /**
     * testArange方法。
     */
    public void testArange() {
        Num arr1 = Pandas.arange(10);
        assertNotNull(arr1);
        assertEquals(10, arr1.size());

        Num arr2 = Pandas.arange(0, 10, 2);
        assertNotNull(arr2);
        assertEquals(5, arr2.size()); // 0, 2, 4, 6, 8
    }

    @Test
    /**
     * testLinspace方法。
     */
    public void testLinspace() {
        Num lin = Pandas.linspace(0, 1, 5);
        assertNotNull(lin);
        assertEquals(5, lin.size());

        // 验证包含端点
        double[] data = (double[]) lin.data();
        assertEquals(0.0, data[0], 1e-10);
        assertEquals(1.0, data[4], 1e-10);
    }

    @Test
    /**
     * testMathFunctions方法。
     */
    public void testMathFunctions() {
        Num arr = Pandas.array(new double[]{0, Math.PI / 2, Math.PI});

        // sin
        Num sin = Pandas.sin(arr);
        assertNotNull(sin);

        // cos
        Num cos = Pandas.cos(arr);
        assertNotNull(cos);

        // exp
        Num exp = Pandas.exp(arr);
        assertNotNull(exp);

        // log
        Num arrPositive = Pandas.array(new double[]{1, 2, 3});
        Num log = Pandas.log(arrPositive);
        assertNotNull(log);

        // sqrt
        Num sqrt = Pandas.sqrt(arrPositive);
        assertNotNull(sqrt);

        // abs
        Num arrNegative = Pandas.array(new double[]{-1, -2, -3});
        Num abs = Pandas.abs(arrNegative);
        double[] absData = (double[]) abs.data();
        assertEquals(1.0, absData[0], 1e-10);
        assertEquals(2.0, absData[1], 1e-10);
        assertEquals(3.0, absData[2], 1e-10);
    }

    @Test
    /**
     * testLinearAlgebra方法。
     */
    public void testLinearAlgebra() {
        Num a = Pandas.ones(2, 2);
        Num b = Pandas.eye(2);

        // dot
        Num c = Pandas.dot(a, b);
        assertNotNull(c);

        // matmul (同 dot)
        Num d = Pandas.matmul(a, b);
        assertNotNull(d);

        // trace
        double trace = Pandas.trace(b);
        assertEquals(2.0, trace, 1e-10);

        // 创建可逆矩阵
        Num invMatrix = Pandas.array(new double[][]{{4, 7}, {2, 6}});

        // det
        double det = Pandas.det(invMatrix);
        assertEquals(10.0, det, 1e-10);
    }

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals(Math.PI, Pandas.PI, 1e-10);
        assertEquals(Math.E, Pandas.E, 1e-10);
        assertTrue(Double.isInfinite(Pandas.INF));
        assertTrue(Double.isInfinite(Pandas.NINF));
        assertTrue(Double.isNaN(Pandas.NAN));
    }

    @Test
    /**
     * testDataFrameCreation方法。
     */
    public void testDataFrameCreation() {
        Map<String, double[]> data = new java.util.LinkedHashMap<>();
        data.put("A", new double[]{1, 2, 3});
        data.put("B", new double[]{4, 5, 6});

        DataFrame df = Pandas.DataFrame(data);
        assertNotNull(df);
        assertEquals(3, df.nRows());
        assertEquals(2, df.nCols());
    }

    @Test
    /**
     * testSeriesCreation方法。
     */
    public void testSeriesCreation() {
        double[] data = {1, 2, 3, 4, 5};
        Series s = Pandas.Series(data);
        assertNotNull(s);
        assertEquals(5, s.count());
    }

    @Test
    /**
     * testRandom方法。
     */
    public void testRandom() {
        // rand
        Num rand = Pandas.rand(3, 4);
        assertNotNull(rand);
        assertEquals(12, rand.size());

        // randn
        Num randn = Pandas.randn(5);
        assertNotNull(randn);
        assertEquals(5, randn.size());

        // randint
        Num randint = Pandas.randint(0, 10, 3, 3);
        assertNotNull(randint);
        assertEquals(9, randint.size());

        // seed
        Pandas.seed(42);
    }

    @Ignore
    @Test
    /**
     * testCSVReadWrite方法。
     */
    public void testCSVReadWrite() throws IOException {
        // 创建测试数据
        Map<String, double[]> data = new java.util.LinkedHashMap<>();
        data.put("A", new double[]{1, 2, 3});
        data.put("B", new double[]{4, 5, 6});
        DataFrame df = new DataFrame(data);

        // 写入临时文件
        String tempFile = System.getProperty("java.io.tmpdir") + "/test_" + System.currentTimeMillis() + ".csv";
        Pandas.to_csv(df, tempFile);

        // 读取文件
        DataFrame df2 = Pandas.read_csv(tempFile);

        // 验证
        assertNotNull(df2);
        assertEquals(df.nRows(), df2.nRows());
        assertEquals(df.nCols(), df2.nCols());

        // 删除临时文件
        new File(tempFile).delete();
    }

    @Test
    /**
     * testCSVString方法。
     */
    public void testCSVString() {
        // 创建测试数据
        Map<String, double[]> data = new java.util.LinkedHashMap<>();
        data.put("A", new double[]{1, 2, 3});
        data.put("B", new double[]{4, 5, 6});
        DataFrame df = new DataFrame(data);

        // 转换为 CSV 字符串
        String csv = Pandas.to_csv_string(df);

        // 验证
        assertNotNull(csv);
        assertTrue(csv.contains("A"));
        assertTrue(csv.contains("B"));
        assertTrue(csv.contains("1"));
        assertTrue(csv.contains("4"));
    }
}
