package com.zifang.util.monitor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * OsMonitorTest类。
 */
public class OsMonitorTest {

    @Test
    /**
     * testOsMonitorExists方法。
     */
    public void testOsMonitorExists() {
        com.zifang.util.monitor.os.OsMonitor monitor = new com.zifang.util.monitor.os.OsMonitor();
        assertNotNull(monitor);
    }
}
