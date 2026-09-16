package com.zifang.util.core.time;

import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * DateUtilTest类。
 */
public class DateUtilTest {

    // ==================== 格式化测试 ====================

    @Test
    /**
     * testFormat_withDefaultPattern方法。
     */
    public void testFormat_withDefaultPattern() {
        Date date = new Date(0);
        String result = DateUtil.format(date);
        assertNotNull(result);
        assertTrue(result.contains("1970"));
    }

    @Test
    /**
     * testFormat_withDatePattern方法。
     */
    public void testFormat_withDatePattern() {
        Date date = new Date(0);
        String result = DateUtil.format(date, DateUtil.PATTERN_DATE);
        assertEquals("1970-01-01", result);
    }

    @Test
    /**
     * testFormat_withNullDate方法。
     */
    public void testFormat_withNullDate() {
        assertNull(DateUtil.format(null));
        assertNull(DateUtil.format(null, DateUtil.PATTERN_DEFAULT));
    }

    // ==================== 解析测试 ====================

    @Test
    /**
     * testParse_withDefaultPattern方法。
     */
    public void testParse_withDefaultPattern() throws ParseException {
        Date date = DateUtil.parse("2023-01-01 12:00:00");
        assertNotNull(date);
        assertEquals(2023 - 1900, date.getYear());
        assertEquals(0, date.getMonth());
        assertEquals(1, date.getDate());
    }

    @Test
    /**
     * testParse_withDatePattern方法。
     */
    public void testParse_withDatePattern() throws ParseException {
        Date date = DateUtil.parse("2023-01-01", DateUtil.PATTERN_DATE);
        assertNotNull(date);
        assertEquals(2023 - 1900, date.getYear());
        assertEquals(0, date.getMonth());
        assertEquals(1, date.getDate());
    }

    @Test
    /**
     * testParse_withNull方法。
     */
    public void testParse_withNull() throws ParseException {
        assertNull(DateUtil.parse(null));
        assertNull(DateUtil.parse(""));
        assertNull(DateUtil.parse("  "));
    }

    @Test
    /**
     * testParseStrict_withMultiplePatterns方法。
     */
    public void testParseStrict_withMultiplePatterns() {
        Date date = DateUtil.parseStrict("2023-01-01", "yyyy-MM-dd", "yyyy/MM/dd");
        assertNotNull(date);

        Date date2 = DateUtil.parseStrict("2023/01/01", "yyyy-MM-dd", "yyyy/MM/dd");
        assertNotNull(date2);
    }

    // ==================== 获取测试 ====================

    @Test
    /**
     * testNow方法。
     */
    public void testNow() {
        Date now = DateUtil.now();
        assertNotNull(now);
        assertTrue(now.getTime() > 0);
    }

    @Test
    /**
     * testTodayStart方法。
     */
    public void testTodayStart() {
        Date todayStart = DateUtil.todayStart();
        assertNotNull(todayStart);
        LocalDateTime ldt = DateUtil.toLocalDateTime(todayStart);
        assertEquals(0, ldt.getHour());
        assertEquals(0, ldt.getMinute());
        assertEquals(0, ldt.getSecond());
    }

    @Test
    /**
     * testTodayEnd方法。
     */
    public void testTodayEnd() {
        Date todayEnd = DateUtil.todayEnd();
        assertNotNull(todayEnd);
        LocalDateTime ldt = DateUtil.toLocalDateTime(todayEnd);
        assertEquals(23, ldt.getHour());
        assertEquals(59, ldt.getMinute());
        assertEquals(59, ldt.getSecond());
    }

    @Test
    /**
     * testDayStart方法。
     */
    public void testDayStart() {
        Date date = new Date();
        Date dayStart = DateUtil.dayStart(date);
        assertNotNull(dayStart);
    }

    @Test
    /**
     * testDayEnd方法。
     */
    public void testDayEnd() {
        Date date = new Date();
        Date dayEnd = DateUtil.dayEnd(date);
        assertNotNull(dayEnd);
    }

    @Test
    /**
     * testGetDayStart_offset方法。
     */
    public void testGetDayStart_offset() {
        LocalDateTime todayStart = DateUtil.getDayStart(0);
        assertNotNull(todayStart);
        assertEquals(0, todayStart.getHour());
        assertEquals(0, todayStart.getMinute());
        assertEquals(0, todayStart.getSecond());

        LocalDateTime tomorrowStart = DateUtil.getDayStart(1);
        assertNotNull(tomorrowStart);

        LocalDateTime yesterdayStart = DateUtil.getDayStart(-1);
        assertNotNull(yesterdayStart);
    }

    @Test
    /**
     * testGetDayEnd_offset方法。
     */
    public void testGetDayEnd_offset() {
        LocalDateTime todayEnd = DateUtil.getDayEnd(0);
        assertNotNull(todayEnd);
        assertEquals(23, todayEnd.getHour());
        assertEquals(59, todayEnd.getMinute());
        assertEquals(59, todayEnd.getSecond());
    }

    // ==================== 日期计算测试 ====================

