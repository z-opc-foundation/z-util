package com.zifang.util.monitor.sampling;

import com.zifang.util.monitor.chart.ChartSeries;
import com.zifang.util.monitor.sampling.store.InMemorySampleStore;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Monitor编排器测试。
 */
class MonitorTest {

    /**
     * 固定产出一个递增指标的假采集器。
     */
    private static final class FakeSampler implements MetricsSampler {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String id() {
            return "fake";
        }

        @Override
        public Collection<MetricDefinition> definitions() {
            return java.util.Collections.singletonList(new MetricDefinition(
                    "fake.counter", MetricDefinition.Category.CUSTOM, "count",
                    MetricDefinition.Type.COUNTER, "自增计数器"));
        }

        @Override
        public void sample(SampleSink sink) {
            sink.put("fake.counter", counter.incrementAndGet());
            sink.attribute("fake.tag", "v" + counter.get());
        }
    }

    @Test
    /**
     * testSampleNowProducesSample方法。
     */
    void testSampleNowProducesSample() {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .build();

        Sample sample = monitor.sampleNow();
        assertNotNull(sample);
        assertEquals(1.0, sample.getDouble("fake.counter", -1));
        assertEquals("v1", sample.getAttributes().get("fake.tag"));
        assertNotNull(monitor.latest());
        assertEquals(1, monitor.query(0, Long.MAX_VALUE).size());
    }

    @Test
    /**
     * testDefinitionsCollectedFromSamplers方法。
     */
    void testDefinitionsCollectedFromSamplers() {
        Monitor monitor = Monitor.builder().sampler(new FakeSampler()).build();

        assertEquals(1, monitor.definitions().size());
        assertNotNull(monitor.definition("fake.counter"));
        assertEquals("count", monitor.definition("fake.counter").getUnit());
        assertTrue(monitor.metricNames().contains("fake.counter"));
    }

    @Test
    /**
     * testSeriesBuildsTimeSeries方法。
     */
    void testSeriesBuildsTimeSeries() {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();
        monitor.sampleNow();
        monitor.sampleNow();

        ChartSeries series = monitor.series("fake.counter", 0, Long.MAX_VALUE);
        assertEquals(3, series.getPoints().size());
        assertEquals(1.0, series.getPoints().get(0).getValue());
        assertEquals(3.0, series.getPoints().get(2).getValue());
        assertEquals("count", series.getUnit());
    }

    @Test
    /**
     * testSeriesAllBuildsEveryMetric方法。
     */
    void testSeriesAllBuildsEveryMetric() {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();
        monitor.sampleNow();

        assertEquals(1, monitor.seriesAll(0, Long.MAX_VALUE).size());
        assertEquals(2, monitor.seriesAll(0, Long.MAX_VALUE).get("fake.counter").getPoints().size());
    }

    @Test
    /**
     * testChartSvgRenders方法。
     */
    void testChartSvgRenders() {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();
        monitor.sampleNow();

        String svg = monitor.chartSvg("fake.counter", 60_000);
        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("polyline"));
        assertTrue(svg.contains("fake.counter"));
    }

    @Test
    /**
     * testListenerReceivesSamples方法。
     */
    void testListenerReceivesSamples() {
        AtomicInteger received = new AtomicInteger();
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .listener(sample -> received.incrementAndGet())
                .build();

        monitor.sampleNow();
        monitor.sampleNow();
        assertEquals(2, received.get());
    }

    @Test
    /**
     * testStartAndStop方法。
     */
    void testStartAndStop() throws Exception {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .intervalMillis(50)
                .store(InMemorySampleStore.builder().build())
                .build();

        assertFalse(monitor.isRunning());
        monitor.start();
        assertTrue(monitor.isRunning());
        Thread.sleep(300);
        monitor.stop();
        assertFalse(monitor.isRunning());

        assertTrue(monitor.query(0, Long.MAX_VALUE).size() >= 2);
        monitor.close();
    }

    @Test
    /**
     * testDefaultStoreIsMemory方法。
     */
    void testDefaultStoreIsMemory() {
        Monitor monitor = Monitor.builder().sampler(new FakeSampler()).build();
        assertEquals(1, monitor.getStores().size());
        assertTrue(monitor.getStores().get(0) instanceof InMemorySampleStore);
        assertNull(monitor.latest());
        assertNotNull(monitor.sampleNow());
        assertNotNull(monitor.latest());
    }

    @Test
    /**
     * testEmptyMonitorHasNoLatest方法。
     */
    void testEmptyMonitorHasNoLatest() {
        Monitor monitor = Monitor.builder()
                .store(InMemorySampleStore.builder().build())
                .build();
        assertNull(monitor.sampleNow());
        assertNull(monitor.latest());
    }

    @Test
    /**
     * testRestartAfterStop方法。
     */
    void testRestartAfterStop() throws Exception {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .intervalMillis(50)
                .store(InMemorySampleStore.builder().build())
                .build();

        monitor.start();
        Thread.sleep(200);
        monitor.stop();
        int afterFirstRun = monitor.query(0, Long.MAX_VALUE).size();

        monitor.start();
        Thread.sleep(200);
        monitor.stop();

        assertTrue(monitor.query(0, Long.MAX_VALUE).size() > afterFirstRun,
                "重新 start 后应当继续采样");
        monitor.close();
    }

    @Test
    /**
     * testSeriesAllIncludesDynamicMetrics方法。
     */
    void testSeriesAllIncludesDynamicMetrics() {
        Monitor monitor = Monitor.builder()
                .sampler(new MetricsSampler() {
                    @Override
                    public String id() {
                        return "dynamic";
                    }

                    @Override
                    public Collection<MetricDefinition> definitions() {
                        return Collections.emptyList();
                    }

                    @Override
                    public void sample(SampleSink sink) {
                        sink.put("dynamic.per.iface.metric", 7);
                    }
                })
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();

        Map<String, ChartSeries> all = monitor.seriesAll(0, Long.MAX_VALUE);
        assertTrue(all.containsKey("dynamic.per.iface.metric"));
        assertEquals(1, all.get("dynamic.per.iface.metric").getPoints().size());
    }
}
