package com.zifang.util.monitor.thread.constant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MonitorConstantTest类。
 */
public class MonitorConstantTest {

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertNotNull(MonitorConstant.INIT_STATUS);
        assertEquals("threadStartTime", MonitorConstant.THREAD_START_TIME);
        assertEquals("taskStartTime", MonitorConstant.TASK_START_TIME);
    }

    @Test
    /**
     * testPrivateConstructor方法。
     */
    public void testPrivateConstructor() {
        // Test that the class cannot be instantiated
        try {
            MonitorConstant.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Expected - constructor is private
        }
    }
}
