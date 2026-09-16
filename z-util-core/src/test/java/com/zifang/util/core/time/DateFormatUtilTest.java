package com.zifang.util.core.time;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * DateFormatUtilTest类。
 */
public class DateFormatUtilTest {

    @Test
    /**
     * testFormatString14ToDateTime方法。
     */
    public void testFormatString14ToDateTime() {
        String input = "20240615183045";
        String result = DateFormatUtil.formatString(input);
        assertEquals("2024-06-15 18:30:45", result);
    }

    @Test
    /**
     * testFormatString19ToCompact方法。
     */
    public void testFormatString19ToCompact() {
        String input = "2024-06-15 18:30:45";
        String result = DateFormatUtil.formatString(input);
        assertEquals("20240615183045", result);
    }

    @Test
    /**
     * testFormatString10ToCompact方法。
     */
    public void testFormatString10ToCompact() {
        String input = "2024-06-15";
        String result = DateFormatUtil.formatString(input);
        assertEquals("20240615", result);
    }

    @Test
    /**
     * testFormatString8ToDate方法。
     */
    public void testFormatString8ToDate() {
        String input = "20240615";
        String result = DateFormatUtil.formatString(input);
        assertEquals("2024-06-15", result);
    }

    @Test
    /**
     * testFormatStringUnknown方法。
     */
    public void testFormatStringUnknown() {
        assertEquals("abc", DateFormatUtil.formatString("abc"));
        assertEquals("12345", DateFormatUtil.formatString("12345"));
    }

    @Test
    /**
     * testFormatStringNull方法。
     */
    public void testFormatStringNull() {
        assertEquals("", DateFormatUtil.formatString(null));
        assertEquals("", DateFormatUtil.formatString(""));
    }

    @Test
    /**
     * testParseDateCompact方法。
     */
    public void testParseDateCompact() {
        Date result = DateFormatUtil.parse("20240615");
        assertNotNull(result);
    }

    @Test
    /**
     * testParseDateFull方法。
     */
    public void testParseDateFull() {
        Date result = DateFormatUtil.parse("20240615183045");
        assertNotNull(result);
    }

    @Test
    /**
     * testParseDateNull方法。
     */
    public void testParseDateNull() {
        assertNull(DateFormatUtil.parse(null));
        assertNull(DateFormatUtil.parse(""));
    }

    @Test
    /**
     * testParseWithOutPattern方法。
     */
    public void testParseWithOutPattern() {
        Date result = DateFormatUtil.parse("20240615", "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    /**
     * testParseInvalid方法。
     */
    public void testParseInvalid() {
        assertNull(DateFormatUtil.parse("invalid"));
        assertNull(DateFormatUtil.parse("2024-06-15 10:30:00")); // wrong length
    }

    @Test
    /**
     * testFormatDateWithPattern方法。
     */
    public void testFormatDateWithPattern() {
        Date date = new Date(1718455200000L);
        String result = DateFormatUtil.format(date, "yyyy-MM-dd");
        assertNotNull(result);
        assertTrue(result.startsWith("2024"));
    }

    @Test
    /**
     * testFormatDateNull方法。
     */
    public void testFormatDateNull() {
        assertEquals("", DateFormatUtil.format(null, "yyyy-MM-dd"));
        assertEquals("", DateFormatUtil.format(null, null));
    }
}
