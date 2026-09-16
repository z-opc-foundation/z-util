package com.zifang.util.monitor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * NetMonitorTest类。
 */
public class NetMonitorTest {

    @Test
    /**
     * testNetMonitorExists方法。
     */
    public void testNetMonitorExists() {
        com.zifang.util.monitor.net.NetMonitor monitor = new com.zifang.util.monitor.net.NetMonitor();
        assertNotNull(monitor);
    }
}
