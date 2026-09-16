package com.zifang.util.core.lang;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * NumberUtilTest类。
 */
public class NumberUtilTest {

    // --- toInt ---

    @Test
    /**
     * testToInt_WithValidString方法。
     */
    public void testToInt_WithValidString() {
        assertEquals(123, NumberUtil.toInt("123"));
    }

    @Test
    /**
     * testToInt_WithNull方法。
     */
    public void testToInt_WithNull() {
        assertEquals(0, NumberUtil.toInt(null));
    }

    @Test
    /**
     * testToInt_WithEmptyString方法。
     */
    public void testToInt_WithEmptyString() {
        assertEquals(0, NumberUtil.toInt(""));
    }

    @Test
    /**
     * testToInt_WithInvalidString方法。
     */
    public void testToInt_WithInvalidString() {
        assertEquals(0, NumberUtil.toInt("abc"));
    }

    @Test
    /**
     * testToInt_WithNegativeNumber方法。
     */
    public void testToInt_WithNegativeNumber() {
        assertEquals(-123, NumberUtil.toInt("-123"));
    }

    // --- toInt with default value ---

    @Test
    /**
     * testToIntWithDefault_WithValidString方法。
     */
    public void testToIntWithDefault_WithValidString() {
        assertEquals(123, NumberUtil.toInt("123", 999));
    }

    @Test
    /**
     * testToIntWithDefault_WithNull方法。
     */
    public void testToIntWithDefault_WithNull() {
        assertEquals(999, NumberUtil.toInt(null, 999));
    }

    @Test
    /**
     * testToIntWithDefault_WithEmptyString方法。
     */
    public void testToIntWithDefault_WithEmptyString() {
        assertEquals(999, NumberUtil.toInt("", 999));
    }

    @Test
    /**
     * testToIntWithDefault_WithInvalidString方法。
     */
    public void testToIntWithDefault_WithInvalidString() {
        assertEquals(999, NumberUtil.toInt("abc", 999));
    }

    // --- toLong ---

    @Test
    /**
     * testToLong_WithValidString方法。
     */
    public void testToLong_WithValidString() {
        assertEquals(123L, NumberUtil.toLong("123"));
    }

    @Test
    /**
     * testToLong_WithNull方法。
     */
    public void testToLong_WithNull() {
        assertEquals(0L, NumberUtil.toLong(null));
    }

    @Test
    /**
     * testToLong_WithEmptyString方法。
     */
    public void testToLong_WithEmptyString() {
        assertEquals(0L, NumberUtil.toLong(""));
    }

    @Test
    /**
     * testToLong_WithInvalidString方法。
     */
    public void testToLong_WithInvalidString() {
        assertEquals(0L, NumberUtil.toLong("abc"));
    }

    // --- toLong with default value ---

    @Test
    /**
     * testToLongWithDefault_WithValidString方法。
     */
    public void testToLongWithDefault_WithValidString() {
        assertEquals(123L, NumberUtil.toLong("123", 999L));
    }

    @Test
    /**
     * testToLongWithDefault_WithNull方法。
     */
    public void testToLongWithDefault_WithNull() {
        assertEquals(999L, NumberUtil.toLong(null, 999L));
    }

    // --- toFloat ---

    @Test
    /**
     * testToFloat_WithValidString方法。
     */
    public void testToFloat_WithValidString() {
        assertEquals(123.5f, NumberUtil.toFloat("123.5"), 0.001);
    }

    @Test
    /**
     * testToFloat_WithNull方法。
     */
    public void testToFloat_WithNull() {
        assertEquals(0.0f, NumberUtil.toFloat(null), 0.001);
    }

    @Test
    /**
     * testToFloat_WithEmptyString方法。
     */
    public void testToFloat_WithEmptyString() {
        assertEquals(0.0f, NumberUtil.toFloat(""), 0.001);
    }

    // --- toFloat with default value ---

