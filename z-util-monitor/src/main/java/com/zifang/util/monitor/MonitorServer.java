package com.zifang.util.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zifang.util.monitor.chart.ChartSeries;
import com.zifang.util.monitor.exporter.ChartsHtmlExporter;
import com.zifang.util.monitor.exporter.HtmlExporter;
import com.zifang.util.monitor.exporter.JsonExporter;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.Monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 监控服务器
 * <p>
 * 内置 HTTP 服务器，提供监控指标的 Web UI 和 JSON API。
 * </p>
 * <p>
 * 挂上 {@link Monitor} 之后额外提供趋势相关的路由：
 * <ul>
 *   <li>{@code /charts} —— 全部指标的迷你趋势面板</li>
 *   <li>{@code /chart.svg?metric=xxx&window=秒} —— 单个指标的 SVG 折线图</li>
 *   <li>{@code /series?metric=xxx&window=秒} —— 单个指标的时间序列 JSON</li>
 * </ul>
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 创建并启动服务器（注册所有内置指标）
 * MonitorServer server = MonitorServer.builder()
 *     .port(8849)
 *     .registerDefaultMonitor()  // 注册 JVM/OS/线程等内置指标
 *     .build()
 *     .start();
 *
 * // 注册自定义指标
 * server.register("myapp.counter", () -> MyCounter.getValue(), "自定义计数器", "次");
 *
 * // 停止服务器
 * server.stop();
 * }</pre>
 */
public class MonitorServer {

    public static final int DEFAULT_PORT = 8849;
    /**
     * 趋势窗口默认值：5 分钟。
     */
    public static final long DEFAULT_WINDOW_MILLIS = 5 * 60 * 1000L;

    private final int port;
    private final MetricsCollector collector;
    private final Monitor monitor;
    private HttpServer httpServer;
    private volatile boolean started = false;

    private MonitorServer(int port, Monitor monitor) {
        this.port = port;
        this.monitor = monitor;
        this.collector = new MetricsCollector();
    }

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    private static void sendResponse(HttpExchange exchange, String contentType, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * 解析查询串。
     *
     * @param rawQuery String类型参数
     *
     * @return MapStringString类型返回值
     */
    static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int index = pair.indexOf('=');
            String key = index < 0 ? pair : pair.substring(0, index);
            String value = index < 0 ? "" : pair.substring(index + 1);
            params.put(decode(key), decode(value));
        }
        return params;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private static long resolveWindow(Map<String, String> params) {
        String raw = params.get("window");
        if (raw == null || raw.isEmpty()) {
            return DEFAULT_WINDOW_MILLIS;
        }
        try {
            long seconds = Long.parseLong(raw.trim());
            return seconds > 0 ? seconds * 1000L : DEFAULT_WINDOW_MILLIS;
        } catch (NumberFormatException e) {
            return DEFAULT_WINDOW_MILLIS;
        }
    }

    /**
     * 启动服务器
     */
    public MonitorServer start() throws IOException {
        if (started) {
            throw new IllegalStateException("Server already started");
        }

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        // 注册路由
        httpServer.createContext("/", new IndexHandler(this));
        httpServer.createContext("/json", new JsonHandler(this));
        httpServer.createContext("/health", new HealthHandler(this));
        httpServer.createContext("/metrics", new MetricsHandler(this));
        httpServer.createContext("/charts", new ChartsHandler(this));
        httpServer.createContext("/chart.svg", new ChartSvgHandler(this));
        httpServer.createContext("/series", new SeriesHandler(this));

        // 设置线程池
        httpServer.setExecutor(Executors.newFixedThreadPool(4));

        // 启动服务器
        httpServer.start();
        started = true;

        System.out.println("MonitorServer started on http://localhost:" + port);
        System.out.println("  - Web UI: http://localhost:" + port + "/");
        System.out.println("  - JSON API: http://localhost:" + port + "/json");
        System.out.println("  - Health: http://localhost:" + port + "/health");
        if (monitor != null) {
            System.out.println("  - Trends: http://localhost:" + port + "/charts");
        }

        return this;
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (!started || httpServer == null) {
            return;
        }
        httpServer.stop(0);
        started = false;
        System.out.println("MonitorServer stopped");
    }

    /**
     * 重新启动服务器
     */
    public MonitorServer restart() throws IOException {
        stop();
        Thread.yield();
        return start();
    }

    /**
     * getPort方法。
     * <p>
     * 启动后返回<b>实际绑定</b>的端口：传入 0 让系统分配时，这个值才是真实端口。
     * </p>
     *
     * @return int类型返回值
     */
    public int getPort() {
        if (started && httpServer != null) {
            return httpServer.getAddress().getPort();
        }
        return port;
    }

    /**
     * getCollector方法。
     *
     * @return MetricsCollector类型返回值
     */
    public MetricsCollector getCollector() {
        return collector;
    }

    /**
     * getRegistry方法。
     *
     * @return MetricsRegistry类型返回值
     */
    public MetricsRegistry getRegistry() {
        return collector.getRegistry();
    }

    /**
     * getMonitor方法。
     *
     * @return Monitor类型返回值，未挂载时返回 null
     */
    public Monitor getMonitor() {
        return monitor;
    }

