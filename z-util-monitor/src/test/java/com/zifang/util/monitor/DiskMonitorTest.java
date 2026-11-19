package com.zifang.util.monitor;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DiskMonitorTest类。
 */
class DiskMonitorTest {

    @Test
    /**
     * testDiskMonitorExists方法。
     */
    void testDiskMonitorExists() {
        com.zifang.util.monitor.os.DiskMonitor monitor = new com.zifang.util.monitor.os.DiskMonitor();
        assertNotNull(monitor);
    }

    @Test
    /**
     * testSampleProducesCoreMetrics方法。
     */
    void testSampleProducesCoreMetrics() {
        com.zifang.util.monitor.os.DiskMonitor monitor = new com.zifang.util.monitor.os.DiskMonitor();
        Map<String, Number> values = new LinkedHashMap<>();
        monitor.sample((metric, value) -> values.put(metric, value));

        assertTrue(values.containsKey("disk.count"));
        assertTrue(values.get("disk.count").intValue() >= 0);
        assertTrue(values.containsKey("disk.count") || values.isEmpty());
    }

    @Test
    /**
     * testDefinitionsAreNotEmpty方法。
     */
    void testDefinitionsAreNotEmpty() {
        com.zifang.util.monitor.os.DiskMonitor monitor = new com.zifang.util.monitor.os.DiskMonitor();
        assertTrue(monitor.definitions().stream()
                .anyMatch(d -> d.getName().equals("disk.count")));
    }
}
