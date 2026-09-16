package com.zifang.util.core.time;

import org.junit.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * SqlDateUtilTest类。
 */
public class SqlDateUtilTest {

    @Test
    /**
     * testFormat方法。
     */
    public void testFormat() {
        Date date = Date.valueOf("2024-06-15");
        String result = SqlDateUtil.format(date);
        assertEquals("2024-06-15", result);
    }

    @Test
    /**
     * testFormatWithPattern方法。
     */
    public void testFormatWithPattern() {
        Date date = Date.valueOf("2024-06-15");
        String result = SqlDateUtil.format(date, "yyyyMMdd");
        assertEquals("20240615", result);
    }

    @Test
    /**
     * testFormatNull方法。
     */
    public void testFormatNull() {
        assertNull(SqlDateUtil.format(null));
        assertNull(SqlDateUtil.format(null, "yyyy-MM-dd"));
    }

    @Test
    /**
     * testParse方法。
     */
    public void testParse() {
        Date date = SqlDateUtil.parse("2024-06-15");
        assertNotNull(date);
        assertEquals(2024, date.toLocalDate().getYear());
        assertEquals(6, date.toLocalDate().getMonthValue());
        assertEquals(15, date.toLocalDate().getDayOfMonth());
    }

    @Test
    /**
     * testParseCompact方法。
     */
    public void testParseCompact() {
        Date date = SqlDateUtil.parse("20240615", "yyyyMMdd");
        assertNotNull(date);
        assertEquals(2024, date.toLocalDate().getYear());
    }

    @Test
    /**
     * testParseNull方法。
     */
    public void testParseNull() {
        assertNull(SqlDateUtil.parse(null));
        assertNull(SqlDateUtil.parse(""));
    }

    @Test
    /**
     * testNow方法。
     */
    public void testNow() {
        Date now = SqlDateUtil.now();
        assertNotNull(now);
        assertTrue(now.getTime() > 0);
    }

    @Test
    /**
     * testPlusMinusDays方法。
     */
    public void testPlusMinusDays() {
        Date base = Date.valueOf("2024-06-15");
        Date plus = SqlDateUtil.plusDays(base, 5);
        Date minus = SqlDateUtil.minusDays(base, 5);
        assertEquals(Date.valueOf("2024-06-20"), plus);
        assertEquals(Date.valueOf("2024-06-10"), minus);
    }

    @Test
    /**
     * testPlusMinusMonths方法。
     */
    public void testPlusMinusMonths() {
        Date base = Date.valueOf("2024-06-15");
        Date plus = SqlDateUtil.plusMonths(base, 2);
        Date minus = SqlDateUtil.minusMonths(base, 2);
        assertEquals(Date.valueOf("2024-08-15"), plus);
        assertEquals(Date.valueOf("2024-04-15"), minus);
    }

    @Test
    /**
     * testPlusMinusYears方法。
     */
    public void testPlusMinusYears() {
        Date base = Date.valueOf("2024-06-15");
        Date plus = SqlDateUtil.plusYears(base, 1);
        Date minus = SqlDateUtil.minusYears(base, 1);
        assertEquals(Date.valueOf("2025-06-15"), plus);
        assertEquals(Date.valueOf("2023-06-15"), minus);
    }

    @Test
    /**
     * testCompare方法。
     */
    public void testCompare() {
        Date earlier = Date.valueOf("2024-06-15");
        Date later = Date.valueOf("2024-06-20");
        assertTrue(SqlDateUtil.isBefore(earlier, later));
        assertTrue(SqlDateUtil.isAfter(later, earlier));
    }

    @Test
    /**
     * testIsSameDay方法。
     */
    public void testIsSameDay() {
        Date date1 = Date.valueOf("2024-06-15");
        Date date2 = Date.valueOf("2024-06-15");
        Date date3 = Date.valueOf("2024-06-16");
        assertTrue(SqlDateUtil.isSameDay(date1, date2));
        assertFalse(SqlDateUtil.isSameDay(date1, date3));
    }

