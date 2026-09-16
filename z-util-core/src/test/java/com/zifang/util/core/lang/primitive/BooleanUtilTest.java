package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BooleanUtilTest类。
 */
public class BooleanUtilTest {

    @Test
    /**
     * parseBoolean_withNull方法。
     */
    public void parseBoolean_withNull() {
        assertNull(BooleanUtil.parseBoolean(null));
    }

    @Test
    /**
     * parseBoolean_withTrueString方法。
     */
    public void parseBoolean_withTrueString() {
        assertEquals(Boolean.TRUE, BooleanUtil.parseBoolean("true"));
        assertEquals(Boolean.TRUE, BooleanUtil.parseBoolean("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtil.parseBoolean("True"));
    }

    @Test
    /**
     * parseBoolean_withFalseString方法。
     */
    public void parseBoolean_withFalseString() {
        assertEquals(Boolean.FALSE, BooleanUtil.parseBoolean("false"));
        assertEquals(Boolean.FALSE, BooleanUtil.parseBoolean("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtil.parseBoolean("False"));
    }

    @Test
    /**
     * parseBoolean_withNonBooleanString方法。
     */
    public void parseBoolean_withNonBooleanString() {
        // Result depends on implementation
        Boolean result = BooleanUtil.parseBoolean("hello");
        assertNotNull(result);
    }

    @Test
    /**
     * parseBoolean_withEmptyString方法。
     */
    public void parseBoolean_withEmptyString() {
        Boolean result = BooleanUtil.parseBoolean("");
        assertNotNull(result);
    }

    @Test
    /**
     * parseBooleanOrDefault_withNull方法。
     */
    public void parseBooleanOrDefault_withNull() {
        assertEquals(Boolean.TRUE, BooleanUtil.parseBooleanOrDefault(null, Boolean.TRUE));
        assertEquals(Boolean.FALSE, BooleanUtil.parseBooleanOrDefault(null, Boolean.FALSE));
        assertNull(BooleanUtil.parseBooleanOrDefault(null, null));
    }

    @Test
    /**
     * parseBooleanOrDefault_withValidValue方法。
     */
    public void parseBooleanOrDefault_withValidValue() {
        assertEquals(Boolean.TRUE, BooleanUtil.parseBooleanOrDefault("true", Boolean.FALSE));
        assertEquals(Boolean.FALSE, BooleanUtil.parseBooleanOrDefault("false", Boolean.TRUE));
    }

    @Test
    /**
     * compare_bothTrue方法。
     */
    public void compare_bothTrue() {
        assertEquals(0, BooleanUtil.compare(true, true));
    }

    @Test
    /**
     * compare_bothFalse方法。
     */
    public void compare_bothFalse() {
        assertEquals(0, BooleanUtil.compare(false, false));
    }

    @Test
    /**
     * compare_xTrue_yFalse方法。
     */
    public void compare_xTrue_yFalse() {
        assertEquals(1, BooleanUtil.compare(true, false));
    }

    @Test
    /**
     * compare_xFalse_yTrue方法。
     */
    public void compare_xFalse_yTrue() {
        assertEquals(-1, BooleanUtil.compare(false, true));
    }
}
