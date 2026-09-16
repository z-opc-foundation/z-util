package com.zifang.util.media.graph.qrcode.encoder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ErrorCorrectionLevelTest类。
 */
public class ErrorCorrectionLevelTest {

    @Test
    /**
     * testLLevel方法。
     */
    public void testLLevel() {
        assertEquals(1, ErrorCorrectionLevel.L.getBits());
        assertEquals(7, ErrorCorrectionLevel.L.getRecoveryPercent());
    }

    @Test
    /**
     * testMLevel方法。
     */
    public void testMLevel() {
        assertEquals(0, ErrorCorrectionLevel.M.getBits());
        assertEquals(15, ErrorCorrectionLevel.M.getRecoveryPercent());
    }

    @Test
    /**
     * testQLevel方法。
     */
    public void testQLevel() {
        assertEquals(3, ErrorCorrectionLevel.Q.getBits());
        assertEquals(25, ErrorCorrectionLevel.Q.getRecoveryPercent());
    }

    @Test
    /**
     * testHLevel方法。
     */
    public void testHLevel() {
        assertEquals(2, ErrorCorrectionLevel.H.getBits());
        assertEquals(30, ErrorCorrectionLevel.H.getRecoveryPercent());
    }

    @Test
    /**
     * testFromBits方法。
     */
    public void testFromBits() {
        assertEquals(ErrorCorrectionLevel.L, ErrorCorrectionLevel.fromBits(1));
        assertEquals(ErrorCorrectionLevel.M, ErrorCorrectionLevel.fromBits(0));
        assertEquals(ErrorCorrectionLevel.Q, ErrorCorrectionLevel.fromBits(3));
        assertEquals(ErrorCorrectionLevel.H, ErrorCorrectionLevel.fromBits(2));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testFromBitsInvalid方法。
     */
    public void testFromBitsInvalid() {
        ErrorCorrectionLevel.fromBits(99);
    }

    @Test
    /**
     * testAllValues方法。
     */
    public void testAllValues() {
        ErrorCorrectionLevel[] levels = ErrorCorrectionLevel.values();
        assertEquals(4, levels.length);
    }
}