    // ========== 注册方法 ==========

    /**
     * isStarted方法。
     *
     * @return boolean类型返回值
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 注册自定义指标
     */
    public MonitorServer register(String name, java.util.function.Supplier<Object> provider) {
        collector.getRegistry().register(name, MetricsSnapshot.Category.CUSTOM, provider);
        return this;
    }

    /**
     * 注册自定义指标（带描述）
     */
    public MonitorServer register(String name, java.util.function.Supplier<Object> provider, String description) {
        collector.getRegistry().register(name, MetricsSnapshot.Category.CUSTOM, provider, description, null);
        return this;
    }

    // ========== HTTP Handlers ==========

    /**
     * 注册自定义指标（带描述和单位）
     */
    public MonitorServer register(String name, java.util.function.Supplier<Object> provider, String description, String unit) {
        collector.getRegistry().register(name, MetricsSnapshot.Category.CUSTOM, provider, description, unit);
        return this;
    }

    static class IndexHandler implements HttpHandler {
        private final MonitorServer server;

        IndexHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            String response = new HtmlExporter(server.getRegistry()).export();
            sendResponse(exchange, "text/html; charset=utf-8", response);
        }
    }

    static class JsonHandler implements HttpHandler {
        private final MonitorServer server;

        JsonHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            String response = new JsonExporter(server.getRegistry()).export();
            sendResponse(exchange, "application/json; charset=utf-8", response);
        }
    }

    static class HealthHandler implements HttpHandler {
        private final MonitorServer server;

        HealthHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            String status = server.getRegistry().isEnabled() ? "UP" : "DOWN";
            String response = "{\"status\":\"" + status + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            sendResponse(exchange, "application/json; charset=utf-8", response);
        }
    }

    static class MetricsHandler implements HttpHandler {
        private final MonitorServer server;

        MetricsHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            // 重定向到 /json
            exchange.getResponseHeaders().set("Location", "/json");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    static class ChartsHandler implements HttpHandler {
        private final MonitorServer server;

        ChartsHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (server.monitor == null) {
                sendError(exchange, 503, "未挂载 Monitor");
                return;
            }
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            long window = resolveWindow(params);
            String response = new ChartsHtmlExporter(server.monitor, window).export();
            sendResponse(exchange, "text/html; charset=utf-8", response);
        }
    }

    static class ChartSvgHandler implements HttpHandler {
        private final MonitorServer server;

        ChartSvgHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (server.monitor == null) {
                sendError(exchange, 503, "未挂载 Monitor");
                return;
            }
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String metric = params.get("metric");
            if (metric == null || metric.isEmpty()) {
                sendError(exchange, 400, "缺少 metric 参数");
                return;
            }
            long window = resolveWindow(params);
            String response = server.monitor.chartSvg(metric, window);
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            sendResponse(exchange, "image/svg+xml; charset=utf-8", response);
        }
    }

    static class SeriesHandler implements HttpHandler {
        private final MonitorServer server;

        SeriesHandler(MonitorServer server) {
            this.server = server;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (server.monitor == null) {
                sendError(exchange, 503, "未挂载 Monitor");
                return;
            }
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String metric = params.get("metric");
            if (metric == null || metric.isEmpty()) {
                sendError(exchange, 400, "缺少 metric 参数");
                return;
            }
            long window = resolveWindow(params);
            long to = System.currentTimeMillis();
            long from = to - window;
            ChartSeries series = server.monitor.series(metric, from, to);
            MetricDefinition definition = server.monitor.definition(metric);

            StringBuilder json = new StringBuilder(1024);
            json.append("{\"metric\":\"").append(metric).append("\",");
            json.append("\"unit\":").append(definition == null || definition.getUnit() == null
                    ? "null" : "\"" + definition.getUnit() + "\"").append(",");
            json.append("\"from\":").append(from).append(",");
            json.append("\"to\":").append(to).append(",");
            json.append("\"points\":[");
            List<ChartSeries.Point> points = series.getPoints();
            for (int i = 0; i < points.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append("{\"t\":").append(points.get(i).getTimestamp())
                        .append(",\"v\":").append(points.get(i).getValue()).append('}');
            }
            json.append("]}");
            sendResponse(exchange, "application/json; charset=utf-8", json.toString());
        }
    }

    // ========== Builder ==========

    public static class Builder {
        private int port = DEFAULT_PORT;
        private boolean registerDefaultMonitor = false;
        private Monitor monitor;

        /**
         * port方法。
         * * @param port int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * 注册所有内置指标（JVM、OS、线程）
         */
        public Builder registerDefaultMonitor() {
            this.registerDefaultMonitor = true;
            return this;
        }

        /**
         * 挂载趋势监控，提供 /charts、/chart.svg、/series 三个路由。
         *
         * @param monitor Monitor类型参数
         *
         * @return Builder类型返回值
         */
        public Builder monitor(Monitor monitor) {
            this.monitor = monitor;
            return this;
        }

        /**
         * build方法。
         *
         * @return MonitorServer类型返回值
         */
        public MonitorServer build() {
            MonitorServer server = new MonitorServer(port, monitor);
            if (registerDefaultMonitor) {
                server.collector.enableAll();
            }
            return server;
        }
    }
}
