package com.zifang.util.monitor.thread;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ThreadPoolStatusTest类。
 */
public class ThreadPoolStatusTest {

    private ThreadPoolStatus status;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        status = new ThreadPoolStatus();
    }

    @Test
    /**
     * testDefaultValues方法。
     */
    public void testDefaultValues() {
        assertNotNull(status.getStartTime());
        assertTrue(status.getStartTime() > 0);
        assertNotNull(status.getSubmitCount());
        assertNotNull(status.getStartCount());
        assertNotNull(status.getSucessCount());
        assertNotNull(status.getFailCount());
        assertNotNull(status.getLastStartTime());
        assertNotNull(status.getLastFinishTime());
        assertNotNull(status.getTotalTimeConsuming());
        assertNotNull(status.getAlarmTimes());
        assertNotNull(status.getThreadStatusMap());
        assertNotNull(status.getTaskStatusMap());
        assertEquals(10000, status.getMonitorInterval());
    }

    @Test
    /**
     * testSubmitCountIncrement方法。
     */
    public void testSubmitCountIncrement() {
        int before = status.getSubmitCount().get();
        status.getSubmitCount().incrementAndGet();
        assertEquals(before + 1, status.getSubmitCount().get());
    }

    @Test
    /**
     * testStartCountIncrement方法。
     */
    public void testStartCountIncrement() {
        int before = status.getStartCount().get();
        status.getStartCount().incrementAndGet();
        assertEquals(before + 1, status.getStartCount().get());
    }

    @Test
    /**
     * testSucessCountIncrement方法。
     */
    public void testSucessCountIncrement() {
        int before = status.getSucessCount().get();
        status.getSucessCount().incrementAndGet();
        assertEquals(before + 1, status.getSucessCount().get());
    }

    @Test
    /**
     * testFailCountIncrement方法。
     */
    public void testFailCountIncrement() {
        int before = status.getFailCount().get();
        status.getFailCount().incrementAndGet();
        assertEquals(before + 1, status.getFailCount().get());
    }

    @Test
    /**
     * testLastStartTimeUpdate方法。
     */
    public void testLastStartTimeUpdate() {
        long before = status.getLastStartTime().get();
        status.getLastStartTime().set(System.currentTimeMillis());
        assertTrue(status.getLastStartTime().get() >= before);
    }

    @Test
    /**
     * testLastFinishTimeUpdate方法。
     */
    public void testLastFinishTimeUpdate() {
        long before = status.getLastFinishTime().get();
        status.getLastFinishTime().set(System.currentTimeMillis());
        assertTrue(status.getLastFinishTime().get() >= before);
    }

    @Test
    /**
     * testTotalTimeConsumingUpdate方法。
     */
    public void testTotalTimeConsumingUpdate() {
        status.getTotalTimeConsuming().addAndGet(100);
        assertEquals(100, status.getTotalTimeConsuming().get());
    }

    @Test
    /**
     * testAlarmTimesIncrement方法。
     */
    public void testAlarmTimesIncrement() {
        int before = status.getAlarmTimes().get();
        status.getAlarmTimes().incrementAndGet();
        assertEquals(before + 1, status.getAlarmTimes().get());
    }

    @Test
    /**
     * testMonitorIntervalSetter方法。
     */
    public void testMonitorIntervalSetter() {
        status.setMonitorInterval(20000);
        assertEquals(20000, status.getMonitorInterval());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        String result = status.toString();
        assertNotNull(result);
        assertTrue(result.contains("ThreadPoolStatus"));
    }
}
