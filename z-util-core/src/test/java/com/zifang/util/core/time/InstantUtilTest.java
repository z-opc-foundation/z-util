package com.zifang.util.core.time;

import org.junit.Test;

import java.time.*;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * InstantUtilTest类。
 */
public class InstantUtilTest {

    @Test
    /**
     * testFormat方法。
     */
    public void testFormat() {
        Instant instant = Instant.parse("2024-06-15T10:30:00Z");
        String result = InstantUtil.format(instant);
        assertNotNull(result);
        assertTrue(result.contains("2024"));
        assertTrue(result.contains("06"));
        assertTrue(result.contains("15"));
    }

    @Test
    /**
     * testFormatWithPattern方法。
     */
    public void testFormatWithPattern() {
        Instant instant = Instant.parse("2024-06-15T10:30:00Z");
        String result = InstantUtil.format(instant, "yyyy-MM-dd");
        assertEquals("2024-06-15", result);
    }

    @Test
    /**
     * testParse方法。
     */
    public void testParse() {
        Instant instant = InstantUtil.parse("2024-06-15 10:30:00", "yyyy-MM-dd HH:mm:ss");
        assertNotNull(instant);
    }

    @Test
    /**
     * testParseNull方法。
     */
    public void testParseNull() {
        assertNull(InstantUtil.parse(null));
        assertNull(InstantUtil.parse(""));
    }

    @Test
    /**
     * testNow方法。
     */
    public void testNow() {
        Instant now = InstantUtil.now();
        assertNotNull(now);
        assertTrue(now.compareTo(Instant.EPOCH) > 0);
    }

    @Test
    /**
     * testPlusMinus方法。
     */
    public void testPlusMinus() {
        Instant base = Instant.parse("2024-06-15T10:00:00Z");
        Instant plus = InstantUtil.plusSeconds(base, 60);
        Instant minus = InstantUtil.minusSeconds(base, 60);
        assertEquals(base.plusSeconds(60), plus);
        assertEquals(base.minusSeconds(60), minus);
    }

    @Test
    /**
     * testMillisBetween方法。
     */
    public void testMillisBetween() {
        Instant start = Instant.parse("2024-06-15T10:00:00Z");
        Instant end = Instant.parse("2024-06-15T10:01:00Z");
        assertEquals(60000, InstantUtil.millisBetween(start, end));
        assertEquals(60, InstantUtil.secondsBetween(start, end));
    }

    @Test
    /**
     * testCompare方法。
     */
    public void testCompare() {
        Instant earlier = Instant.parse("2024-06-15T10:00:00Z");
        Instant later = Instant.parse("2024-06-15T11:00:00Z");
        assertTrue(InstantUtil.isBefore(earlier, later));
        assertTrue(InstantUtil.isAfter(later, earlier));
        assertFalse(InstantUtil.isBefore(later, earlier));
    }

    @Test
    /**
     * testToDate方法。
     */
    public void testToDate() {
        Instant instant = Instant.parse("2024-06-15T10:00:00Z");
        Date date = InstantUtil.toDate(instant);
        assertNotNull(date);
        assertEquals(instant.toEpochMilli(), date.getTime());
    }

    @Test
    /**
     * testToLocalDateTime方法。
     */
    public void testToLocalDateTime() {
        Instant instant = Instant.parse("2024-06-15T10:00:00Z");
        LocalDateTime ldt = InstantUtil.toLocalDateTime(instant);
        assertNotNull(ldt);
        assertEquals(2024, ldt.getYear());
        assertEquals(6, ldt.getMonthValue());
        assertEquals(15, ldt.getDayOfMonth());
    }

    @Test
    /**
     * testToLocalDate方法。
     */
    public void testToLocalDate() {
        Instant instant = Instant.parse("2024-06-15T10:00:00Z");
        LocalDate ld = InstantUtil.toLocalDate(instant);
        assertNotNull(ld);
        assertEquals(2024, ld.getYear());
        assertEquals(6, ld.getMonthValue());
        assertEquals(15, ld.getDayOfMonth());
    }

    @Test
    /**
     * testToZonedDateTime方法。
     */
    public void testToZonedDateTime() {
        Instant instant = Instant.parse("2024-06-15T10:00:00Z");
        ZonedDateTime zdt = InstantUtil.toZonedDateTime(instant);
        assertNotNull(zdt);
        assertEquals(instant, zdt.toInstant());
    }

    @Test
    /**
     * testFromDate方法。
     */
    public void testFromDate() {
        Date date = new Date(1718455200000L);
        Instant instant = InstantUtil.fromDate(date);
        assertNotNull(instant);
        assertEquals(date.getTime(), instant.toEpochMilli());
    }

    @Test
    /**
     * testFromLocalDateTime方法。
     */
    public void testFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        Instant instant = InstantUtil.fromLocalDateTime(ldt);
        assertNotNull(instant);
        assertEquals(ldt.atZone(ZoneId.systemDefault()).toInstant(), instant);
    }

    @Test
    /**
     * testFromEpochMilli方法。
     */
    public void testFromEpochMilli() {
        Instant instant = InstantUtil.fromEpochMilli(1718455200000L);
        assertNotNull(instant);
        assertEquals(1718455200000L, instant.toEpochMilli());
    }

    @Test
    /**
     * testNullHandling方法。
     */
    public void testNullHandling() {
        assertNull(InstantUtil.format(null));
        assertNull(InstantUtil.parse(null, "yyyy-MM-dd"));
        assertEquals(0, InstantUtil.toEpochMilli(null));
    }
}
