package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DoublesTest类。
 */
public class DoublesTest {

    @Test
    /**
     * formatDouble_basic方法。
     */
    public void formatDouble_basic() {
        assertEquals("3.14", Doubles.formatDouble(3.14, "#.##"));
    }

    @Test
    /**
     * formatDouble_withTrailingZeros方法。
     */
    public void formatDouble_withTrailingZeros() {
        assertEquals("3.10", Doubles.formatDouble(3.1, "#.00"));
    }

    @Test
    /**
     * formatDouble_integer方法。
     */
    public void formatDouble_integer() {
        assertEquals("5.00", Doubles.formatDouble(5, "#.00"));
    }

    @Test
    /**
     * formatDouble_negative方法。
     */
    public void formatDouble_negative() {
        assertEquals("-3.14", Doubles.formatDouble(-3.14, "#.##"));
    }

    @Test
    /**
     * formatDouble_zero方法。
     */
    public void formatDouble_zero() {
        String result = Doubles.formatDouble(0, "#.00");
        assertNotNull(result);
        assertTrue(result.contains("00"));
    }

    @Test
    /**
     * formatDouble_largeNumber方法。
     */
    public void formatDouble_largeNumber() {
        assertEquals("1234567.89", Doubles.formatDouble(1234567.89, "#.##"));
    }
}
