package com.zifang.util.monitor;

import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.Monitor;
import com.zifang.util.monitor.sampling.SampleSink;
import com.zifang.util.monitor.sampling.store.InMemorySampleStore;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 监控服务器测试。
 */
class MonitorServerTest {

    private static final class FakeSampler implements MetricsSampler {

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
    }

    private static MonitorServer startServer() throws Exception {
        Monitor monitor = Monitor.builder()
                .sampler(new FakeSampler())
                .store(InMemorySampleStore.builder().build())
                .build();
        monitor.sampleNow();
        monitor.sampleNow();

        MonitorServer server = MonitorServer.builder()
                .port(0)
                .registerDefaultMonitor()
                .monitor(monitor)
                .build()
                .start();
        return server;
    }

    private static String fetch(String path) throws Exception {
        URL url = new URL("http://127.0.0.1:" + CURRENT_PORT + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream in = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static int CURRENT_PORT;

    @Test
    /**
     * testTrendEndpoints方法。
     */
    void testTrendEndpoints() throws Exception {
        MonitorServer server = startServer();
        CURRENT_PORT = server.getPort();
        try {
            String charts = fetch("/charts");
            assertTrue(charts.contains("fake.counter"));
            assertTrue(charts.contains("Z-Util Monitor 趋势"));

            String svg = fetch("/chart.svg?metric=fake.counter&window=60");
            assertTrue(svg.contains("<svg"));
            assertTrue(svg.contains("polyline"));

            String series = fetch("/series?metric=fake.counter&window=60");
            assertTrue(series.contains("\"metric\":\"fake.counter\""));
            assertTrue(series.contains("\"v\":42.0"));

            String json = fetch("/json");
            assertTrue(json.contains("\"metrics\""));
        } finally {
            server.stop();
            CURRENT_PORT = 0;
        }
    }

    @Test
    /**
     * testSeriesRequiresMetric方法。
     */
    void testSeriesRequiresMetric() throws Exception {
        MonitorServer server = startServer();
        CURRENT_PORT = server.getPort();
        try {
            URL url = new URL("http://127.0.0.1:" + CURRENT_PORT + "/series");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            assertEquals(400, connection.getResponseCode());
            connection.disconnect();
        } finally {
            server.stop();
            CURRENT_PORT = 0;
        }
    }
}
