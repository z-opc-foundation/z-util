package com.zifang.util.core.time;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * SmartDateParserTest类。
 */
public class SmartDateParserTest {

    // ==================== 纯数字定长格式 ====================

    @Test
    /**
     * testParseCompact方法。
     */
    public void testParseCompact() {
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0), SmartDateParser.parseDateTime("2026"));
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0, 0), SmartDateParser.parseDateTime("202608"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0), SmartDateParser.parseDateTime("20260822"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 0, 0), SmartDateParser.parseDateTime("2026082214"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 30, 0), SmartDateParser.parseDateTime("202608221430"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 30, 5), SmartDateParser.parseDateTime("20260822143005"));
    }

    @Test
    /**
     * testParseCompact_invalidLength方法。
     */
    public void testParseCompact_invalidLength() {
        assertNull(SmartDateParser.parseDateTime("20260"));
        assertNull(SmartDateParser.parseDateTime("2026082"));
        assertNull(SmartDateParser.parseDateTime("202608221"));
        assertNull(SmartDateParser.parseDateTime("202608221430051"));
        assertNull(SmartDateParser.parseDateTime(""));
    }

    // ==================== 分隔符格式 ====================

    @Test
    /**
     * testParseSegmented方法。
     */
    public void testParseSegmented() {
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0), SmartDateParser.parseDateTime("2026-08-22"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0), SmartDateParser.parseDateTime("2026/08/22"));
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0), SmartDateParser.parseDateTime("2026.08.22"));
        // 单位数字字段
        assertEquals(LocalDateTime.of(2026, 8, 2, 0, 0, 0), SmartDateParser.parseDateTime("2026-8-2"));
        // 日期 + 时分
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 30, 0), SmartDateParser.parseDateTime("2026-08-22 14:30"));
        // 日期 + 时分秒
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 30, 5), SmartDateParser.parseDateTime("2026-08-22 14:30:05"));
        // 混合分隔符
        assertEquals(LocalDateTime.of(2026, 8, 22, 14, 30, 5), SmartDateParser.parseDateTime("2026年08月22日 14时30分05秒"));
    }

    @Test
    /**
     * testParseSegmented_twoDigitYear方法。
     */
    public void testParseSegmented_twoDigitYear() {
        // 两位年份按2000年窗口解释
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0), SmartDateParser.parseDateTime("26-08-22"));
    }

    @Test
    /**
     * testParseSegmented_invalidFieldCount方法。
     */
    public void testParseSegmented_invalidFieldCount() {
        // 字段数少于3（只有时间）或多于6
        assertNull(SmartDateParser.parseDateTime("2026-08"));
        assertNull(SmartDateParser.parseDateTime("14-30"));
        assertNull(SmartDateParser.parseDateTime("2026-08-22 14:30:05.123456"));
    }

    @Test
    /**
     * testParseSegmented_invalidDate方法。
     */
    public void testParseSegmented_invalidDate() {
        // 严格校验：越界日期返回null
        assertNull(SmartDateParser.parseDateTime("2026-02-31"));
        assertNull(SmartDateParser.parseDateTime("2026-13-01"));
        assertNull(SmartDateParser.parseDateTime("2026-08-22 25:30:00"));
        assertNull(SmartDateParser.parseDateTime("2026-08-22 14:60:00"));
        assertNull(SmartDateParser.parseDateTime("20260231"));
    }

    // ==================== 纯时间格式 ====================

    @Test
    /**
     * testParseTimeOfToday方法。
     */
    public void testParseTimeOfToday() {
        LocalDateTime result = SmartDateParser.parseDateTime("14:30");
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.toLocalDate());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(0, result.getSecond());

        LocalDateTime resultWithSecond = SmartDateParser.parseDateTime("9:5:7");
        assertNotNull(resultWithSecond);
        assertEquals(9, resultWithSecond.getHour());
        assertEquals(5, resultWithSecond.getMinute());
        assertEquals(7, resultWithSecond.getSecond());
    }

    // ==================== 其他入口与边界 ====================

    @Test
    /**
     * testParseDate方法。
     */
    public void testParseDate() {
        assertEquals(LocalDate.of(2026, 8, 22), SmartDateParser.parseDate("2026-08-22 14:30:05"));
        assertEquals(LocalDate.of(2026, 8, 22), SmartDateParser.parseDate("20260822"));
        assertNull(SmartDateParser.parseDate("not-a-date"));
    }

    @Test
    /**
     * testParse方法。
     */
    public void testParse() {
        Date date = SmartDateParser.parse("2026-08-22 14:30:05");
        assertNotNull(date);
        String formatted = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2026-08-22 14:30:05", formatted);
    }

    @Test
    /**
     * testParse_nullAndGarbage方法。
     */
    public void testParse_nullAndGarbage() {
        assertNull(SmartDateParser.parse(null));
        assertNull(SmartDateParser.parse(""));
        assertNull(SmartDateParser.parse("   "));
        assertNull(SmartDateParser.parse("abc"));
        assertNull(SmartDateParser.parse("2026-aa-22"));
    }

    @Test
    /**
     * testParse_leapYear方法。
     */
    public void testParse_leapYear() {
        assertEquals(LocalDateTime.of(2024, 2, 29, 0, 0, 0), SmartDateParser.parseDateTime("2024-02-29"));
        assertNull(SmartDateParser.parseDateTime("2023-02-29"));
    }
}
