package com.zifang.util.pandas.num;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Nums 工具类测试
 */

/**
 * NumsTest类。
 */
public class NumsTest {

    @Test
    /**
     * testArrayCreation方法。
     */
    public void testArrayCreation() {
        // 测试基本数组创建
        int[] intArray = {1, 2, 3, 4, 5};
        Num num = Nums.array(intArray);
        assertNotNull(num);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testArrayFromList方法。
     */
    public void testArrayFromList() {
        // 测试从 List 创建数组
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Num num = Nums.array(list);
        assertNotNull(num);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testNDim方法。
     */
    public void testNDim() {
        // 测试维度计算
        int[] oneDim = {1, 2, 3};
        Num num1 = Nums.array(oneDim);
        assertEquals(1, num1.nDim());

        // 二维数组
        int[][] twoDim = {{1, 2}, {3, 4}};
        Num num2 = Nums.array(twoDim);
        assertEquals(2, num2.nDim());
    }

    @Test
    /**
     * testShape方法。
     */
    public void testShape() {
        // 测试形状计算
        int[][] twoDim = {{1, 2, 3}, {4, 5, 6}};
        Num num = Nums.array(twoDim);
        int[] shape = num.shape();
        assertEquals(2, shape.length);
        assertEquals(2, shape[0]);
        assertEquals(3, shape[1]);
    }

    @Test
    /**
     * testSize方法。
     */
    public void testSize() {
        // 测试元素总数计算
        int[][] twoDim = {{1, 2, 3}, {4, 5, 6}};
        Num num = Nums.array(twoDim);
        assertEquals(6, num.size());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        // 测试字符串表示
        int[] arr = {1, 2, 3};
        Num num = Nums.array(arr);
        String str = num.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }

    @Test(expected = RuntimeException.class)
    /**
     * testArrayWithNonArray方法。
     */
    public void testArrayWithNonArray() {
        // 测试传入非数组时抛出异常
        Nums.array("not an array");
    }

    @Test
    /**
     * testRandomAccess方法。
     */
    public void testRandomAccess() {
        // 测试随机数生成器可访问
        assertNotNull(Nums.random);
    }

    // ==================== 工厂方法（原先是 return null 的空壳） ====================

    @Test
    public void testArrayWithShape() {
        Num num = Nums.array(new int[]{2, 3}, DType.FLOAT64);
        assertArrayEquals(new int[]{2, 3}, num.shape());
        assertEquals(6, num.size());
        assertEquals(0.0, num.sum(), 0.0);
    }

    @Test
    public void testArrayWithShapeAndValues() {
        Num num = Nums.array(new int[]{2, 2}, new Object[]{1, 2, 3, 4}, DType.FLOAT64);
        assertArrayEquals(new int[]{2, 2}, num.shape());
        assertEquals(10.0, num.sum(), 0.0);
        assertEquals(4.0, num.max(), 0.0);
    }

    @Test
    public void testArrayRejectsWrongValueCount() {
        try {
            Nums.array(new int[]{2, 2}, new Object[]{1, 2, 3}, DType.FLOAT64);
            fail("元素个数不足，应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("不匹配"));
        }
    }

    @Test
    public void testFill() {
        Num num = Nums.array(new int[]{2, 2}, DType.FLOAT64);
        Nums.fill(num, new Object[]{5, 6, 7, 8});
        assertEquals(26.0, num.sum(), 0.0);
        assertEquals(8.0, num.max(), 0.0);
    }

    @Test
    public void testFillRejectsWrongValueCount() {
        Num num = Nums.array(new int[]{3}, DType.FLOAT64);
        try {
            Nums.fill(num, new Object[]{1, 2});
            fail("元素个数不足，应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("不匹配"));
        }
    }

    @Test
    public void testARange() {
        assertEquals(45.0, Nums.aRange(10).sum(), 0.0);   // 0..9
        assertEquals(10, Nums.aRange(10).size());
        assertEquals(95.0, Nums.aRange(5, 15).sum(), 0.0);   // 5..14
        assertEquals(4, Nums.aRange(5, 12, 2).size());                    // 5,7,9,11
        assertEquals(11.0, Nums.aRange(5, 12, 2).max(), 0.0);
        assertEquals(5.0, Nums.aRange(5, 12, 2).min(), 0.0);
    }

    @Test
    public void testARangeRejectsZeroStep() {
        try {
            Nums.aRange(0, 10, 0);
            fail("步长为 0 应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("步长"));
        }
    }

    @Test
    public void testLinSpace() {
        Num closed = Nums.linSpace(10, 20, 11, true);
        assertEquals(11, closed.size());
        assertEquals(10.0, closed.min(), 1e-9);
        assertEquals(20.0, closed.max(), 1e-9);

        Num open = Nums.linSpace(0, 10, 5, false);
        assertEquals(5, open.size());
        assertEquals(0.0, open.min(), 1e-9);
        assertEquals(8.0, open.max(), 1e-9);   // 不含终点
    }

    @Test
    public void testZerosOnesEye() {
        assertArrayEquals(new int[]{2, 2}, Nums.zeros(new Integer[]{2, 2}, DType.FLOAT64).shape());
        assertEquals(0.0, Nums.zeros(new Integer[]{2, 2}, null).sum(), 0.0);

        assertEquals(9.0, Nums.ones(3, 3).sum(), 0.0);
        assertArrayEquals(new int[]{3, 3}, Nums.ones(3, 3).shape());

        Num eye = Nums.eye(4);
        assertArrayEquals(new int[]{4, 4}, eye.shape());
        assertEquals(4.0, eye.sum(), 0.0);     // 只有主对角线是 1
    }

    // ==================== 栈与分割 ====================

    @Test
    public void testHStack1D() {
        Num result = Nums.hStack(Nums.array(new double[]{1, 2}), Nums.array(new double[]{3, 4, 5}));
        assertEquals(1, result.nDim());
        assertEquals(5, result.size());
        assertEquals(15.0, result.sum(), 0.0);
    }

    @Test
    public void testHStack2D() {
        Num left = Nums.array(new double[][]{{1, 2}, {3, 4}});
        Num right = Nums.array(new double[][]{{5}, {6}});
        Num result = Nums.hStack(left, right);
        assertArrayEquals(new int[]{2, 3}, result.shape());
        assertEquals(21.0, result.sum(), 0.0);
    }

    @Test
    public void testHStackRejectsRowMismatch() {
        Num a = Nums.array(new double[][]{{1, 2}});
        Num b = Nums.array(new double[][]{{3, 4}, {5, 6}});
        try {
            Nums.hStack(a, b);
            fail("行数不一致应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("行数"));
        }
    }

    @Test
    public void testVStack() {
        Num result = Nums.vStack(Nums.array(new double[]{1, 2}), Nums.array(new double[]{3, 4}));
        assertArrayEquals(new int[]{2, 2}, result.shape());
        assertEquals(10.0, result.sum(), 0.0);
    }

    @Test
    public void testVStackRejectsColumnMismatch() {
        Num a = Nums.array(new double[]{1, 2});
        Num b = Nums.array(new double[]{3, 4, 5});
        try {
            Nums.vStack(a, b);
            fail("列数不一致应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("列数"));
        }
    }

    @Test
    public void testHSplit() {
        Num[] parts = Nums.hsplit(Nums.array(new double[][]{{1, 2, 3, 4}}), 2);
        assertEquals(2, parts.length);
        assertArrayEquals(new int[]{1, 2}, parts[0].shape());
        assertEquals(3.0, parts[0].sum(), 0.0);   // 1,2
        assertEquals(7.0, parts[1].sum(), 0.0);   // 3,4
    }

    @Test
    public void testHSplitRejectsIndivisibleSections() {
        try {
            Nums.hsplit(Nums.array(new double[][]{{1, 2, 3}}), 2);
            fail("列数不能整除份数，应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("整除"));
        }
    }

    @Test
    public void testVSplit() {
        Num[] parts = Nums.vsplit(Nums.array(new double[][]{{1}, {2}, {3}, {4}}), 2);
        assertEquals(2, parts.length);
        assertArrayEquals(new int[]{2, 1}, parts[0].shape());
        assertEquals(3.0, parts[0].sum(), 0.0);   // 1,2
        assertEquals(7.0, parts[1].sum(), 0.0);   // 3,4
    }

    @Test
    public void testVSplitRejectsNonPositiveSections() {
        try {
            Nums.vsplit(Nums.array(new double[][]{{1}, {2}}), 0);
            fail("份数为 0 应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("份数"));
        }
    }

    @Test
    public void testStackRejectsEmptyInput() {
        try {
            Nums.hStack();
            fail("空入参应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 消息全文是「hStack 至少需要一个数组」——
            // 「至少一个」不是它的连续子串（中间隔着「需要」），所以查「至少需要」。
            assertTrue("实际消息: [" + e.getMessage() + "]", e.getMessage().contains("至少需要"));
        }
    }
}
