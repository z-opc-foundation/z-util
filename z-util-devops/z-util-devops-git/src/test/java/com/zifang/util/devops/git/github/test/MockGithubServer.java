package com.zifang.util.devops.git.github.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 单元测试用的本地 GitHub REST mock。基于 JDK {@code com.sun.net.httpserver.HttpServer}，
 * 零新依赖。
 *
 * <p>构造时绑定一条 route（method + path + 响应体 + 状态码）；{@link #newClient()}
 * 返回一个已经指向 mock base URL 的 {@link GithubHttpClient}；try-with-resources 关闭。
 *
 * <p>多个 mock 实例可以同时跑：每个实例选一个空闲端口。
 *
 * <p>典型用法：
 * <pre>{@code
 * try (MockGithubServer mock = MockGithubServer.respondTo(
 *         "GET", "/repos/octocat/Hello-World",
 *         "{\"full_name\":\"octocat/Hello-World\", ...}", 200)) {
 *     Repository r = new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").get();
 * }
 * }</pre>
 */
public class MockGithubServer implements AutoCloseable {

    private final HttpServer server;
    private final String baseUrl;
    private final List<HttpExchange> exchanges = new ArrayList<>();

    private MockGithubServer(HttpServer server) {
        this.server = server;
        this.baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    public static MockGithubServer respondTo(String method, String path, String responseBody, int statusCode) {
        try {
            HttpServer http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            MockGithubServer wrapper = new MockGithubServer(http);
            http.createContext(path, new RecordedHandler(method, statusCode, responseBody, wrapper.exchanges));
            http.start();
            return wrapper;
        } catch (IOException e) {
            throw new RuntimeException("Failed to start MockGithubServer", e);
        }
    }

    /**
     * 返回一个指向本 mock base URL 的 GithubHttpClient。
     */
    public GithubHttpClient newClient() {
        return new GithubHttpClient(baseUrl, "mock-token");
    }

    public String baseUrl() {
        return baseUrl;
    }

    /**
     * 收到的所有 HTTP 请求（按到达顺序）。每个请求都包含 path / method / headers。
     */
    public List<HttpExchange> exchanges() {
        return exchanges;
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static final class RecordedHandler implements HttpHandler {
        private final String expectedMethod;
        private final int statusCode;
        private final String body;
        private final List<HttpExchange> sink;

        RecordedHandler(String expectedMethod, int statusCode, String body, List<HttpExchange> sink) {
            this.expectedMethod = expectedMethod;
            this.statusCode = statusCode;
            this.body = body;
            this.sink = sink;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sink.add(exchange);
            if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }
            byte[] bytes = body == null ? new byte[0] : body.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
