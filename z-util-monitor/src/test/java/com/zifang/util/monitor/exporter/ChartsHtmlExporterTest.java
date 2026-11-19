package com.zifang.util.monitor.exporter;

import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.Monitor;
import com.zifang.util.monitor.sampling.SampleSink;
import com.zifang.util.monitor.sampling.store.InMemorySampleStore;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 趋势面板导出器测试。
 */
class ChartsHtmlExporterTest {

    @Test
    /**
     * testExportRendersMetrics方法。
     */
    void testExportRendersMetrics() {
        Monitor monitor = Monitor.builder()
                .sampler(new MetricsSampler() {
                    @Override
                    public String id() {
                        return "fake";
                    }

                    @Override
                    public Collection<MetricDefinition> definitions() {
                        return Collections.singletonList(new MetricDefinition(
                                "fake.counter", MetricDefinition.Category.CUSTOM, "count",
                                MetricDefinition.Type.COUNTER, "自增计数器"));
                    }

                    @Override
                    public void sample(SampleSink sink) {
                        sink.put("fake.counter", 42);
                    }
                })
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();
        monitor.sampleNow();

        String html = new ChartsHtmlExporter(monitor, 60_000).export();
        assertTrue(html.contains("fake.counter"));
        assertTrue(html.contains("42"));
        assertTrue(html.contains("<svg"));
    }
}
