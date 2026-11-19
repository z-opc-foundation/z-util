package com.zifang.util.monitor.sampling;

import com.zifang.util.monitor.chart.ChartSeries;
import com.zifang.util.monitor.chart.TimeSeriesChart;
import com.zifang.util.monitor.jvm.JvmMonitor;
import com.zifang.util.monitor.net.NetMonitor;
import com.zifang.util.monitor.os.DiskMonitor;
import com.zifang.util.monitor.os.OsMonitor;
import com.zifang.util.monitor.sampling.store.InMemorySampleStore;
import com.zifang.util.monitor.sampling.store.SampleStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 监控编排器。
 * <p>
 * 把「采集 → 存储 → 查询 → 绘图」串成一条链：
 * </p>
 * <ol>
 *   <li>按固定周期驱动所有采集器，把 JVM / 宿主机 / 磁盘 / 网络的读数汇成一条样本；</li>
 *   <li>样本同时写入多个存储 —— 内存环形缓冲负责实时绘图，磁盘文件负责长时间留痕；</li>
 *   <li>查询接口从存储里取回时间区间内的样本，供图表渲染。</li>
 * </ol>
 * <p>
 * 存储是可插拔的：只挂内存存储就是纯内存监控，同时挂磁盘存储就多一份落盘留痕，
 * 换存储不需要动采集与绘图代码。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * Monitor monitor = Monitor.builder()
 *         .enableDefaultSamplers()
 *         .interval(Duration.ofSeconds(1))
 *         .store(InMemorySampleStore.builder()
 *                 .maxSamples(3600)
 *                 .maxAgeMillis(TimeUnit.MINUTES.toMillis(10))
 *                 .build())
 *         .store(FileSampleStore.builder(Paths.get("/var/log/app/metrics")).build())
 *         .build();
 * monitor.start();
 *
 * String svg = monitor.chartSvg("jvm.memory.heap.used", 5 * 60 * 1000L);
 * }</pre>
 *
 * @author zifang
 */
public final class Monitor implements AutoCloseable {

    private static final long DEFAULT_INTERVAL_MILLIS = 1000L;
    private static final int DEFAULT_MAX_SAMPLES = 3600;

    private final List<MetricsSampler> samplers;
    private final List<SampleStore> stores;
    private final Map<String, MetricDefinition> definitions;
    private final long intervalMillis;
    private final List<Consumer<Sample>> listeners = new CopyOnWriteArrayList<>();

    private volatile ScheduledExecutorService scheduler;
    private volatile boolean running;
    private volatile Sample latestSample;

    private Monitor(List<MetricsSampler> samplers, List<SampleStore> stores,
                    Map<String, MetricDefinition> definitions, long intervalMillis) {
        this.samplers = Collections.unmodifiableList(new ArrayList<>(samplers));
        this.stores = Collections.unmodifiableList(new ArrayList<>(stores));
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
        this.intervalMillis = intervalMillis;
    }

