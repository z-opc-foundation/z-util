package com.zifang.util.monitor.thread.utility;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * DateUtilsTest类。
 */
public class DateUtilsTest {

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals("yyyy-MM-dd", DateUtils.DEFAULT_DATE_FORMAT);
        assertEquals("yyyy-MM-dd HH:mm:ss", DateUtils.DEFAULT_DATE_TIME_FORMAT);
        assertEquals(86400000L, DateUtils.dayTotalMilliseconds);
        assertEquals(86400, DateUtils.dayTotalSeconds);
    }

    @Test
    /**
     * testGetDateString方法。
     */
    public void testGetDateString() {
        String dateStr = DateUtils.getDateString(System.currentTimeMillis());
        assertNotNull(dateStr);
        assertTrue(dateStr.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    /**
     * testGetDateStringNoArg方法。
     */
    public void testGetDateStringNoArg() {
        String dateStr = DateUtils.getDateString();
        assertNotNull(dateStr);
        assertTrue(dateStr.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    /**
     * testGetDateAndTimeString方法。
     */
    public void testGetDateAndTimeString() {
        String dateTimeStr = DateUtils.getDateAndTimeString();
        assertNotNull(dateTimeStr);
        assertTrue(dateTimeStr.contains("-"));
    }

    @Test
    /**
     * testGetNextDateString方法。
     */
    public void testGetNextDateString() {
        String today = DateUtils.getDateString();
        String nextDay = DateUtils.getNextDateString(System.currentTimeMillis());
        assertNotNull(nextDay);
        assertNotEquals(today, nextDay);
    }

    @Test
    /**
     * testGetDateTimeString方法。
     */
    public void testGetDateTimeString() {
        long timestamp = System.currentTimeMillis();
        String result = DateUtils.getDateTimeString(timestamp, "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    /**
     * testDateToStr方法。
     */
    public void testDateToStr() {
        Date date = new Date();
        String result = DateUtils.dateToStr(date, "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    /**
     * testStrToDate方法。
     */
    public void testStrToDate() {
        Date date = DateUtils.strToDate("2023-01-01", "yyyy-MM-dd");
        assertNotNull(date);
    }

    @Test
    /**
     * testStrToDateMillisTimestamp方法。
     */
    public void testStrToDateMillisTimestamp() {
        long millis = DateUtils.strToDateMillisTimestamp("2023-01-01", "yyyy-MM-dd");
        assertTrue(millis > 0);
    }

    @Test
    /**
     * testStrToDateTimestamp方法。
     */
    public void testStrToDateTimestamp() {
        int timestamp = DateUtils.strToDateTimestamp("2023-01-01", "yyyy-MM-dd");
        assertTrue(timestamp > 0);
    }

    @Test
    /**
     * testCalcDay方法。
     */
    public void testCalcDay() throws Exception {
        long days = DateUtils.calcDay("2023-01-02", "2023-01-01", "yyyy-MM-dd");
        assertEquals(1, days);
    }

    @Test
    /**
     * testCompareDate方法。
     */
    public void testCompareDate() {
        Date date1 = new Date(1000);
        Date date2 = new Date(2000);
        assertEquals(-1, DateUtils.compareDate(date1, date2));
        assertEquals(1, DateUtils.compareDate(date2, date1));
        assertEquals(0, DateUtils.compareDate(date1, date1));
    }

    @Test
    /**
     * testCompareDateString方法。
     */
    public void testCompareDateString() {
        int result = DateUtils.compareDate("2023-01-01", "2023-01-02", "yyyy-MM-dd");
        assertTrue(result < 0);
    }
}
