package com.zifang.util.core.time;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * TimestampUtilTest类。
 */
public class TimestampUtilTest {

    @Test
    /**
     * testFormat方法。
     */
    public void testFormat() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        String result = TimestampUtil.format(ts);
        assertNotNull(result);
        assertTrue(result.contains("2024"));
    }

    @Test
    /**
     * testFormatWithPattern方法。
     */
    public void testFormatWithPattern() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        String result = TimestampUtil.format(ts, "yyyy-MM-dd");
        assertEquals("2024-06-15", result);
    }

    @Test
    /**
     * testFormatNull方法。
     */
    public void testFormatNull() {
        assertNull(TimestampUtil.format(null));
        assertNull(TimestampUtil.format(null, "yyyy-MM-dd"));
    }

    @Test
    /**
     * testParse方法。
     */
    public void testParse() {
        Timestamp ts = TimestampUtil.parse("2024-06-15 10:30:00");
        assertNotNull(ts);
        assertEquals(2024, ts.toLocalDateTime().getYear());
        assertEquals(6, ts.toLocalDateTime().getMonthValue());
        assertEquals(15, ts.toLocalDateTime().getDayOfMonth());
    }

    @Test
    /**
     * testParseNull方法。
     */
    public void testParseNull() {
        assertNull(TimestampUtil.parse(null));
        assertNull(TimestampUtil.parse(""));
    }

    @Test
    /**
     * testNow方法。
     */
    public void testNow() {
        Timestamp now = TimestampUtil.now();
        assertNotNull(now);
        assertTrue(now.getTime() > 0);
    }

    @Test
    /**
     * testOfLong方法。
     */
    public void testOfLong() {
        Timestamp ts = TimestampUtil.of(1718455200000L);
        assertNotNull(ts);
        assertEquals(1718455200000L, ts.getTime());
    }

    @Test
    /**
     * testOfLocalDateTime方法。
     */
    public void testOfLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        Timestamp ts = TimestampUtil.of(ldt);
        assertNotNull(ts);
        assertEquals(ldt, ts.toLocalDateTime());
    }

    @Test
    /**
     * testPlusMinus方法。
     */
    public void testPlusMinus() {
        Timestamp base = Timestamp.valueOf("2024-06-15 10:00:00");
        Timestamp plus = TimestampUtil.plusSeconds(base, 60);
        Timestamp minus = TimestampUtil.minusSeconds(base, 60);
        assertEquals(base.getTime() + 60000, plus.getTime());
        assertEquals(base.getTime() - 60000, minus.getTime());
    }

    @Test
    /**
     * testCompare方法。
     */
    public void testCompare() {
        Timestamp earlier = Timestamp.valueOf("2024-06-15 10:00:00");
        Timestamp later = Timestamp.valueOf("2024-06-15 11:00:00");
        assertTrue(TimestampUtil.isBefore(earlier, later));
        assertTrue(TimestampUtil.isAfter(later, earlier));
    }

    @Test
    /**
     * testToDate方法。
     */
    public void testToDate() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        Date date = TimestampUtil.toDate(ts);
        assertNotNull(date);
        assertEquals(ts.getTime(), date.getTime());
    }

    @Test
    /**
     * testToLocalDateTime方法。
     */
    public void testToLocalDateTime() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        LocalDateTime ldt = TimestampUtil.toLocalDateTime(ts);
        assertNotNull(ldt);
        assertEquals(2024, ldt.getYear());
        assertEquals(6, ldt.getMonthValue());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHour());
        assertEquals(30, ldt.getMinute());
    }

    @Test
    /**
     * testToLocalDate方法。
     */
    public void testToLocalDate() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        LocalDate ld = TimestampUtil.toLocalDate(ts);
        assertNotNull(ld);
        assertEquals(2024, ld.getYear());
        assertEquals(6, ld.getMonthValue());
        assertEquals(15, ld.getDayOfMonth());
    }

    @Test
    /**
     * testToInstant方法。
     */
    public void testToInstant() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        Instant instant = TimestampUtil.toInstant(ts);
        assertNotNull(instant);
        assertEquals(ts.toInstant(), instant);
    }

    @Test
    /**
     * testToEpochMilli方法。
     */
    public void testToEpochMilli() {
        Timestamp ts = Timestamp.valueOf("2024-06-15 10:30:00");
        long epochMilli = TimestampUtil.toEpochMilli(ts);
        assertEquals(ts.getTime(), epochMilli);
    }

    @Test
    /**
     * testFromDate方法。
     */
    public void testFromDate() {
        Date date = new Date(1718455200000L);
        Timestamp ts = TimestampUtil.fromDate(date);
        assertNotNull(ts);
        assertEquals(date.getTime(), ts.getTime());
    }

    @Test
    /**
     * testFromLocalDateTime方法。
     */
    public void testFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        Timestamp ts = TimestampUtil.fromLocalDateTime(ldt);
        assertNotNull(ts);
        assertEquals(ldt, ts.toLocalDateTime());
    }

    @Test
    /**
     * testFromLocalDate方法。
     */
    public void testFromLocalDate() {
        LocalDate ld = LocalDate.of(2024, 6, 15);
        Timestamp ts = TimestampUtil.fromLocalDate(ld);
        assertNotNull(ts);
        assertEquals(ld, ts.toLocalDateTime().toLocalDate());
    }

    @Test
    /**
     * testFromInstant方法。
     */
    public void testFromInstant() {
        Instant instant = Instant.parse("2024-06-15T10:00:00Z");
        Timestamp ts = TimestampUtil.fromInstant(instant);
        assertNotNull(ts);
        assertEquals(instant, ts.toInstant());
    }

    @Test
    /**
     * testFromEpochMilli方法。
     */
    public void testFromEpochMilli() {
        Timestamp ts = TimestampUtil.fromEpochMilli(1718455200000L);
        assertNotNull(ts);
        assertEquals(1718455200000L, ts.getTime());
    }

    @Test
    /**
     * testNullHandling方法。
     */
    public void testNullHandling() {
        assertNull(TimestampUtil.toDate(null));
        assertNull(TimestampUtil.toLocalDateTime(null));
        assertNull(TimestampUtil.toLocalDate(null));
        assertNull(TimestampUtil.toInstant(null));
        assertEquals(0, TimestampUtil.toEpochMilli(null));
        assertNull(TimestampUtil.fromDate(null));
        assertNull(TimestampUtil.fromLocalDateTime(null));
        assertNull(TimestampUtil.fromLocalDate(null));
        assertNull(TimestampUtil.fromInstant(null));
    }
}
