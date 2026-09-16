package com.zifang.util.http.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一 HTTP 执行结果 - 任何 HTTP 调用最终都返回这个
 * <p>
 * 不管是:
 * - 同步执行
 * - 异步执行
 * - SSE 流式
 * - 文件上传
 * - 直接 HttpRequestDefinition
 * - curl 字符串
 * - method+url
 * 都会归一化到此结果，调用方可以无差别处理。
 * <p>
 * 与 HttpClientResult 的区别:
 * - HttpClientResult: 只有 code + content（旧式，最小信息集）
 * - HttpExecutionResult: 完整信息集（headers/body/status/duration/error/source/redirectChain/...）
 * <p>
 * 这是 z-util-http 的标准结果类型。任何基于 z-util-http 的上层应用（z-script、z-mock 等）
 * 都应返回或包装此类型，而不是自己定义 Result POJO。
 */
public class HttpExecutionResult {

    private boolean success;
    private int status;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;
    private Object bodyObject;
    private long durationMs;
    private String error;
    private String errorType;
    private Throwable exception;
    private String source;
    private List<String> redirectChain;
    private long bodySize;
    private Map<String, Object> context = new LinkedHashMap<>();

    public static HttpExecutionResult ok(int status, Map<String, String> headers, String body, long durationMs) {
        HttpExecutionResult r = new HttpExecutionResult();
        // ok() 表示"HTTP 调用本身完成了"（未抛异常、拿到了 Response）。
        // 状态码语义（2xx/4xx/5xx）是业务侧的事，调用方应根据 r.getStatus() 自行判断。
        // 因此 success 始终为 true，5xx 不会让 isSuccess()=false。
        r.success = true;
        r.status = status;
        r.headers = headers == null ? new LinkedHashMap<>() : headers;
        r.body = body;
        r.durationMs = durationMs;
        r.bodySize = body == null ? 0 : body.getBytes().length;
        return r;
    }

    public static HttpExecutionResult fail(String error, Throwable ex) {
        HttpExecutionResult r = new HttpExecutionResult();
        r.success = false;
        r.error = error;
        r.exception = ex;
        r.errorType = ex == null ? null : ex.getClass().getName();
        return r;
    }

    public static HttpExecutionResult fail(String error, String errorType) {
        HttpExecutionResult r = new HttpExecutionResult();
        r.success = false;
        r.error = error;
        r.errorType = errorType;
        return r;
    }

    public static HttpExecutionResult sseEvent(String event, String data) {
        HttpExecutionResult r = new HttpExecutionResult();
        r.success = true;
        r.status = 200;
        r.source = "SSE";
        r.context.put("event", event);
        r.body = data;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean v) {
        this.success = v;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int v) {
        this.status = v;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> v) {
        this.headers = v;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String v) {
        this.body = v;
        this.bodySize = v == null ? 0 : v.getBytes().length;
    }

    public Object getBodyObject() {
        return bodyObject;
    }

    public void setBodyObject(Object v) {
        this.bodyObject = v;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long v) {
        this.durationMs = v;
    }

    public String getError() {
        return error;
    }

    public void setError(String v) {
        this.error = v;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String v) {
        this.errorType = v;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable v) {
        this.exception = v;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String v) {
        this.source = v;
    }

    public List<String> getRedirectChain() {
        return redirectChain;
    }

    public void setRedirectChain(List<String> v) {
        this.redirectChain = v;
    }

    public long getBodySize() {
        return bodySize;
    }

    public void setBodySize(long v) {
        this.bodySize = v;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> v) {
        this.context = v;
    }
}
