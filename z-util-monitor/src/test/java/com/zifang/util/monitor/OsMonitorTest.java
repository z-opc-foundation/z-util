package com.zifang.util.monitor;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OsMonitorTest类。
 */
class OsMonitorTest {

    @Test
    /**
     * testOsMonitorExists方法。
     */
    void testOsMonitorExists() {
        com.zifang.util.monitor.os.OsMonitor monitor = new com.zifang.util.monitor.os.OsMonitor();
        assertNotNull(monitor);
    }

    @Test
    /**
     * testSampleProducesCoreMetrics方法。
     */
    void testSampleProducesCoreMetrics() {
        com.zifang.util.monitor.os.OsMonitor monitor = new com.zifang.util.monitor.os.OsMonitor();
        Map<String, Number> values = collect(monitor);

        assertTrue(values.containsKey("host.cpu.cores"));
        assertTrue(values.get("host.cpu.cores").intValue() > 0);
        assertTrue(values.containsKey("host.process.pid"));
    }

    @Test
    /**
     * testDefinitionsCoverProducedMetrics方法。
     */
    void testDefinitionsCoverProducedMetrics() {
        com.zifang.util.monitor.os.OsMonitor monitor = new com.zifang.util.monitor.os.OsMonitor();
        Map<String, Number> values = collect(monitor);

        assertFalse(monitor.definitions().isEmpty());
        for (String metric : values.keySet()) {
            assertTrue(monitor.definitions().stream().anyMatch(d -> d.getName().equals(metric)),
                    "指标 " + metric + " 缺少定义");
        }
    }

    static Map<String, Number> collect(com.zifang.util.monitor.sampling.MetricsSampler sampler) {
        Map<String, Number> values = new LinkedHashMap<>();
        sampler.sample((metric, value) -> values.put(metric, value));
        return values;
    }
}
