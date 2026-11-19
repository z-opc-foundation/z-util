package com.zifang.util.monitor;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JvmMonitorTest类。
 */
class JvmMonitorTest {

    @Test
    /**
     * testJvmMonitorExists方法。
     */
    void testJvmMonitorExists() {
        com.zifang.util.monitor.jvm.JvmMonitor monitor = new com.zifang.util.monitor.jvm.JvmMonitor();
        assertNotNull(monitor);
    }

    @Test
    /**
     * testSampleProducesCoreMetrics方法。
     */
    void testSampleProducesCoreMetrics() {
        com.zifang.util.monitor.jvm.JvmMonitor monitor = new com.zifang.util.monitor.jvm.JvmMonitor();
        Map<String, Number> values = collect(monitor);

        assertTrue(values.containsKey("jvm.memory.heap.used"));
        assertTrue(values.get("jvm.memory.heap.used").doubleValue() > 0);
        assertTrue(values.containsKey("jvm.memory.heap.max"));
        assertTrue(values.containsKey("jvm.runtime.uptime"));
        assertTrue(values.containsKey("jvm.thread.count"));
        assertTrue(values.containsKey("jvm.class.loaded"));
        assertTrue(values.containsKey("jvm.thread.state.runnable"));
    }

    @Test
    /**
     * testDefinitionsCoverProducedMetrics方法。
     */
    void testDefinitionsCoverProducedMetrics() {
        com.zifang.util.monitor.jvm.JvmMonitor monitor = new com.zifang.util.monitor.jvm.JvmMonitor();
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
