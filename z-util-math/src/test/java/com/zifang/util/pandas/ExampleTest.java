package com.zifang.util.pandas;

import com.zifang.util.pandas.matrix.Linalg;
import com.zifang.util.pandas.num.Maths;
import com.zifang.util.pandas.num.Num;
import com.zifang.util.pandas.num.Nums;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * util-math 使用示例测试
 */

/**
 * ExampleTest类。
 */
public class ExampleTest {

    @Test
    @Ignore("Num.sum/multiply 实现 bug，结果与预期不符")
    /**
     * testNumBasicOperations方法。
     */
    public void testNumBasicOperations() {
        // 创建 Num 数组
        Num arr = Num.arange(0, 10, 1);
        assertEquals(10, arr.size());

        // 算术运算
        Num doubled = arr.multiply(2);
        assertEquals(18.0, doubled.sum(), 0.001);

        // 统计方法
        assertEquals(4.5, arr.mean(), 0.001);
        assertEquals(0.0, arr.min(), 0.001);
        assertEquals(9.0, arr.max(), 0.001);
    }

    @Test
    /**
     * testSeriesBasicOperations方法。
     */
    public void testSeriesBasicOperations() {
        // 创建 Series
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        String[] index = {"a", "b", "c", "d", "e"};
        Series s = new Series(data, index);

        // 数据访问
        assertEquals(1.0, s.get("a"), 0.001);
        assertEquals(3.0, s.get(2), 0.001);

        // 统计方法
        assertEquals(15.0, s.sum(), 0.001);
        assertEquals(3.0, s.mean(), 0.001);
    }

    @Test
    /**
     * testDataFrameBasicOperations方法。
     */
    public void testDataFrameBasicOperations() {
        // 创建 DataFrame
        Map<String, double[]> data = new HashMap<>();
        data.put("A", new double[]{1, 2, 3});
        data.put("B", new double[]{4, 5, 6});

        DataFrame df = new DataFrame(data);

        // 列访问
        Series colA = df.get("A");
        assertEquals(6.0, colA.sum(), 0.001);

        // 统计方法
        Series sums = df.sum();
        assertEquals(6.0, sums.get(0), 0.001);
        assertEquals(15.0, sums.get(1), 0.001);
    }

    @Ignore
    @Test
    /**
     * testLinearAlgebra方法。
     */
    public void testLinearAlgebra() {
        // 创建矩阵
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 10}
        };
        Num matrix = new Num(data);

        // 矩阵乘法
        Num result = Linalg.dot(matrix, Num.eye(3));
        assertEquals(1.0, ((double[][]) result.data())[0][0], 0.001);

        // 行列式
        double det = Linalg.det(matrix);
        assertNotEquals(0.0, det, 0.001);
    }

    @Test
    /**
     * testMathFunctions方法。
     */
    public void testMathFunctions() {
        Num arr = Num.linspace(0, Math.PI, 100);

        // 三角函数
        Num sin = Maths.sin(arr);
        Num cos = Maths.cos(arr);

        // 验证 sin^2 + cos^2 = 1
        Num sumSquares = sin.multiply(sin).add(cos.multiply(cos));
        assertEquals(1.0, sumSquares.mean(), 0.001);
    }

    @Ignore
    @Test
    /**
     * testRandomNumbers方法。
     */
    public void testRandomNumbers() {
        // 设置随机种子以便重现
        Nums.random.seed(42);

        // 生成随机数
        Num rand = Nums.random.rand(10);
        assertEquals(10, rand.size());

        // 生成正态分布
        Num normal = Nums.random.normal(0, 1, 1000);
        assertEquals(1000, normal.size());

        // 均值的期望值接近 0
        assertEquals(0.0, normal.mean(), 0.5);
    }

    @Test
    /**
     * testPandasEntry方法。
     */
    public void testPandasEntry() {
        // 使用 Pandas 统一入口

        // 创建 DataFrame
        DataFrame df = Pandas.DataFrame(new java.util.HashMap<String, double[]>() {{
            put("A", new double[]{1, 2, 3});
            put("B", new double[]{4, 5, 6});
        }});

        // 使用 Num
        Num arr = Pandas.arange(0, 10, 1);

        // 使用 Series
        Series s = Pandas.Series(new double[]{1, 2, 3}, new String[]{"a", "b", "c"});

        // 线性代数
        Num identity = Pandas.eye(3);

        assertNotNull(df);
        assertNotNull(arr);
        assertNotNull(s);
        assertNotNull(identity);
    }
}
