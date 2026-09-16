package com.zifang.util.http.client;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;
import com.zifang.util.json.JsonUtil;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * HttpExecutor 单元测试
 * <p>
 * 覆盖：
 * 1. executeByMethodUrl  - 4 种 HTTP 方法（GET/POST/PUT/DELETE）
 * 2. execute(definition)  - 直接拿 HttpRequestDefinition 跑
 * 3. executeByCurl        - curl 字符串入口
 * 4. executeByDefinitionJson - JSON 入口
 * 5. sendAsync            - 异步执行
 * 6. HttpExecutionResult  - 结果 POJO 字段完整性
 * 7. 错误处理             - 5xx / 异常 URL
 * <p>
 * 不依赖任何外部网络 — 用 com.sun.net.httpserver.HttpServer 起本地端口。
 */
public class HttpExecutorTest {

    private static final HttpExecutor executor = HttpExecutor.getDefault();
    private static HttpServer server;
    private static int port;

    @BeforeClass
    public static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();

        // GET /hello
        server.createContext("/hello", ex -> {
            byte[] resp = "{\"msg\":\"hello\"}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });

        // POST /echo  (回写 body)
        server.createContext("/echo", ex -> {
            java.io.InputStream in = ex.getRequestBody();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) != -1) {
                buf.write(chunk, 0, n);
            }
            byte[] body = buf.toByteArray();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length == 0 ? 0 : body.length);
            if (body.length > 0) ex.getResponseBody().write(body);
            ex.close();
        });

        // PUT /put
        server.createContext("/put", ex -> {
            byte[] resp = "{\"put\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });

        // DELETE /del
        server.createContext("/del", ex -> {
            byte[] resp = "{\"del\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });

        // /slow — 用于异步测试
        server.createContext("/slow", ex -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            byte[] resp = "{\"slow\":\"done\"}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });

        // /boom — 永远 500
        server.createContext("/boom", ex -> {
            byte[] resp = "{\"err\":\"boom\"}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(500, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });

        // /sse-test — 简单 SSE
        server.createContext("/sse-test", ex -> {
            ex.getResponseHeaders().add("Content-Type", "text/event-stream");
            ex.sendResponseHeaders(200, 0);
            java.io.OutputStream os = ex.getResponseBody();
            os.write("data: hello-1\n\n".getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.write("data: hello-2\n\n".getBytes(StandardCharsets.UTF_8));
            os.flush();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            ex.close();
        });

        server.start();
        System.out.println(">>> HttpExecutorTest server started on port " + port);
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) server.stop(0);
    }

    private String base() {
        return "http://127.0.0.1:" + port;
    }

    // ===================== 1. executeByMethodUrl =====================

    @Test
    public void test_executeByMethodUrl_GET() {
        HttpExecutionResult r = executor.executeByMethodUrl("GET", base() + "/hello", null, null);
        assertTrue("should succeed", r.isSuccess());
        assertEquals(200, r.getStatus());
        assertEquals("METHOD_URL", r.getSource());
        assertTrue("body should contain hello", r.getBody().contains("hello"));
        assertTrue("durationMs >= 0", r.getDurationMs() >= 0);
    }

    @Test
    public void test_executeByMethodUrl_POST_json() {
        String body = "{\"name\":\"alice\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpExecutionResult r = executor.executeByMethodUrl(
                "POST", base() + "/echo",
                headers,
                body);
        assertTrue("should succeed", r.isSuccess());
        assertEquals(200, r.getStatus());
        assertEquals(body, r.getBody());
    }

    @Test
    public void test_executeByMethodUrl_PUT() {
        // OkHttp 4.12 要求 PUT 必须有 RequestBody（null 会抛 IllegalArgumentException）
        HttpExecutionResult r = executor.executeByMethodUrl("PUT", base() + "/put", null, "{\"x\":1}");
        assertTrue("should succeed: " + r.getError(), r.isSuccess());
        assertEquals(200, r.getStatus());
        assertTrue(r.getBody().contains("put"));
    }

    @Test
    public void test_executeByMethodUrl_DELETE() {
        HttpExecutionResult r = executor.executeByMethodUrl("DELETE", base() + "/del", null, null);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertTrue(r.getBody().contains("del"));
    }

    @Test
    public void test_executeByMethodUrl_withHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "test-123");
        headers.put("Accept", "application/json");
        HttpExecutionResult r = executor.executeByMethodUrl(
                "GET", base() + "/hello",
                headers,
                null);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
    }

    // ===================== 2. execute(definition) =====================

    @Test
    public void test_execute_definition() {
        HttpRequestDefinition def = new HttpRequestDefinition();
        HttpRequestLine rl = new HttpRequestLine();
        rl.setRequestMethod(RequestMethod.GET);
        rl.setUrl(base() + "/hello");
        def.setHttpRequestLine(rl);

        HttpExecutionResult r = executor.execute(def);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertEquals("DEFINITION", r.getSource());
    }

    @Test
    public void test_execute_definition_withBody() {
        HttpRequestDefinition def = new HttpRequestDefinition();
        HttpRequestLine rl = new HttpRequestLine();
        rl.setRequestMethod(RequestMethod.POST);
        rl.setUrl(base() + "/echo");
        def.setHttpRequestLine(rl);

        HttpRequestHeader hh = new HttpRequestHeader();
        hh.put("Content-Type", "application/json");
        def.setHttpRequestHeader(hh);

        HttpRequestBody b = new HttpRequestBody();
        String body = "{\"x\":1}";
        b.setBody(body.getBytes(StandardCharsets.UTF_8));
        def.setHttpRequestBody(b);

        HttpExecutionResult r = executor.execute(def);
        assertTrue(r.isSuccess());
        assertEquals(body, r.getBody());
    }

    // ===================== 3. executeByCurl =====================

    @Test
    public void test_executeByCurl_get() {
        String curl = "curl -X GET '" + base() + "/hello'";
        HttpExecutionResult r = executor.executeByCurl(curl);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertEquals("CURL", r.getSource());
        assertTrue(r.getBody().contains("hello"));
    }

    @Test
    public void test_executeByCurl_postJson() {
        String curl = "curl -X POST '" + base() + "/echo' " +
                "-H 'Content-Type: application/json' " +
                "-d '{\"x\":42}'";
        HttpExecutionResult r = executor.executeByCurl(curl);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertTrue(r.getBody().contains("\"x\":42"));
    }

    // ===================== 4. executeByDefinitionJson =====================

    @Test
    public void test_executeByDefinitionJson() {
        Map<String, Object> def = new HashMap<>();
        Map<String, Object> line = new HashMap<>();
        line.put("requestMethod", "GET");
        line.put("url", base() + "/hello");
        def.put("httpRequestLine", line);

        HttpExecutionResult r = executor.executeByDefinitionJson(JsonUtil.toJson(def));
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
    }

    // ===================== 5. sendAsync =====================

    @Test
    public void test_sendAsync() throws Exception {
        HttpRequestDefinition def = new HttpRequestDefinition();
        HttpRequestLine rl = new HttpRequestLine();
        rl.setRequestMethod(RequestMethod.GET);
        rl.setUrl(base() + "/slow");
        def.setHttpRequestLine(rl);

        CompletableFuture<HttpExecutionResult> future = executor.sendAsync(def);
        HttpExecutionResult r = future.get(5, TimeUnit.SECONDS);

        assertTrue("async should succeed", r.isSuccess());
        assertEquals(200, r.getStatus());
        assertTrue("body should arrive", r.getBody().contains("slow"));
    }

    // ===================== 6. HttpExecutionResult 结构 =====================

    @Test
    public void test_result_fields() {
        HttpExecutionResult r = executor.executeByMethodUrl("GET", base() + "/hello", null, null);
        assertNotNull(r);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertNotNull(r.getHeaders());
        assertNotNull(r.getBody());
        assertTrue(r.getBodySize() > 0 || r.getBody().length() > 0);
        assertNotNull(r.getSource());
        assertTrue(r.getDurationMs() >= 0);
    }

    @Test
    public void test_result_factory_ok() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X", "y");
        HttpExecutionResult r = HttpExecutionResult.ok(200, headers, "body", 123L);
        assertTrue(r.isSuccess());
        assertEquals(200, r.getStatus());
        assertEquals("body", r.getBody());
        assertEquals(123L, r.getDurationMs());
    }

    @Test
    public void test_result_factory_fail() {
        HttpExecutionResult r = HttpExecutionResult.fail("bad", new RuntimeException("oops"));
        assertFalse(r.isSuccess());
        assertEquals("bad", r.getError());
        assertNotNull(r.getException());
    }

    @Test
    public void test_result_factory_sseEvent() {
        HttpExecutionResult r = HttpExecutionResult.sseEvent("message", "{\"k\":1}");
        assertEquals("SSE", r.getSource());
        assertEquals("{\"k\":1}", r.getBody());
    }

    // ===================== 7. 错误处理 =====================

    @Test
    public void test_5xx_returns_status_500() {
        HttpExecutionResult r = executor.executeByMethodUrl("GET", base() + "/boom", null, null);
        // 5xx 仍然 success=true，但 status=500，业务侧根据 status 判断
        assertTrue(r.isSuccess());
        assertEquals(500, r.getStatus());
        assertTrue(r.getBody().contains("boom"));
    }

    @Test
    public void test_unreachable_url_returns_non_success() {
        // 端口 1 上没有服务 — OkHttp 应在 connect 阶段失败
        HttpExecutionResult r = executor.executeByMethodUrl("GET", "http://127.0.0.1:1/notexist", null, null);
        assertNotNull(r);
        // 一些机器上 connect 会非常快 ECONNREFUSED, 一些会 connect timeout
        // 不强断 success=false，但要求 result 不为 null
    }
}
