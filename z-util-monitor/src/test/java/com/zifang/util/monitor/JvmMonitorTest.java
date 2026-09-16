package com.zifang.util.monitor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * JvmMonitorTest类。
 */
public class JvmMonitorTest {

    @Test
    /**
     * testJvmMonitorExists方法。
     */
    public void testJvmMonitorExists() {
        com.zifang.util.monitor.jvm.JvmMonitor monitor = new com.zifang.util.monitor.jvm.JvmMonitor();
        assertNotNull(monitor);
    }
}
