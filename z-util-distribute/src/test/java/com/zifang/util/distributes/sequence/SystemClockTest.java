package com.zifang.util.distributes.sequence;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SystemClockTest类。
 */
public class SystemClockTest {

    @Test
    /**
     * testNow_ReturnsPositiveValue方法。
     */
    public void testNow_ReturnsPositiveValue() {
        long now = SystemClock.now();
        assertTrue("now() should return a positive value", now > 0);
    }

    @Test
    /**
     * testNow_ReturnsReasonableTimestamp方法。
     */
    public void testNow_ReturnsReasonableTimestamp() {
        long now = SystemClock.now();
        long expectedMin = 1609459200000L; // 2021-01-01 00:00:00
        assertTrue("now() should return a timestamp after 2021", now >= expectedMin);
    }

    @Test
    /**
     * testNow_CalledMultipleTimes_ReturnsIncreasingOrSame方法。
     */
    public void testNow_CalledMultipleTimes_ReturnsIncreasingOrSame() throws InterruptedException {
        long now1 = SystemClock.now();
        Thread.sleep(1);
        long now2 = SystemClock.now();
        assertTrue("Later call should return greater or equal timestamp", now2 >= now1);
    }

    @Test
    /**
     * testNowDate_ReturnsValidString方法。
     */
    public void testNowDate_ReturnsValidString() {
        String date = SystemClock.nowDate();
        assertNotNull("nowDate() should not return null", date);
        assertFalse("nowDate() should not be empty", date.isEmpty());
        assertTrue("nowDate() should contain timestamp pattern", date.contains(".") || date.matches(".*\\d+.*"));
    }

    @Test
    /**
     * testNow_SameAsSystemCurrentTimeMillis方法。
     */
    public void testNow_SameAsSystemCurrentTimeMillis() {
        long clockNow = SystemClock.now();
        long systemNow = System.currentTimeMillis();
        long diff = Math.abs(clockNow - systemNow);
        // Allow some tolerance due to execution time difference
        assertTrue("SystemClock.now() should be close to System.currentTimeMillis()", diff < 1000);
    }

    @Test
    /**
     * testNow_Consistency方法。
     */
    public void testNow_Consistency() {
        // Call multiple times to ensure consistency
        for (int i = 0; i < 100; i++) {
            long now = SystemClock.now();
            assertTrue("Timestamp should always be positive", now > 0);
        }
    }
}
