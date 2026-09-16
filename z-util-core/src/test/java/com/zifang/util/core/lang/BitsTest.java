package com.zifang.util.core.lang;

import com.zifang.util.core.lang.primitive.Bits;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BitsTest类。
 */
public class BitsTest {

    @Test
    /**
     * multipleLess方法。
     */
    public void multipleLess() {
        // multipleLess rounds down to nearest power of 2
        assertEquals(2, Bits.multipleLess(3));
        assertEquals(2, Bits.multipleLess(2));
        assertEquals(1, Bits.multipleLess(1));
        assertEquals(0, Bits.multipleLess(0));
    }

    @Test
    /**
     * multipleMore方法。
     */
    public void multipleMore() {
        // multipleMore: round up to next power of 2
        assertEquals(4, Bits.multipleMore(3));
        assertEquals(4, Bits.multipleMore(4));
        assertEquals(8, Bits.multipleMore(5));
        assertEquals(1, Bits.multipleMore(0));
        assertEquals(1, Bits.multipleMore(1));
    }

    @Test
    /**
     * avg方法。
     */
    public void avg() {
        assertEquals(7, Bits.avg(5, 9));
        assertEquals(7, Bits.avg(5, 10));
        assertEquals(3, Bits.avg(2, 4));
    }

    @Test
    /**
     * abs方法。
     */
    public void abs() {
        assertEquals(2, Bits.abs(2));
        assertEquals(2, Bits.abs(-2));
        assertEquals(Integer.MAX_VALUE, Bits.abs(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, Bits.abs(Integer.MIN_VALUE));
    }

    @Test
    /**
     * isOdd方法。
     */
    public void isOdd() {
        // isOdd: (abs(x) & 1) != 0
        assertTrue(Bits.isOdd(1));
        assertTrue(Bits.isOdd(3));
        assertFalse(Bits.isOdd(2));
        assertFalse(Bits.isOdd(0));
        assertTrue(Bits.isOdd(-1));
        assertFalse(Bits.isOdd(-2));
    }

    @Test
    /**
     * isEven方法。
     */
    public void isEven() {
        assertTrue(Bits.isEven(2));
        assertTrue(Bits.isEven(0));
        assertTrue(Bits.isEven(-2));
        assertFalse(Bits.isEven(3));
        assertFalse(Bits.isEven(1));
    }

    @Test
    /**
     * setTrueAndGetFlag方法。
     */
    public void setTrueAndGetFlag() {
        long flags = Bits.setTrue(0L, 0);
        assertTrue(Bits.getFlag(flags, 0));
        assertFalse(Bits.getFlag(flags, 1));

        flags = Bits.setTrue(flags, 5);
        assertTrue(Bits.getFlag(flags, 5));
        assertFalse(Bits.getFlag(flags, 1));
    }

    @Test
    /**
     * setFalseAndGetFlag方法。
     */
    public void setFalseAndGetFlag() {
        long allTrue = ~0L;
        long flags = Bits.setFalse(allTrue, 0);
        assertFalse(Bits.getFlag(flags, 0));
        assertTrue(Bits.getFlag(flags, 1));
    }

    @Test
    /**
     * isPowFrom2方法。
     */
    public void isPowFrom2() {
        assertTrue(Bits.isPowFrom2(1));
        assertTrue(Bits.isPowFrom2(2));
        assertTrue(Bits.isPowFrom2(8));
        assertTrue(Bits.isPowFrom2(1024));
        assertFalse(Bits.isPowFrom2(0));
        assertFalse(Bits.isPowFrom2(3));
        assertFalse(Bits.isPowFrom2(6));
    }

    @Test
    /**
     * getAllTrueIndex方法。
     */
    public void getAllTrueIndex() {
        long flags = Bits.setTrue(Bits.setTrue(0L, (byte) 1), (byte) 3);
        java.util.Set<Byte> trueIndex = Bits.getAllTrueIndex(flags);
        assertEquals(2, trueIndex.size());
        assertTrue(trueIndex.contains((byte) 1));
        assertTrue(trueIndex.contains((byte) 3));
    }

    @Test
    /**
     * getAllFalseIndex方法。
     */
    public void getAllFalseIndex() {
        long flags = Bits.setTrue(0L, (byte) 0);
        java.util.Set<Byte> falseIndex = Bits.getAllFalseIndex(flags);
        assertEquals(63, falseIndex.size());
        assertFalse(falseIndex.contains((byte) 0));
    }
}
