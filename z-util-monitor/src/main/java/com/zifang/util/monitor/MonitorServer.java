package com.zifang.util.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zifang.util.monitor.exporter.HtmlExporter;
import com.zifang.util.monitor.exporter.JsonExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * 监控服务器
 * <p>
 * 内置 HTTP 服务器，提供监控指标的 Web UI 和 JSON API。
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
    private final int port;
    private final MetricsCollector collector;
    private HttpServer httpServer;
    private volatile boolean started = false;

    private MonitorServer(int port) {
        this.port = port;
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

        // 设置线程池
        httpServer.setExecutor(Executors.newFixedThreadPool(4));

        // 启动服务器
        httpServer.start();
        started = true;

        System.out.println("MonitorServer started on http://localhost:" + port);
        System.out.println("  - Web UI: http://localhost:" + port + "/");
        System.out.println("  - JSON API: http://localhost:" + port + "/json");
        System.out.println("  - Health: http://localhost:" + port + "/health");

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
     *
     * @return int类型返回值
     */
    public int getPort() {
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

    // ========== Builder ==========

    public static class Builder {
        private int port = DEFAULT_PORT;
        private boolean registerDefaultMonitor = false;

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
         * build方法。
         *
         * @return MonitorServer类型返回值
         */
        public MonitorServer build() {
            MonitorServer server = new MonitorServer(port);
            if (registerDefaultMonitor) {
                server.collector.enableAll();
            }
            return server;
        }
    }
}
