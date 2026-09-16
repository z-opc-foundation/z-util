package com.zifang.util.http.client;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;
import com.zifang.util.http.parser.curl.CurlParser;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 统一 HTTP 执行器 - z-util-http 的核心执行入口
 * <p>
 * 设计目标: 取代分散的 OkHttpUtil、HttpRequestProducer、HttpRequestProxy 各自为政的局面。
 * 所有 HTTP 执行都走这里，对外暴露统一的 HttpExecutionResult。
 * <p>
 * 4 个入口:
 * 1. execute(definition)              - 直接拿 HttpRequestDefinition 跑
 * 2. executeByCurl(curl)              - 用 curl 字符串跑（CurlParser 转换）
 * 3. executeByMethodUrl(method, url)  - 用 method+url 直接跑（最快路径）
 * 4. executeByDefinitionString(json)  - 用 JSON 字符串形式的 HttpRequestDefinition 跑
 * <p>
 * 4 种执行模式:
 * - send(definition)                  - 同步
 * - sendAsync(definition)             - 异步（CompletableFuture）
 * - sendSse(definition, consumer)     - SSE 流
 * - sendUpload(definition, file)      - 文件上传
 * <p>
 * 全部走 OkHttp（自带连接池、超时、连接复用），不再用 HttpURLConnection 或 Apache HttpClient。
 */
public class HttpExecutor {

    private static final Logger log = LoggerFactory.getLogger(HttpExecutor.class);

    /**
     * 共享 OkHttpClient（自带连接池和默认超时）
     */
    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .build();
    private static final HttpExecutor DEFAULT = new HttpExecutor();
    private final OkHttpClient client;

    public HttpExecutor() {
        this.client = DEFAULT_CLIENT;
    }

    // =================================================================
    // 4 个入口
    // =================================================================

    public HttpExecutor(OkHttpClient client) {
        this.client = client == null ? DEFAULT_CLIENT : client;
    }

    public static HttpExecutor getDefault() {
        return DEFAULT;
    }

    /**
     * 入口1: 直接拿 HttpRequestDefinition 跑（最常用）
     */
    public HttpExecutionResult execute(HttpRequestDefinition def) {
        long start = System.currentTimeMillis();
        try {
            Request req = buildRequest(def);
            return doCall(req, "DEFINITION", start);
        } catch (Exception e) {
            log.error("Execute failed", e);
            return HttpExecutionResult.fail("Execute failed: " + e.getMessage(), e);
        }
    }

