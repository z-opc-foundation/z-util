package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * BitsTest类。
 */
public class BitsTest {

    // --- binaryStr ---

    @Test(expected = NullPointerException.class)
    /**
     * binaryStr_withNull方法。
     */
    public void binaryStr_withNull() {
        Bits.binaryStr(null);
    }

    @Test
    /**
     * binaryStr_withInteger方法。
     */
    public void binaryStr_withInteger() {
        String result = Bits.binaryStr(5);
        assertEquals("00000000000000000000000000000101", result);
    }

    @Test
    /**
     * binaryStr_withLong方法。
     */
    public void binaryStr_withLong() {
        String result = Bits.binaryStr(5L);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000101", result);
    }

    // --- isOdd / isEven ---

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

    // --- avg ---

    @Test
    /**
     * avg_basic方法。
     */
    public void avg_basic() {
        assertEquals(3, Bits.avg(2, 4));
        assertEquals(3, Bits.avg(3, 3));
        assertEquals(0, Bits.avg(-1, 1));
    }

    // --- isPowFrom2 ---

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

    // --- abs ---

    @Test
    /**
     * abs_positive方法。
     */
    public void abs_positive() {
        assertEquals(5, Bits.abs(5));
        assertEquals(100, Bits.abs(100));
    }

    @Test
    /**
     * abs_negative方法。
     */
    public void abs_negative() {
        assertEquals(5, Bits.abs(-5));
        assertEquals(1, Bits.abs(-1));
    }

    @Test
    /**
     * abs_minValue方法。
     */
    public void abs_minValue() {
        // Integer.MIN_VALUE has no positive counterpart, abs returns MIN_VALUE
        assertEquals(Integer.MIN_VALUE, Bits.abs(Integer.MIN_VALUE));
    }

    // --- mod ---

    @Test
    /**
     * mod_powOf2方法。
     */
    public void mod_powOf2() {
        // x & (mod-1) for mod=8 (2^3)
        assertEquals(0, Bits.mod(8, 8));
        assertEquals(3, Bits.mod(11, 8));
        assertEquals(7, Bits.mod(15, 8));
    }

    @Test
    /**
     * mod_nonPowOf2方法。
     */
    public void mod_nonPowOf2() {
        assertEquals(1, Bits.mod(7, 3));
        assertEquals(2, Bits.mod(8, 3));
    }

    // --- multipleLess ---

    @Test
    /**
     * multipleLess_basic方法。
     */
    public void multipleLess_basic() {
        // multipleLess rounds down to nearest power of 2
        assertEquals(1, Bits.multipleLess(1));
        assertEquals(2, Bits.multipleLess(2));
        assertEquals(2, Bits.multipleLess(3));
        assertEquals(4, Bits.multipleLess(4));
        assertEquals(4, Bits.multipleLess(5));
    }

    // --- multipleMore ---

    @Test
    /**
     * multipleMore_basic方法。
     */
    public void multipleMore_basic() {
        assertTrue(Bits.multipleMore(1) >= 1);
        assertTrue(Bits.multipleMore(5) >= 5);
        assertTrue(Bits.multipleMore(1) >= 1 && Bits.isPowFrom2(Bits.multipleMore(1)));
        assertTrue(Bits.isPowFrom2(Bits.multipleMore(10)));
    }

    // --- setFalse / setTrue / getFlag ---

    @Test
    /**
     * setTrue_getFlag方法。
     */
    public void setTrue_getFlag() {
        long result = Bits.setTrue(0L, 5);
        assertEquals(32L, result);
        assertTrue(Bits.getFlag(result, 5));
        assertFalse(Bits.getFlag(result, 4));
    }

    @Test
    /**
     * setFalse_getFlag方法。
     */
    public void setFalse_getFlag() {
        long allTrue = ~0L;
        long result = Bits.setFalse(allTrue, 5);
        assertFalse(Bits.getFlag(result, 5));
        assertTrue(Bits.getFlag(result, 4));
    }

    @Test
    /**
     * setTrue_middleBit方法。
     */
    public void setTrue_middleBit() {
        // 42 (101010) already has bit 5 set (32), so OR with 32 keeps 42
        long result = Bits.setTrue(42L, 5);
        assertEquals(42L, result);
    }

    // --- getAllTrueIndex / getAllFalseIndex ---

    @Test
    /**
     * getAllTrueIndex方法。
     */
    public void getAllTrueIndex() {
        long flags = Bits.setTrue(Bits.setTrue(0L, 1), 3);
        Set<Byte> trueIndex = Bits.getAllTrueIndex(flags);
        assertEquals(2, trueIndex.size());
        assertTrue(trueIndex.contains((byte) 1));
        assertTrue(trueIndex.contains((byte) 3));
    }

    @Test
    /**
     * getAllFalseIndex方法。
     */
    public void getAllFalseIndex() {
        long flags = Bits.setTrue(0L, 0);
        Set<Byte> falseIndex = Bits.getAllFalseIndex(flags);
        assertEquals(63, falseIndex.size());
        assertFalse(falseIndex.contains((byte) 0));
    }
}
