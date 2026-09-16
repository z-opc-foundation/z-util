package com.zifang.util.monitor.thread.executor;

import com.zifang.util.monitor.thread.alarm.AlarmPolicy;
import com.zifang.util.monitor.thread.alarm.AlarmService;
import com.zifang.util.monitor.thread.alarm.LogAlarmService;
import com.zifang.util.monitor.thread.alarm.ThreadPoolOvertimeAlarmPolicy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ThreadPoolConfigUnitTest类。
 */
public class ThreadPoolConfigUnitTest {

    private ThreadPoolConfigUnit configUnit;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        configUnit = new ThreadPoolConfigUnit();
    }

    @Test
    /**
     * testDefaultValues方法。
     */
    public void testDefaultValues() {
        assertEquals(180000, configUnit.getThreadOvertimeThreshhold());
        assertEquals(10000, configUnit.getMonitorInterval());
    }

    @Test
    /**
     * testPoolName方法。
     */
    public void testPoolName() {
        configUnit.setPoolName("TestPool");
        assertEquals("TestPool", configUnit.getPoolName());
    }

    @Test
    /**
     * testPoolSize方法。
     */
    public void testPoolSize() {
        configUnit.setPoolSize(10);
        assertEquals(10, configUnit.getPoolSize());
    }

    @Test
    /**
     * testComputeType方法。
     */
    public void testComputeType() {
        configUnit.setComputeType((byte) 1);
        assertEquals(1, configUnit.getComputeType());
    }

    @Test
    /**
     * testTaskType方法。
     */
    public void testTaskType() {
        configUnit.setTaskType(String.class);
        assertEquals(String.class, configUnit.getTaskType());
    }

    @Test
    /**
     * testThreadOvertimeThreshhold方法。
     */
    public void testThreadOvertimeThreshhold() {
        configUnit.setThreadOvertimeThreshhold(60000);
        assertEquals(60000, configUnit.getThreadOvertimeThreshhold());
    }

    @Test
    /**
     * testAlarmPolicy方法。
     */
    public void testAlarmPolicy() {
        AlarmPolicy policy = new ThreadPoolOvertimeAlarmPolicy();
        configUnit.setAlarmPolicy(policy);
        assertEquals(policy, configUnit.getAlarmPolicy());
    }

    @Test
    /**
     * testAlarmService方法。
     */
    public void testAlarmService() {
        AlarmService service = new LogAlarmService();
        configUnit.setAlarmService(service);
        assertEquals(service, configUnit.getAlarmService());
    }

    @Test
    /**
     * testMonitorInterval方法。
     */
    public void testMonitorInterval() {
        configUnit.setMonitorInterval(20000);
        assertEquals(20000, configUnit.getMonitorInterval());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        configUnit.setPoolName("TestPool");
        configUnit.setPoolSize(5);
        String result = configUnit.toString();
        assertTrue(result.contains("TestPool"));
        assertTrue(result.contains("5"));
    }
}
