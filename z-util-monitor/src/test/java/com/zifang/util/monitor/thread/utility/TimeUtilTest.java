package com.zifang.util.monitor.thread.utility;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TimeUtilTest类。
 */
public class TimeUtilTest {

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals(24, TimeUtil.DAY_HOURS);
        assertEquals(60, TimeUtil.HOUR_MINUTES);
        assertEquals(60, TimeUtil.MINUTES_SECONDS);
        assertEquals(1000, TimeUtil.SECOND_MILLISECONDS);
        assertEquals(86400000L, TimeUtil.DAY_MILLISECONDS);
        assertEquals(86400, TimeUtil.DAY_SECONDS);
    }

    @Test
    /**
     * testGetTimestamp方法。
     */
    public void testGetTimestamp() {
        int timestamp = TimeUtil.getTimestamp();
        assertTrue(timestamp > 0);
    }

    @Test
    /**
     * testGetMillisTimestamp方法。
     */
    public void testGetMillisTimestamp() {
        long millis = TimeUtil.getMillisTimestamp();
        assertTrue(millis > 0);
        assertEquals(System.currentTimeMillis(), millis, 1000);
    }

    @Test
    /**
     * testGetMillisTimestampFromInt方法。
     */
    public void testGetMillisTimestampFromInt() {
        int timestamp = 1000;
        long millis = TimeUtil.getMillisTimestamp(timestamp);
        assertEquals(1000000L, millis);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetMillisTimestampNegative方法。
     */
    public void testGetMillisTimestampNegative() {
        TimeUtil.getMillisTimestamp(-1);
    }

    @Test
    /**
     * testGetTimestampFromMillis方法。
     */
    public void testGetTimestampFromMillis() {
        long millis = 1000000L;
        int timestamp = TimeUtil.getTimestamp(millis);
        assertEquals(1000, timestamp);
    }

    @Test
    /**
     * testGetHourMillisTimestamp方法。
     */
    public void testGetHourMillisTimestamp() {
        assertEquals(0L, TimeUtil.getHourMillisTimestamp(0));
        assertEquals(3600000L, TimeUtil.getHourMillisTimestamp(1));
        assertEquals(86400000L, TimeUtil.getHourMillisTimestamp(24));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetHourMillisTimestampNegative方法。
     */
    public void testGetHourMillisTimestampNegative() {
        TimeUtil.getHourMillisTimestamp(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetHourMillisTimestampOutOfRange方法。
     */
    public void testGetHourMillisTimestampOutOfRange() {
        TimeUtil.getHourMillisTimestamp(25);
    }

    @Test
    /**
     * testHourageEnumValues方法。
     */
    public void testHourageEnumValues() {
        TimeUtil.Hourage[] values = TimeUtil.Hourage.values();
        assertEquals(5, values.length);
    }

    @Test
    /**
     * testHourageEnumGetHour方法。
     */
    public void testHourageEnumGetHour() {
        assertEquals(1, TimeUtil.Hourage.ONE.getHour());
        assertEquals(2, TimeUtil.Hourage.TWO.getHour());
        assertEquals(3, TimeUtil.Hourage.THREE.getHour());
        assertEquals(15, TimeUtil.Hourage.FIFTEEN.getHour());
        assertEquals(23, TimeUtil.Hourage.TWENTY_THREE.getHour());
    }

    @Test
    /**
     * testHourageGetMillisTimestamp方法。
     */
    public void testHourageGetMillisTimestamp() {
        assertEquals(TimeUtil.getHourMillisTimestamp(1), TimeUtil.Hourage.ONE.getHourageMillisTimestamp());
        assertEquals(TimeUtil.getHourMillisTimestamp(23), TimeUtil.Hourage.TWENTY_THREE.getHourageMillisTimestamp());
    }
}