    @Test
    /**
     * testToFloatWithDefault_WithValidString方法。
     */
    public void testToFloatWithDefault_WithValidString() {
        assertEquals(123.5f, NumberUtil.toFloat("123.5", 999.0f), 0.001);
    }

    @Test
    /**
     * testToFloatWithDefault_WithNull方法。
     */
    public void testToFloatWithDefault_WithNull() {
        assertEquals(999.0f, NumberUtil.toFloat(null, 999.0f), 0.001);
    }

    // --- toDouble ---

    @Test
    /**
     * testToDouble_WithValidString方法。
     */
    public void testToDouble_WithValidString() {
        assertEquals(123.5, NumberUtil.toDouble("123.5"), 0.001);
    }

    @Test
    /**
     * testToDouble_WithNull方法。
     */
    public void testToDouble_WithNull() {
        assertEquals(0.0d, NumberUtil.toDouble((String) null), 0.001);
    }

    @Test
    /**
     * testToDouble_WithEmptyString方法。
     */
    public void testToDouble_WithEmptyString() {
        assertEquals(0.0d, NumberUtil.toDouble(""), 0.001);
    }

    // --- toDouble with default value ---

    @Test
    /**
     * testToDoubleWithDefault_WithValidString方法。
     */
    public void testToDoubleWithDefault_WithValidString() {
        assertEquals(123.5, NumberUtil.toDouble("123.5", 999.0), 0.001);
    }

    @Test
    /**
     * testToDoubleWithDefault_WithNull方法。
     */
    public void testToDoubleWithDefault_WithNull() {
        assertEquals(999.0, NumberUtil.toDouble((String) null, 999.0), 0.001);
    }

    // --- toDouble with BigDecimal ---

    @Test
    /**
     * testToDoubleWithBigDecimal_WithValidValue方法。
     */
    public void testToDoubleWithBigDecimal_WithValidValue() {
        BigDecimal bd = new BigDecimal("123.456");
        assertEquals(123.456, NumberUtil.toDouble(bd), 0.001);
    }

    @Test
    /**
     * testToDoubleWithBigDecimal_WithNull方法。
     */
    public void testToDoubleWithBigDecimal_WithNull() {
        assertEquals(0.0d, NumberUtil.toDouble((BigDecimal) null), 0.001);
    }

    @Test
    /**
     * testToDoubleWithBigDecimalWithDefault_WithValidValue方法。
     */
    public void testToDoubleWithBigDecimalWithDefault_WithValidValue() {
        BigDecimal bd = new BigDecimal("123.456");
        assertEquals(123.456, NumberUtil.toDouble(bd, 999.0), 0.001);
    }

    @Test
    /**
     * testToDoubleWithBigDecimalWithDefault_WithNull方法。
     */
    public void testToDoubleWithBigDecimalWithDefault_WithNull() {
        assertEquals(999.0, NumberUtil.toDouble((BigDecimal) null, 999.0), 0.001);
    }

    // --- toByte ---

    @Test
    /**
     * testToByte_WithValidString方法。
     */
    public void testToByte_WithValidString() {
        assertEquals((byte) 123, NumberUtil.toByte("123"));
    }

    @Test
    /**
     * testToByte_WithNull方法。
     */
    public void testToByte_WithNull() {
        assertEquals((byte) 0, NumberUtil.toByte(null));
    }

    // --- toByte with default value ---

    @Test
    /**
     * testToByteWithDefault_WithValidString方法。
     */
    public void testToByteWithDefault_WithValidString() {
        assertEquals((byte) 123, NumberUtil.toByte("123", (byte) 99));
    }

    @Test
    /**
     * testToByteWithDefault_WithNull方法。
     */
    public void testToByteWithDefault_WithNull() {
        assertEquals((byte) 99, NumberUtil.toByte(null, (byte) 99));
    }

    // --- toShort ---

    @Test
    /**
     * testToShort_WithValidString方法。
     */
    public void testToShort_WithValidString() {
        assertEquals((short) 123, NumberUtil.toShort("123"));
    }

