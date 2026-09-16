package com.zifang.util.monitor.thread.alarm;

import com.zifang.util.monitor.thread.StatusLevel;
import com.zifang.util.monitor.thread.ThreadPoolStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * ThreadPoolOvertimeAlarmPolicyTest类。
 */
public class ThreadPoolOvertimeAlarmPolicyTest {

    private ThreadPoolOvertimeAlarmPolicy policy;
    private ThreadPoolStatus status;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        policy = new ThreadPoolOvertimeAlarmPolicy();
        status = new ThreadPoolStatus();
        status.setLevel(StatusLevel.OK);
    }

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        ThreadPoolOvertimeAlarmPolicy p = new ThreadPoolOvertimeAlarmPolicy();
        assertNotNull(p);
    }

    @Test
    /**
     * testConstructorWithThreshold方法。
     */
    public void testConstructorWithThreshold() {
        int customThreshold = 60000;
        ThreadPoolOvertimeAlarmPolicy p = new ThreadPoolOvertimeAlarmPolicy(customThreshold);
        assertNotNull(p);
    }

    @Test
    /**
     * testNoAlarmWhenLastFinishTimeAfterLastStartTime方法。
     */
    public void testNoAlarmWhenLastFinishTimeAfterLastStartTime() {
        // Task finished, no alarm needed
        status.getLastStartTime().set(System.currentTimeMillis() - 100000);
        status.getLastFinishTime().set(System.currentTimeMillis());
        assertFalse(policy.needAlarm(status));
    }

    @Test
    /**
     * testNoAlarmWhenTaskRunningButWithinThreshold方法。
     */
    public void testNoAlarmWhenTaskRunningButWithinThreshold() {
        // Task still running but within threshold
        status.getLastStartTime().set(System.currentTimeMillis() - 1000);
        status.getLastFinishTime().set(0); // Not finished
        assertFalse(policy.needAlarm(status));
    }
}
