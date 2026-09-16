package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * FloatUtilTest类。
 */
public class FloatUtilTest {

    @Test
    /**
     * parseFloat_withNull方法。
     */
    public void parseFloat_withNull() {
        assertNull(FloatUtil.parseFloat(null));
    }

    @Test
    /**
     * parseFloat_withValidString方法。
     */
    public void parseFloat_withValidString() {
        assertEquals(Float.valueOf("0.0"), FloatUtil.parseFloat("0"));
        assertEquals(Float.valueOf("3.14"), FloatUtil.parseFloat("3.14"));
        assertEquals(Float.valueOf("-2.5"), FloatUtil.parseFloat("-2.5"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), FloatUtil.parseFloat(String.valueOf(Float.MAX_VALUE)));
        assertEquals(Float.valueOf(Float.MIN_VALUE), FloatUtil.parseFloat(String.valueOf(Float.MIN_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * parseFloat_withInvalidString方法。
     */
    public void parseFloat_withInvalidString() {
        FloatUtil.parseFloat("not a number");
    }

    @Test
    /**
     * parseFloat_withObject方法。
     */
    public void parseFloat_withObject() {
        assertEquals(Float.valueOf("100.5"), FloatUtil.parseFloat(Double.valueOf(100.5)));
    }

    @Test
    /**
     * parseFloat_withSpecialValues方法。
     */
    public void parseFloat_withSpecialValues() {
        assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), FloatUtil.parseFloat("Infinity"));
        assertEquals(Float.valueOf(Float.NEGATIVE_INFINITY), FloatUtil.parseFloat("-Infinity"));
        assertEquals(Float.valueOf(Float.NaN), FloatUtil.parseFloat("NaN"));
    }

    @Test
    /**
     * parseFloatOrDefault_withNull方法。
     */
    public void parseFloatOrDefault_withNull() {
        assertEquals(Float.valueOf("99.9"), FloatUtil.parseFloatOrDefault(null, Float.valueOf("99.9")));
        assertNull(FloatUtil.parseFloatOrDefault(null, null));
    }

    @Test
    /**
     * parseFloatOrDefault_withValidValue方法。
     */
    public void parseFloatOrDefault_withValidValue() {
        assertEquals(Float.valueOf("3.14"), FloatUtil.parseFloatOrDefault("3.14", Float.valueOf("99.9")));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * parseFloatOrDefault_withInvalidValue方法。
     */
    public void parseFloatOrDefault_withInvalidValue() {
        FloatUtil.parseFloatOrDefault("notanumber", Float.valueOf("0"));
    }
}