    /**
     * 创建构建器。
     *
     * @return Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 启动定时采样。
     */
    public void start() {
        if (running) {
            return;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "z-util-monitor");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler = executor;
        running = true;
        executor.scheduleAtFixedRate(this::safeSample, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定时采样。存储不会被关闭，可再次 {@link #start()}。
     */
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        ScheduledExecutorService executor = this.scheduler;
        this.scheduler = null;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * isRunning方法。
     *
     * @return boolean类型返回值
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 立即采一次样并写入存储。
     *
     * @return Sample类型返回值
     */
    public Sample sampleNow() {
        Sample sample = collect();
        if (sample == null) {
            return null;
        }
        publish(sample);
        return sample;
    }

    /**
     * 最新一条样本。
     *
     * @return Sample类型返回值，无数据时返回 null
     */
    public Sample latest() {
        return latestSample;
    }

    /**
     * 查询时间区间内的样本。
     *
     * @param from long类型参数
     * @param to   long类型参数
     *
     * @return ListSample类型返回值
     */
    public List<Sample> query(long from, long to) {
        SampleStore store = primaryStore();
        return store == null ? Collections.emptyList() : store.query(from, to);
    }

    /**
     * 取出某个指标的时间序列。
     *
     * @param metric String类型参数
     * @param from   long类型参数
     * @param to     long类型参数
     *
     * @return ChartSeries类型返回值
     */
    public ChartSeries series(String metric, long from, long to) {
        ChartSeries.Builder builder = ChartSeries.builder(metric);
        MetricDefinition definition = definitions.get(metric);
        if (definition != null) {
            builder.unit(definition.getUnit());
        }
        for (Sample sample : query(from, to)) {
            Number value = sample.get(metric);
            if (value != null) {
                builder.point(sample.getTimestamp(), value.doubleValue());
            }
        }
        return builder.build();
    }

    /**
     * 一次性取出全部指标的时间序列。
     * <p>
     * 只扫一遍样本就能拆出所有指标的序列，比逐个指标反复查存储快得多 ——
     * 监控面板要一次画几十条曲线时用这个。
     * </p>
     * <p>
     * 静态指标表里没有的运行时指标（如逐网卡的 {@code net.iface.*}）同样会被拆出来，
     * 只是没有单位信息。
     * </p>
     *
     * @param from long类型参数
     * @param to   long类型参数
     *
     * @return MapStringChartSeries类型返回值
     */
    public Map<String, ChartSeries> seriesAll(long from, long to) {
        Map<String, ChartSeries.Builder> builders = new LinkedHashMap<>();
        for (Sample sample : query(from, to)) {
            for (Map.Entry<String, Number> entry : sample.getValues().entrySet()) {
                ChartSeries.Builder builder = builders.computeIfAbsent(entry.getKey(), name -> {
                    ChartSeries.Builder created = ChartSeries.builder(name);
                    MetricDefinition definition = definitions.get(name);
                    return definition == null ? created : created.unit(definition.getUnit());
                });
                builder.point(sample.getTimestamp(), entry.getValue().doubleValue());
            }
        }
        Map<String, ChartSeries> result = new LinkedHashMap<>();
        for (Map.Entry<String, ChartSeries.Builder> entry : builders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().build());
        }
        return result;
    }

    /**
     * 渲染某个指标最近一段时间的折线图。
     *
     * @param metric       String类型参数
     * @param windowMillis long类型参数
     *
     * @return String类型返回值
     */
    public String chartSvg(String metric, long windowMillis) {
        long to = System.currentTimeMillis();
        long from = to - windowMillis;
        ChartSeries series = series(metric, from, to);
        MetricDefinition definition = definitions.get(metric);
        return TimeSeriesChart.builder()
                .title(metric)
                .unit(definition == null ? null : definition.getUnit())
                .range(from, to)
                .series(series)
                .build()
                .toSvg();
    }

    /**
     * 渲染多个指标叠加的折线图。
     *
     * @param metrics      ListString类型参数
     * @param windowMillis long类型参数
     *
     * @return String类型返回值
     */
    public String chartSvg(List<String> metrics, long windowMillis) {
        long to = System.currentTimeMillis();
        long from = to - windowMillis;
        TimeSeriesChart.Builder builder = TimeSeriesChart.builder()
                .title(String.join(" / ", metrics))
                .range(from, to);
        String unit = null;
        for (String metric : metrics) {
            ChartSeries series = series(metric, from, to);
            builder.series(series);
            MetricDefinition definition = definitions.get(metric);
            if (definition != null) {
                unit = definition.getUnit();
            }
        }
        return builder.unit(unit).build().toSvg();
    }

    /**
     * 取指标定义。
     *
     * @param metric String类型参数
     *
     * @return MetricDefinition类型返回值
     */
    public MetricDefinition definition(String metric) {
        return definitions.get(metric);
    }

    /**
     * 全部指标定义。
     *
     * @return CollectionMetricDefinition类型返回值
     */
    public Collection<MetricDefinition> definitions() {
        return definitions.values();
    }

    /**
     * 全部指标名。
     *
     * @return ListString类型返回值
     */
    public List<String> metricNames() {
        return new ArrayList<>(definitions.keySet());
    }

    /**
     * 注册采样监听器，每采到一条样本回调一次。
     *
     * @param listener ConsumerSample类型参数
     */
    public void addListener(Consumer<Sample> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除采样监听器。
     *
     * @param listener ConsumerSample类型参数
     */
    public void removeListener(Consumer<Sample> listener) {
        listeners.remove(listener);
    }

    /**
     * getSamplers方法。
     *
     * @return ListMetricsSampler类型返回值
     */
    public List<MetricsSampler> getSamplers() {
        return samplers;
    }

    /**
     * getStores方法。
     *
     * @return ListSampleStore类型返回值
     */
    public List<SampleStore> getStores() {
        return stores;
    }

    /**
     * getIntervalMillis方法。
     *
     * @return long类型返回值
     */
    public long getIntervalMillis() {
        return intervalMillis;
    }

    @Override
    public void close() {
        stop();
        for (SampleStore store : stores) {
            try {
                store.close();
            } catch (Exception ignored) {
                // 关闭失败不影响其它存储
            }
        }
    }

    private SampleStore primaryStore() {
        return stores.isEmpty() ? null : stores.get(0);
    }

    private void safeSample() {
        try {
            sampleNow();
        } catch (Throwable e) {
            // 单次采样失败不应中断后续采样
        }
    }

    private Sample collect() {
        if (samplers.isEmpty()) {
            return null;
        }
        Sample.Builder builder = Sample.builder(System.currentTimeMillis());
        SampleSink sink = new SampleSink() {
            @Override
            public void put(String metric, double value) {
                builder.put(metric, value);
            }

            @Override
            public void attribute(String key, String value) {
                builder.attribute(key, value);
            }
        };
        for (MetricsSampler sampler : samplers) {
            try {
                sampler.sample(sink);
            } catch (Throwable e) {
                // 单个采集器失败不影响其它采集器
            }
        }
        return builder.build();
    }

    private void publish(Sample sample) {
        latestSample = sample;
        for (SampleStore store : stores) {
            try {
                store.append(sample);
            } catch (Exception ignored) {
                // 单个存储写失败不影响其它存储
            }
        }
        for (Consumer<Sample> listener : listeners) {
            try {
                listener.accept(sample);
            } catch (Exception ignored) {
                // 监听器异常不影响采样主流程
            }
        }
    }

    /**
     * 监控构建器。
     */
    public static final class Builder {

        private final List<MetricsSampler> samplers = new ArrayList<>();
        private final List<SampleStore> stores = new ArrayList<>();
        private final List<Consumer<Sample>> listeners = new ArrayList<>();
        private long intervalMillis = DEFAULT_INTERVAL_MILLIS;

        /**
         * 采样周期。
         *
         * @param interval Duration类型参数
         *
         * @return Builder类型返回值
         */
        public Builder interval(Duration interval) {
            if (interval != null && !interval.isNegative() && !interval.isZero()) {
                this.intervalMillis = interval.toMillis();
            }
            return this;
        }

        /**
         * 采样周期。
         *
         * @param intervalMillis long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder intervalMillis(long intervalMillis) {
            if (intervalMillis > 0) {
                this.intervalMillis = intervalMillis;
            }
            return this;
        }

        /**
         * 挂载一个存储，可多次调用。
         *
         * @param store SampleStore类型参数
         *
         * @return Builder类型返回值
         */
        public Builder store(SampleStore store) {
            if (store != null) {
                stores.add(store);
            }
            return this;
        }

        /**
         * 挂载一个采集器，可多次调用。
         *
         * @param sampler MetricsSampler类型参数
         *
         * @return Builder类型返回值
         */
        public Builder sampler(MetricsSampler sampler) {
            if (sampler != null) {
                samplers.add(sampler);
            }
            return this;
        }

        /**
         * 挂载内置的 JVM、宿主机、磁盘、网络采集器。
         *
         * @return Builder类型返回值
         */
        public Builder enableDefaultSamplers() {
            samplers.add(new JvmMonitor());
            samplers.add(new OsMonitor());
            samplers.add(new DiskMonitor());
            samplers.add(new NetMonitor());
            return this;
        }

        /**
         * 注册采样监听器。
         *
         * @param listener ConsumerSample类型参数
         *
         * @return Builder类型返回值
         */
        public Builder listener(Consumer<Sample> listener) {
            if (listener != null) {
                listeners.add(listener);
            }
            return this;
        }

        /**
         * build方法。
         *
         * @return Monitor类型返回值
         */
        public Monitor build() {
            if (stores.isEmpty()) {
                stores.add(InMemorySampleStore.builder()
                        .maxSamples(DEFAULT_MAX_SAMPLES)
                        .build());
            }
            Map<String, MetricDefinition> catalog = new LinkedHashMap<>();
            for (MetricsSampler sampler : samplers) {
                for (MetricDefinition definition : sampler.definitions()) {
                    catalog.put(definition.getName(), definition);
                }
            }
            Monitor monitor = new Monitor(samplers, stores, catalog, intervalMillis);
            for (Consumer<Sample> listener : listeners) {
                monitor.addListener(listener);
            }
            return monitor;
        }
    }
}
