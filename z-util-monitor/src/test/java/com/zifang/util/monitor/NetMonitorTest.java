package com.zifang.util.monitor;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NetMonitorTest类。
 */
class NetMonitorTest {

    @Test
    /**
     * testNetMonitorExists方法。
     */
    void testNetMonitorExists() {
        com.zifang.util.monitor.net.NetMonitor monitor = new com.zifang.util.monitor.net.NetMonitor();
        assertNotNull(monitor);
    }

    @Test
    /**
     * testSampleProducesCoreMetrics方法。
     */
    void testSampleProducesCoreMetrics() {
        com.zifang.util.monitor.net.NetMonitor monitor = new com.zifang.util.monitor.net.NetMonitor();
        Map<String, Number> values = collect(monitor);

        assertTrue(values.containsKey("net.interface.count"));
        assertTrue(values.get("net.interface.count").intValue() > 0);
    }

    @Test
    /**
     * testSecondSampleProducesRate方法。
     * <p>
     * 速率依赖累计字节，而累计字节读自 {@code /proc/net/dev} —— 非 Linux 平台
     * 没有这个文件，因此只在能读到累计量时才断言速率存在。
     * </p>
     */
    void testSecondSampleProducesRate() {
        com.zifang.util.monitor.net.NetMonitor monitor = new com.zifang.util.monitor.net.NetMonitor();
        Map<String, Number> first = collect(monitor);
        Map<String, Number> values = collect(monitor);

        if (first.containsKey("net.bytes.received")) {
            assertTrue(values.containsKey("net.rx.rate"));
            assertTrue(values.containsKey("net.tx.rate"));
            assertTrue(values.get("net.rx.rate").doubleValue() >= 0);
        }
    }

    @Test
    /**
     * testDefinitionsCoverStaticMetrics方法。
     * <p>
     * 逐网卡的指标（net.iface.xxx）是运行时才知道的，不在静态定义表里，
     * 这里只校验固定的那批。
     * </p>
     */
    void testDefinitionsCoverStaticMetrics() {
        com.zifang.util.monitor.net.NetMonitor monitor = new com.zifang.util.monitor.net.NetMonitor();

        assertFalse(monitor.definitions().isEmpty());
        for (String metric : new String[]{"net.interface.count", "net.bytes.received",
                "net.bytes.sent", "net.rx.rate", "net.tx.rate", "net.tcp.total"}) {
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