    /**
     * 入口2: curl 字符串直接跑（CurlParser → HttpRequestDefinition → execute）
     */
    public HttpExecutionResult executeByCurl(String curlCommand) {
        try {
            HttpRequestDefinition def = CurlParser.parse(curlCommand);
            HttpExecutionResult r = execute(def);
            r.setSource("CURL");
            return r;
        } catch (Exception e) {
            return HttpExecutionResult.fail("Curl parse/execute failed: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // 4 种执行模式
    // =================================================================

    /**
     * 入口3: 用 method+url 直接跑（最快路径）
     */
    public HttpExecutionResult executeByMethodUrl(String method, String url,
                                                  Map<String, String> headers, String body) {
        HttpRequestDefinition def = new HttpRequestDefinition();
        HttpRequestLine rl = new HttpRequestLine();
        try {
            rl.setRequestMethod(RequestMethod.valueOf(method == null ? "GET" : method.toUpperCase()));
        } catch (Exception e) {
            rl.setRequestMethod(RequestMethod.GET);
        }
        rl.setUrl(url);
        def.setHttpRequestLine(rl);

        if (headers != null && !headers.isEmpty()) {
            HttpRequestHeader hh = new HttpRequestHeader();
            for (Map.Entry<String, String> e : headers.entrySet()) hh.put(e.getKey(), e.getValue());
            def.setHttpRequestHeader(hh);
        }
        if (body != null && !body.isEmpty()) {
            HttpRequestBody b = new HttpRequestBody();
            b.setBody(body.getBytes(StandardCharsets.UTF_8));
            def.setHttpRequestBody(b);
        }

        HttpExecutionResult r = execute(def);
        r.setSource("METHOD_URL");
        return r;
    }

    /**
     * 入口4: 用 JSON 字符串形式的 HttpRequestDefinition 跑（适合 RPC 调度）
     */
    public HttpExecutionResult executeByDefinitionJson(String json) {
        try {
            HttpRequestDefinition def = com.zifang.util.json.JsonUtil.fromJson(json, HttpRequestDefinition.class);
            if (def == null) {
                return HttpExecutionResult.fail("Definition JSON parse to null", "PARSE_ERROR");
            }
            return execute(def);
        } catch (Exception e) {
            return HttpExecutionResult.fail("Definition JSON parse/execute failed: " + e.getMessage(), e);
        }
    }

    /**
     * 同步
     */
    public HttpExecutionResult send(HttpRequestDefinition def) {
        return execute(def);
    }

    /**
     * 异步
     */
    public CompletableFuture<HttpExecutionResult> sendAsync(HttpRequestDefinition def) {
        return CompletableFuture.supplyAsync(() -> execute(def));
    }

    // =================================================================
    // 内部 - 真正执行 OkHttp 调用
    // =================================================================

    /**
     * SSE 流 - 持续回调直到连接关闭
     */
    public void sendSse(HttpRequestDefinition def, Consumer<HttpExecutionResult> onEvent) {
        long start = System.currentTimeMillis();
        try {
            Request req = buildRequest(def);
            Request.Builder rb = req.newBuilder()
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache");
            Request sseReq = rb.build();

            EventSource.Factory factory = EventSources.createFactory(client);
            factory.newEventSource(sseReq, new EventSourceListener() {
                @Override
                public void onEvent(EventSource es, String id, String type, String data) {
                    HttpExecutionResult ev = HttpExecutionResult.sseEvent(type, data);
                    ev.setDurationMs(System.currentTimeMillis() - start);
                    ev.setSource("SSE");
                    try {
                        onEvent.accept(ev);
                    } catch (Exception ignore) {
                    }
                }

                @Override
                public void onClosed(EventSource es) {
                    log.info("SSE closed");
                }

                @Override
                public void onFailure(EventSource es, Throwable t, Response r) {
                    HttpExecutionResult err = HttpExecutionResult.fail("SSE failure: " + (t == null ? "?" : t.getMessage()), t);
                    err.setStatus(r == null ? 0 : r.code());
                    err.setSource("SSE");
                    try {
                        onEvent.accept(err);
                    } catch (Exception ignore) {
                    }
                }
            });
        } catch (Exception e) {
            HttpExecutionResult err = HttpExecutionResult.fail("SSE init failed: " + e.getMessage(), e);
            err.setSource("SSE");
            onEvent.accept(err);
        }
    }

    /**
     * 文件上传
     */
    public HttpExecutionResult sendUpload(HttpRequestDefinition def, String fileField, File file) {
        long start = System.currentTimeMillis();
        try {
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
            MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (def.getHttpRequestBody() != null && def.getHttpRequestBody().getBody() != null) {
                String text = new String(def.getHttpRequestBody().getBody(), StandardCharsets.UTF_8);
                if (text.startsWith("{") && text.endsWith("}")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) (Map) com.zifang.util.json.JsonUtil.parseObject(text);
                    if (map != null) for (Map.Entry<String, Object> e : map.entrySet()) {
                        mb.addFormDataPart(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
                    }
                }
            }
            mb.addFormDataPart(fileField, file.getName(), fileBody);

            Request.Builder rb = new Request.Builder().url(def.getHttpRequestLine().getUrl()).post(mb.build());
            if (def.getHttpRequestHeader() != null) {
                for (Map.Entry<String, String> e : def.getHttpRequestHeader().entrySet()) {
                    rb.header(e.getKey(), e.getValue());
                }
            }
            Request req = rb.build();
            return doCall(req, "UPLOAD", start);
        } catch (Exception e) {
            return HttpExecutionResult.fail("Upload failed: " + e.getMessage(), e);
        }
    }

    private Request buildRequest(HttpRequestDefinition def) {
        String url = def.getHttpRequestLine() == null ? null : def.getHttpRequestLine().getUrl();
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("HttpRequestDefinition.url is required");
        }
        RequestMethod m = def.getHttpRequestLine().getRequestMethod();
        String method = m == null ? "GET" : m.name();
        RequestBody body = null;

        boolean bodyMethod = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
        if (def.getHttpRequestBody() != null && def.getHttpRequestBody().getBody() != null && bodyMethod) {
            String text = new String(def.getHttpRequestBody().getBody(), StandardCharsets.UTF_8);
            MediaType mt = MediaType.parse(detectContentType(text));
            body = RequestBody.create(text, mt);
        } else if (bodyMethod) {
            // OkHttp 4.12 严格要求 POST/PUT/PATCH/DELETE 必须有非空 RequestBody。
            // 调用方没传 body 时，回退到一个 0 字节 body，避免 IllegalArgumentException。
            body = RequestBody.create(new byte[0], null);
        }

        Request.Builder rb = new Request.Builder().url(url);
        if (body != null) rb.method(method, body);
        else rb.method(method, null);

        if (def.getHttpRequestHeader() != null) {
            for (Map.Entry<String, String> e : def.getHttpRequestHeader().entrySet()) {
                rb.header(e.getKey(), e.getValue());
            }
        }

        return rb.build();
    }

    // =================================================================
    // 单例访问（避免每个调用方都 new 一个）
    // =================================================================

    private HttpExecutionResult doCall(Request req, String source, long start) throws IOException {
        try (Response res = client.newCall(req).execute()) {
            Map<String, String> headers = new LinkedHashMap<>();
            for (String name : res.headers().names()) {
                String v = res.header(name);
                if (v != null) headers.put(name, v);
            }
            String body = res.body() == null ? "" : res.body().string();
            HttpExecutionResult r = HttpExecutionResult.ok(res.code(), headers, body, System.currentTimeMillis() - start);
            r.setSource(source);
            if (res.priorResponse() != null) {
                List<String> chain = new ArrayList<>();
                Response p = res.priorResponse();
                while (p != null) {
                    chain.add(p.request().url().toString());
                    p = p.priorResponse();
                }
                r.setRedirectChain(chain);
            }
            return r;
        }
    }

    private String detectContentType(String body) {
        if (body == null) return "text/plain; charset=utf-8";
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) return "application/json; charset=utf-8";
        if (trimmed.startsWith("<")) return "application/xml; charset=utf-8";
        return "text/plain; charset=utf-8";
    }
}
