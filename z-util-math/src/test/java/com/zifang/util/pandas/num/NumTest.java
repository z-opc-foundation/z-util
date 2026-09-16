package com.zifang.util.pandas.num;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Num 类测试
 */

/**
 * NumTest类。
 */
public class NumTest {

    @Test
    /**
     * testCreationWithIntArray方法。
     */
    public void testCreationWithIntArray() {
        int[] intArray = {1, 2, 3, 4, 5};
        Num num = new Num(intArray);
        assertNotNull(num);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testCreationWithDoubleArray方法。
     */
    public void testCreationWithDoubleArray() {
        double[] doubleArray = {1.0, 2.0, 3.0, 4.0, 5.0};
        Num num = new Num(doubleArray);
        assertNotNull(num);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testCreationWith2DArray方法。
     */
    public void testCreationWith2DArray() {
        int[][] twoDimArray = {{1, 2, 3}, {4, 5, 6}};
        Num num = new Num(twoDimArray);
        assertNotNull(num);
        assertEquals(2, num.nDim());
        assertEquals(6, num.size());
    }

    @Test
    /**
     * testCreationWithList方法。
     */
    public void testCreationWithList() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Num num = Nums.array(list);
        assertNotNull(num);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testNDim1DArray方法。
     */
    public void testNDim1DArray() {
        int[] array = {1, 2, 3};
        Num num = new Num(array);
        assertEquals(1, num.nDim());
    }

    @Test
    /**
     * testNDim2DArray方法。
     */
    public void testNDim2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        Num num = new Num(array);
        assertEquals(2, num.nDim());
    }

    @Test
    /**
     * testNDim3DArray方法。
     */
    public void testNDim3DArray() {
        int[][][] array = {{{1, 2}}, {{3, 4}}};
        Num num = new Num(array);
        assertEquals(3, num.nDim());
    }

    @Test
    /**
     * testShape1DArray方法。
     */
    public void testShape1DArray() {
        int[] array = {1, 2, 3, 4, 5};
        Num num = new Num(array);
        int[] shape = num.shape();
        assertEquals(1, shape.length);
        assertEquals(5, shape[0]);
    }

    @Test
    /**
     * testShape2DArray方法。
     */
    public void testShape2DArray() {
        int[][] array = {{1, 2, 3}, {4, 5, 6}};
        Num num = new Num(array);
        int[] shape = num.shape();
        assertEquals(2, shape.length);
        assertEquals(2, shape[0]);
        assertEquals(3, shape[1]);
    }

    @Test
    /**
     * testSize1DArray方法。
     */
    public void testSize1DArray() {
        int[] array = {1, 2, 3, 4, 5};
        Num num = new Num(array);
        assertEquals(5, num.size());
    }

    @Test
    /**
     * testSize2DArray方法。
     */
    public void testSize2DArray() {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Num num = new Num(array);
        assertEquals(9, num.size());
    }

    @Ignore
    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        int[] array = {1, 2, 3};
        Num num = new Num(array);
        String str = num.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }

    @Test
    /**
     * testCreationWithDifferentTypes方法。
     */
    public void testCreationWithDifferentTypes() {
        // 测试不同类型的数组
        byte[] byteArray = {1, 2, 3};
        short[] shortArray = {1, 2, 3};
        long[] longArray = {1L, 2L, 3L};
        float[] floatArray = {1.0f, 2.0f, 3.0f};

        assertEquals(3, new Num(byteArray).size());
        assertEquals(3, new Num(shortArray).size());
        assertEquals(3, new Num(longArray).size());
        assertEquals(3, new Num(floatArray).size());
    }

    @Test(expected = RuntimeException.class)
    /**
     * testCreationWithNonArray方法。
     */
    public void testCreationWithNonArray() {
        new Num("not an array");
    }

    @Test
    /**
     * testEmptyArray方法。
     */
    public void testEmptyArray() {
        int[] emptyArray = {};
        Num num = new Num(emptyArray);
        assertEquals(0, num.size());
        assertEquals(1, num.nDim());
    }

    @Ignore
    @Test
    /**
     * testJaggedArray方法。
     */
    public void testJaggedArray() {
        // 不规则数组
        int[][] jaggedArray = {{1, 2, 3}, {4, 5}, {6}};
        Num num = new Num(jaggedArray);
        assertEquals(2, num.nDim());
        assertEquals(6, num.size());
    }
}