    @Test
    /**
     * testToShort_WithNull方法。
     */
    public void testToShort_WithNull() {
        assertEquals((short) 0, NumberUtil.toShort(null));
    }

    // --- toShort with default value ---

    @Test
    /**
     * testToShortWithDefault_WithValidString方法。
     */
    public void testToShortWithDefault_WithValidString() {
        assertEquals((short) 123, NumberUtil.toShort("123", (short) 99));
    }

    @Test
    /**
     * testToShortWithDefault_WithNull方法。
     */
    public void testToShortWithDefault_WithNull() {
        assertEquals((short) 99, NumberUtil.toShort(null, (short) 99));
    }

    // --- toScaledBigDecimal ---

    @Test
    /**
     * testToScaledBigDecimal_WithValidValue方法。
     */
    public void testToScaledBigDecimal_WithValidValue() {
        BigDecimal bd = new BigDecimal("123.4567");
        BigDecimal result = NumberUtil.toScaledBigDecimal(bd);
        assertNotNull(result);
        assertEquals(2, result.scale());
    }

    @Test
    /**
     * testToScaledBigDecimal_WithNull方法。
     */
    public void testToScaledBigDecimal_WithNull() {
        BigDecimal result = NumberUtil.toScaledBigDecimal((Double) null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    /**
     * testToScaledBigDecimal_WithScaleAndRoundingMode方法。
     */
    public void testToScaledBigDecimal_WithScaleAndRoundingMode() {
        BigDecimal bd = new BigDecimal("123.4567");
        BigDecimal result = NumberUtil.toScaledBigDecimal(bd, 3, RoundingMode.HALF_UP);
        assertNotNull(result);
        assertEquals(3, result.scale());
    }

    // --- toScaledBigDecimal with Float ---

    @Test
    /**
     * testToScaledBigDecimalWithFloat_WithValidValue方法。
     */
    public void testToScaledBigDecimalWithFloat_WithValidValue() {
        BigDecimal result = NumberUtil.toScaledBigDecimal(123.45f);
        assertNotNull(result);
        assertEquals(2, result.scale());
    }

    @Test
    /**
     * testToScaledBigDecimalWithFloat_WithNull方法。
     */
    public void testToScaledBigDecimalWithFloat_WithNull() {
        BigDecimal result = NumberUtil.toScaledBigDecimal((Float) null);
        assertEquals(BigDecimal.ZERO, result);
    }

    // --- Constants ---

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals(Long.valueOf(0L), NumberUtil.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtil.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtil.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtil.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtil.INTEGER_ONE);
        assertEquals(Integer.valueOf(2), NumberUtil.INTEGER_TWO);
        assertEquals(Integer.valueOf(-1), NumberUtil.INTEGER_MINUS_ONE);
        assertEquals(Short.valueOf((short) 0), NumberUtil.SHORT_ZERO);
        assertEquals(Short.valueOf((short) 1), NumberUtil.SHORT_ONE);
        assertEquals(Short.valueOf((short) -1), NumberUtil.SHORT_MINUS_ONE);
        assertEquals(Byte.valueOf((byte) 0), NumberUtil.BYTE_ZERO);
        assertEquals(Byte.valueOf((byte) 1), NumberUtil.BYTE_ONE);
        assertEquals(Byte.valueOf((byte) -1), NumberUtil.BYTE_MINUS_ONE);
        assertEquals(Double.valueOf(0.0d), NumberUtil.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtil.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtil.DOUBLE_MINUS_ONE);
        assertEquals(Float.valueOf(0.0f), NumberUtil.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtil.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtil.FLOAT_MINUS_ONE);
        assertEquals(Long.valueOf(Integer.MAX_VALUE), NumberUtil.LONG_INT_MAX_VALUE);
        assertEquals(Long.valueOf(Integer.MIN_VALUE), NumberUtil.LONG_INT_MIN_VALUE);
    }
}
