package com.zifang.util.core.lang;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * PrimitiveUtilTest类。
 */
public class PrimitiveUtilTest {

    @Test
    /**
     * isPrimitive方法。
     */
    public void isPrimitive() {
        assertTrue(PrimitiveUtil.isPrimitive(byte.class));
        assertTrue(PrimitiveUtil.isPrimitive(char.class));
        assertTrue(PrimitiveUtil.isPrimitive(short.class));
        assertTrue(PrimitiveUtil.isPrimitive(int.class));
        assertTrue(PrimitiveUtil.isPrimitive(long.class));
        assertTrue(PrimitiveUtil.isPrimitive(float.class));
        assertTrue(PrimitiveUtil.isPrimitive(double.class));
        assertTrue(PrimitiveUtil.isPrimitive(boolean.class));
        assertFalse(PrimitiveUtil.isPrimitive(String.class));
        assertFalse(PrimitiveUtil.isPrimitive(Byte.class));
    }

    @Test
    /**
     * isPrimitiveWrapper方法。
     */
    public void isPrimitiveWrapper() {
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Byte.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Character.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Short.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Integer.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Long.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Float.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Double.class));
        assertTrue(PrimitiveUtil.isPrimitiveWrapper(Boolean.class));
        assertFalse(PrimitiveUtil.isPrimitiveWrapper(byte.class));
        assertFalse(PrimitiveUtil.isPrimitiveWrapper(String.class));
    }

    @Test
    /**
     * getPrimitive方法。
     */
    public void getPrimitive() {
        assertEquals(PrimitiveUtil.getPrimitive(Byte.class), byte.class);
        assertEquals(PrimitiveUtil.getPrimitive(Character.class), char.class);
        assertEquals(PrimitiveUtil.getPrimitive(Short.class), short.class);
        assertEquals(PrimitiveUtil.getPrimitive(Integer.class), int.class);
        assertEquals(PrimitiveUtil.getPrimitive(Long.class), long.class);
        assertEquals(PrimitiveUtil.getPrimitive(Float.class), float.class);
        assertEquals(PrimitiveUtil.getPrimitive(Double.class), double.class);
        assertEquals(PrimitiveUtil.getPrimitive(Boolean.class), boolean.class);
    }

    @Test(expected = RuntimeException.class)
    /**
     * getPrimitiveThrow方法。
     */
    public void getPrimitiveThrow() {
        assertEquals(PrimitiveUtil.getPrimitive(String.class), byte.class);
    }

    @Test
    /**
     * getPrimitiveWrapper方法。
     */
    public void getPrimitiveWrapper() {
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(byte.class), Byte.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(char.class), Character.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(short.class), Short.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(int.class), Integer.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(long.class), Long.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(float.class), Float.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(double.class), Double.class);
        assertEquals(PrimitiveUtil.getPrimitiveWrapper(boolean.class), Boolean.class);
    }

    @Test
    /**
     * getPrimitiveWrapperThrow方法。
     */
    public void getPrimitiveWrapperThrow() {
        // getPrimitiveWrapper(String.class) returns String.class, not null or exception
        Class<?> result = PrimitiveUtil.getPrimitiveWrapper(String.class);
        assertEquals(String.class, result);
    }

    @Test
    /**
     * isGeneralType方法。
     */
    public void isGeneralType() {
        assertTrue(PrimitiveUtil.isGeneralType(String.class));
        assertFalse(PrimitiveUtil.isGeneralType(byte.class));
        assertFalse(PrimitiveUtil.isGeneralType(Byte.class));
    }

}