    @Test
    /**
     * testPlusDays方法。
     */
    public void testPlusDays() {
        Date date = new Date(0);
        Date result = DateUtil.plusDays(date, 1);
        assertNotNull(result);
        assertTrue(result.getTime() > date.getTime());
    }

    @Test
    /**
     * testMinusDays方法。
     */
    public void testMinusDays() {
        Date date = new Date(0);
        Date result = DateUtil.minusDays(date, 1);
        assertNotNull(result);
        assertTrue(result.getTime() < date.getTime());
    }

    @Test
    /**
     * testPlusHours方法。
     */
    public void testPlusHours() {
        Date date = new Date(0);
        Date result = DateUtil.plusHours(date, 1);
        assertNotNull(result);
    }

    @Test
    /**
     * testMinusHours方法。
     */
    public void testMinusHours() {
        Date date = new Date(0);
        Date result = DateUtil.minusHours(date, 1);
        assertNotNull(result);
    }

    @Test
    /**
     * testPlusMinutes方法。
     */
    public void testPlusMinutes() {
        Date date = new Date(0);
        Date result = DateUtil.plusMinutes(date, 1);
        assertNotNull(result);
    }

    @Test
    /**
     * testMinusMinutes方法。
     */
    public void testMinusMinutes() {
        Date date = new Date(0);
        Date result = DateUtil.minusMinutes(date, 1);
        assertNotNull(result);
    }

    @Test
    /**
     * testPlusSeconds方法。
     */
    public void testPlusSeconds() {
        Date date = new Date(0);
        Date result = DateUtil.plusSeconds(date, 1);
        assertNotNull(result);
    }

    @Test
    /**
     * testMinusSeconds方法。
     */
    public void testMinusSeconds() {
        Date date = new Date(0);
        Date result = DateUtil.minusSeconds(date, 1);
        assertNotNull(result);
    }

    // ==================== 日期比较测试 ====================

    @Test
    /**
     * testIsBefore方法。
     */
    public void testIsBefore() {
        Date date1 = new Date(0);
        Date date2 = new Date(1);
        assertTrue(DateUtil.isBefore(date1, date2));
        assertFalse(DateUtil.isBefore(date2, date1));
    }

    @Test
    /**
     * testIsAfter方法。
     */
    public void testIsAfter() {
        Date date1 = new Date(0);
        Date date2 = new Date(1);
        assertTrue(DateUtil.isAfter(date2, date1));
        assertFalse(DateUtil.isAfter(date1, date2));
    }

    @Test
    /**
     * testIsSameDay方法。
     */
    public void testIsSameDay() {
        Date date1 = new Date();
        Date date2 = new Date();
        assertTrue(DateUtil.isSameDay(date1, date2));
    }

    @Test
    /**
     * testIsSameInstant方法。
     */
    public void testIsSameInstant() {
        Date date1 = new Date(0);
        Date date2 = new Date(0);
        assertTrue(DateUtil.isSameInstant(date1, date2));
    }

    // ==================== 时间差测试 ====================

    @Test
    /**
     * testDaysBetween方法。
     */
    public void testDaysBetween() {
        Date start = new Date(0);
        Date end = new Date(java.time.Duration.ofDays(5).toMillis());
        long days = DateUtil.daysBetween(start, end);
        assertEquals(5, days);
    }

    @Test
    /**
     * testHoursBetween方法。
     */
    public void testHoursBetween() {
        Date start = new Date(0);
        Date end = new Date(java.time.Duration.ofHours(5).toMillis());
        long hours = DateUtil.hoursBetween(start, end);
        assertEquals(5, hours);
    }

    @Test
    /**
     * testMinutesBetween方法。
     */
    public void testMinutesBetween() {
        Date start = new Date(0);
        Date end = new Date(java.time.Duration.ofMinutes(5).toMillis());
        long minutes = DateUtil.minutesBetween(start, end);
        assertEquals(5, minutes);
    }

    @Test
    /**
     * testSecondsBetween方法。
     */
    public void testSecondsBetween() {
        Date start = new Date(0);
        Date end = new Date(5000);
        long seconds = DateUtil.secondsBetween(start, end);
        assertEquals(5, seconds);
    }

    @Test
    /**
     * testMillisBetween方法。
     */
    public void testMillisBetween() {
        Date start = new Date(0);
        Date end = new Date(500);
        long millis = DateUtil.millisBetween(start, end);
        assertEquals(500, millis);
    }

    // ==================== 类型转换测试 ====================

    @Test
    /**
     * testToLocalDateTime方法。
     */
    public void testToLocalDateTime() {
        Date date = new Date();
        LocalDateTime ldt = DateUtil.toLocalDateTime(date);
        assertNotNull(ldt);
    }

    @Test
    /**
     * testToLocalDate方法。
     */
    public void testToLocalDate() {
        Date date = new Date();
        java.time.LocalDate ld = DateUtil.toLocalDate(date);
        assertNotNull(ld);
    }

    @Test
    /**
     * testToLocalTime方法。
     */
    public void testToLocalTime() {
        Date date = new Date();
        LocalTime lt = DateUtil.toLocalTime(date);
        assertNotNull(lt);
    }

