package com.zifang.util.media.graph.qrcode.encoder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BitMatrixTest类。
 */
public class BitMatrixTest {

    @Test
    /**
     * testConstructor方法。
     */
    public void testConstructor() {
        BitMatrix matrix = new BitMatrix(10, 15);
        assertEquals(10, matrix.getWidth());
        assertEquals(15, matrix.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructorWithZeroWidth方法。
     */
    public void testConstructorWithZeroWidth() {
        new BitMatrix(0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructorWithZeroHeight方法。
     */
    public void testConstructorWithZeroHeight() {
        new BitMatrix(10, 0);
    }

    @Test
    /**
     * testSetAndGet方法。
     */
    public void testSetAndGet() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(3, 5);
        assertTrue(matrix.get(3, 5));
    }

    @Test
    /**
     * testUnset方法。
     */
    public void testUnset() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(3, 5);
        matrix.unset(3, 5);
        assertFalse(matrix.get(3, 5));
    }

    @Test
    /**
     * testSetWithValue方法。
     */
    public void testSetWithValue() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(3, 5, true);
        matrix.set(4, 6, false);
        assertTrue(matrix.get(3, 5));
        assertFalse(matrix.get(4, 6));
    }

    @Test
    /**
     * testFlip方法。
     */
    public void testFlip() {
        BitMatrix matrix = new BitMatrix(10, 10);
        assertFalse(matrix.get(3, 5));
        matrix.flip(3, 5);
        assertTrue(matrix.get(3, 5));
        matrix.flip(3, 5);
        assertFalse(matrix.get(3, 5));
    }

    @Test
    /**
     * testClear方法。
     */
    public void testClear() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(3, 5);
        matrix.set(7, 8);
        matrix.clear();
        assertFalse(matrix.get(3, 5));
        assertFalse(matrix.get(7, 8));
    }

    @Test
    /**
     * testGetOutOfBounds方法。
     */
    public void testGetOutOfBounds() {
        BitMatrix matrix = new BitMatrix(10, 10);
        // Should return false for out of bounds, not throw
        assertFalse(matrix.get(-1, 5));
        assertFalse(matrix.get(5, -1));
        assertFalse(matrix.get(10, 5));
        assertFalse(matrix.get(5, 10));
    }

    @Test
    /**
     * testSetOutOfBounds方法。
     */
    public void testSetOutOfBounds() {
        BitMatrix matrix = new BitMatrix(10, 10);
        // Should not throw for out of bounds
        matrix.set(-1, 5);
        matrix.set(5, -1);
        matrix.set(10, 5);
        matrix.set(5, 10);
    }

    @Test
    /**
     * testGetTopLeftOnBit方法。
     */
    public void testGetTopLeftOnBit() {
        BitMatrix matrix = new BitMatrix(10, 10);
        assertNull(matrix.getTopLeftOnBit());
        // set bits at (7,2) and (3,5) - top-left should be the one with smaller y
        matrix.set(7, 2);
        matrix.set(3, 5);
        int[] result = matrix.getTopLeftOnBit();
        assertNotNull(result);
        // top-left = smaller y (y=2 < y=5)
        assertEquals(7, result[0]);
        assertEquals(2, result[1]);
    }

    @Test
    /**
     * testGetBottomRightOnBit方法。
     */
    public void testGetBottomRightOnBit() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(7, 2);
        matrix.set(3, 5);
        int[] result = matrix.getBottomRightOnBit();
        assertNotNull(result);
        // bottom-right = larger y (y=5 > y=2)
        assertEquals(3, result[0]);
        assertEquals(5, result[1]);
    }

    @Test
    /**
     * testClone方法。
     */
    public void testClone() {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(3, 5);
        matrix.set(7, 8);
        BitMatrix cloned = matrix.clone();
        assertEquals(matrix.getWidth(), cloned.getWidth());
        assertEquals(matrix.getHeight(), cloned.getHeight());
        assertTrue(cloned.get(3, 5));
        assertTrue(cloned.get(7, 8));
        // Ensure it's a different object
        assertNotSame(matrix, cloned);
    }
}
