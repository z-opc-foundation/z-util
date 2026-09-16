package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * LongUtilTest类。
 */
public class LongUtilTest {

    @Test
    /**
     * parseLong_withNull方法。
     */
    public void parseLong_withNull() {
        assertNull(LongUtil.parseLong(null));
    }

    @Test
    /**
     * parseLong_withValidString方法。
     */
    public void parseLong_withValidString() {
        assertEquals(Long.valueOf(0), LongUtil.parseLong("0"));
        assertEquals(Long.valueOf(123456789L), LongUtil.parseLong("123456789"));
        assertEquals(Long.valueOf(-987654321L), LongUtil.parseLong("-987654321"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), LongUtil.parseLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.valueOf(Long.MIN_VALUE), LongUtil.parseLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * parseLong_withInvalidString方法。
     */
    public void parseLong_withInvalidString() {
        LongUtil.parseLong("not a number");
    }

    @Test
    /**
     * parseLong_withObject方法。
     */
    public void parseLong_withObject() {
        assertEquals(Long.valueOf(100L), LongUtil.parseLong(Integer.valueOf(100)));
    }

    @Test
    /**
     * parseLongOrDefault_withNull方法。
     */
    public void parseLongOrDefault_withNull() {
        assertEquals(Long.valueOf(999L), LongUtil.parseLongOrDefault(null, Long.valueOf(999)));
        assertNull(LongUtil.parseLongOrDefault(null, null));
    }

    @Test
    /**
     * parseLongOrDefault_withValidValue方法。
     */
    public void parseLongOrDefault_withValidValue() {
        assertEquals(Long.valueOf(42L), LongUtil.parseLongOrDefault("42", Long.valueOf(999)));
    }

    @Test
    /**
     * length_withNull方法。
     */
    public void length_withNull() {
        assertEquals(0, LongUtil.length(null));
    }

    @Test
    /**
     * length_withPositiveNumber方法。
     */
    public void length_withPositiveNumber() {
        assertEquals(1, LongUtil.length(0L));
        assertEquals(1, LongUtil.length(1L));
        assertEquals(3, LongUtil.length(123L));
        assertEquals(10, LongUtil.length(1234567890L));
    }

    @Test
    /**
     * length_withNegativeNumber方法。
     */
    public void length_withNegativeNumber() {
        assertEquals(2, LongUtil.length(-1L));
        assertEquals(4, LongUtil.length(-123L));
    }

    @Test
    /**
     * length_withMaxMin方法。
     */
    public void length_withMaxMin() {
        assertEquals(19, LongUtil.length(Long.MAX_VALUE));
        assertEquals(20, LongUtil.length(Long.MIN_VALUE));
    }
}