    @Test
    /**
     * testToInstant方法。
     */
    public void testToInstant() {
        Date date = new Date();
        java.time.Instant instant = DateUtil.toInstant(date);
        assertNotNull(instant);
    }

    @Test
    /**
     * testToEpochMilli方法。
     */
    public void testToEpochMilli() {
        Date date = new Date();
        long milli = DateUtil.toEpochMilli(date);
        assertEquals(date.getTime(), milli);
    }

    @Test
    /**
     * testFromLocalDateTime方法。
     */
    public void testFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.now();
        Date date = DateUtil.fromLocalDateTime(ldt);
        assertNotNull(date);
    }

    @Test
    /**
     * testFromLocalDate方法。
     */
    public void testFromLocalDate() {
        java.time.LocalDate ld = java.time.LocalDate.now();
        Date date = DateUtil.fromLocalDate(ld);
        assertNotNull(date);
    }

    @Test
    /**
     * testFromEpochMilli方法。
     */
    public void testFromEpochMilli() {
        Date date = DateUtil.fromEpochMilli(0);
        assertNotNull(date);
        assertEquals(0, date.getTime());
    }

    @Test
    /**
     * testFromInstant方法。
     */
    public void testFromInstant() {
        java.time.Instant instant = java.time.Instant.now();
        Date date = DateUtil.fromInstant(instant);
        assertNotNull(date);
    }

    // ==================== Java 8 Time API 测试 ====================

    @Test
    /**
     * testTimestampToLocalDateTime方法。
     */
    public void testTimestampToLocalDateTime() {
        LocalDateTime ldt = DateUtil.timestampToLocalDateTime(0L);
        assertNotNull(ldt);
    }

    @Test
    /**
     * testTimestampToLocalDateTime_withOffset方法。
     */
    public void testTimestampToLocalDateTime_withOffset() {
        LocalDateTime ldt = DateUtil.timestampToLocalDateTime(0L, 8);
        assertNotNull(ldt);
    }

    @Test
    /**
     * testLocalDateTimeToTimestamp方法。
     */
    public void testLocalDateTimeToTimestamp() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        Long timestamp = DateUtil.localDateTimeToTimestamp(ldt);
        assertNotNull(timestamp);
        assertTrue(timestamp > 0);
    }

    @Test
    /**
     * testLocalDateTimeToMilliTimestamp方法。
     */
    public void testLocalDateTimeToMilliTimestamp() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        Long milli = DateUtil.localDateTimeToMilliTimestamp(ldt);
        assertNotNull(milli);
        assertTrue(milli > 0);
    }

    @Test
    /**
     * testParseLocalDateTime方法。
     */
    public void testParseLocalDateTime() {
        LocalDateTime ldt = DateUtil.parseLocalDateTime("2023-01-01 12:00:00");
        assertNotNull(ldt);
        assertEquals(2023, ldt.getYear());
        assertEquals(1, ldt.getMonthValue());
        assertEquals(1, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHour());
    }

    @Test
    /**
     * testParseLocalDateTime_withMicroseconds方法。
     */
    public void testParseLocalDateTime_withMicroseconds() {
        LocalDateTime ldt = DateUtil.parseLocalDateTime("2023-01-01 12:00:00.123456");
        assertNotNull(ldt);
    }

    @Test
    /**
     * testParseLocalTime方法。
     */
    public void testParseLocalTime() {
        LocalTime lt = DateUtil.parseLocalTime("12:30:45");
        assertNotNull(lt);
        assertEquals(12, lt.getHour());
        assertEquals(30, lt.getMinute());
        assertEquals(45, lt.getSecond());
    }

    @Test
    /**
     * testParseLocalTime_withMicroseconds方法。
     */
    public void testParseLocalTime_withMicroseconds() {
        LocalTime lt = DateUtil.parseLocalTime("12:30:45.123456");
        assertNotNull(lt);
    }

    @Test
    /**
     * testGetMicrosecond方法。
     */
    public void testGetMicrosecond() {
        long result = DateUtil.getMicrosecond("2023-01-01 12:00:00");
        assertTrue(result > 0);
    }

    @Test
    /**
     * testGetMicrosecond_withMicroseconds方法。
     */
    public void testGetMicrosecond_withMicroseconds() {
        long result = DateUtil.getMicrosecond("2023-01-01 12:00:00.123456");
        assertTrue(result > 0);
    }

    @Test
    /**
     * testParseLocalDateTime_withNull方法。
     */
    public void testParseLocalDateTime_withNull() {
        assertNull(DateUtil.parseLocalDateTime(null));
        assertNull(DateUtil.parseLocalDateTime(""));
        assertNull(DateUtil.parseLocalDateTime("  "));
    }

    @Test
    /**
     * testParseLocalTime_withNull方法。
     */
    public void testParseLocalTime_withNull() {
        assertNull(DateUtil.parseLocalTime(null));
        assertNull(DateUtil.parseLocalTime(""));
        assertNull(DateUtil.parseLocalTime("  "));
    }
}