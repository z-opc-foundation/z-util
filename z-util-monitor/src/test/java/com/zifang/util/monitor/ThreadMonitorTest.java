package com.zifang.util.monitor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * ThreadMonitorTest类。
 */
public class ThreadMonitorTest {

    @Test
    /**
     * testThreadMonitorExists方法。
     */
    public void testThreadMonitorExists() {
        com.zifang.util.monitor.thread.ThreadMonitor monitor = new com.zifang.util.monitor.thread.ThreadMonitor();
        assertNotNull(monitor);
    }
}