    @Test
    /**
     * testDaysBetween方法。
     */
    public void testDaysBetween() {
        Date start = Date.valueOf("2024-06-01");
        Date end = Date.valueOf("2024-06-15");
        assertEquals(14, SqlDateUtil.daysBetween(start, end));
    }

    @Test
    /**
     * testGetParts方法。
     */
    public void testGetParts() {
        Date date = Date.valueOf("2024-06-15");
        assertEquals(2024, SqlDateUtil.getYear(date));
        assertEquals(6, SqlDateUtil.getMonth(date));
        assertEquals(15, SqlDateUtil.getDayOfMonth(date));
    }

    @Test
    /**
     * testToLocalDate方法。
     */
    public void testToLocalDate() {
        Date date = Date.valueOf("2024-06-15");
        LocalDate ld = SqlDateUtil.toLocalDate(date);
        assertNotNull(ld);
        assertEquals(2024, ld.getYear());
        assertEquals(6, ld.getMonthValue());
        assertEquals(15, ld.getDayOfMonth());
    }

    @Test
    /**
     * testToLocalDateTime方法。
     */
    public void testToLocalDateTime() {
        Date date = Date.valueOf("2024-06-15");
        LocalDateTime ldt = SqlDateUtil.toLocalDateTime(date);
        assertNotNull(ldt);
        assertEquals(2024, ldt.getYear());
        assertEquals(6, ldt.getMonthValue());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(0, ldt.getHour());
        assertEquals(0, ldt.getMinute());
        assertEquals(0, ldt.getSecond());
    }

    @Test
    /**
     * testToLocalTime方法。
     */
    public void testToLocalTime() {
        Date date = Date.valueOf("2024-06-15");
        LocalTime lt = SqlDateUtil.toLocalTime(date);
        assertNotNull(lt);
        assertEquals(0, lt.getHour());
        assertEquals(0, lt.getMinute());
        assertEquals(0, lt.getSecond());
    }

    @Test
    /**
     * testToUtilDate方法。
     */
    public void testToUtilDate() {
        Date date = Date.valueOf("2024-06-15");
        java.util.Date utilDate = SqlDateUtil.toUtilDate(date);
        assertNotNull(utilDate);
        assertEquals(date.getTime(), utilDate.getTime());
    }

    @Test
    /**
     * testToInstant方法。
     */
    public void testToInstant() {
        Date date = Date.valueOf("2024-06-15");
        Instant instant = SqlDateUtil.toInstant(date);
        assertNotNull(instant);
        assertEquals(Instant.ofEpochMilli(date.getTime()), instant);
    }

    @Test
    /**
     * testFromLocalDate方法。
     */
    public void testFromLocalDate() {
        LocalDate ld = LocalDate.of(2024, 6, 15);
        Date date = SqlDateUtil.fromLocalDate(ld);
        assertNotNull(date);
        assertEquals(ld, date.toLocalDate());
    }

    @Test
    /**
     * testFromLocalDateTime方法。
     */
    public void testFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
        Date date = SqlDateUtil.fromLocalDateTime(ldt);
        assertNotNull(date);
        assertEquals(LocalDate.of(2024, 6, 15), date.toLocalDate());
    }

    @Test
    /**
     * testFromEpochMilli方法。
     */
    public void testFromEpochMilli() {
        Date date = SqlDateUtil.fromEpochMilli(1718455200000L);
        assertNotNull(date);
        assertEquals(1718455200000L, date.getTime());
    }

    @Test
    /**
     * testNullHandling方法。
     */
    public void testNullHandling() {
        assertNull(SqlDateUtil.toLocalDate(null));
        assertNull(SqlDateUtil.toLocalDateTime(null));
        assertNull(SqlDateUtil.toLocalTime(null));
        assertNull(SqlDateUtil.toUtilDate(null));
        assertNull(SqlDateUtil.toInstant(null));
        assertNull(SqlDateUtil.fromLocalDate(null));
        assertNull(SqlDateUtil.fromLocalDateTime(null));
        assertEquals(0, SqlDateUtil.toEpochMilli(null));
    }
}
