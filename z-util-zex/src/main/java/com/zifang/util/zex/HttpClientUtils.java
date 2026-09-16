package com.zifang.util.zex;

import com.zifang.util.http.client.HttpExecutionResult;
import com.zifang.util.http.client.HttpExecutor;

import java.net.URLEncoder;
import java.util.Map;

/**
 * HTTP 客户端工具类。
 * <p>
 * 提供 GET/POST/PUT/DELETE 等常用 HTTP 请求方法。
 * 内部统一走 z-util-http 的 {@link HttpExecutor}（OkHttp 实现），与 z-opc 全栈保持一致。
 * <p>
 * 编码：UTF-8。
 *
 * @author zifang
 * @version 2.0  // migrated from Apache HttpClient to HttpExecutor
 */
public class HttpClientUtils {

    /**
     * 发送 GET 请求，不带请求头和请求参数。
     */
    public static HttpExecutionResult doGet(String url) throws Exception {
        return doGet(url, null, null);
    }

    /**
     * 发送 GET 请求，带请求参数。
     */
    public static HttpExecutionResult doGet(String url, Map<String, String> params) throws Exception {
        return doGet(url, null, params);
    }

    /**
     * 发送 GET 请求，带请求头和请求参数。
     */
    public static HttpExecutionResult doGet(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        String fullUrl = appendQueryString(url, params);
        return HttpExecutor.getDefault().executeByMethodUrl("GET", fullUrl, headers, null);
    }

    /**
     * 发送 POST 请求，不带请求头和请求参数。
     */
    public static HttpExecutionResult doPost(String url) throws Exception {
        return doPost(url, null, null);
    }

    /**
     * 发送 POST 请求，带请求参数（form-encoded）。
     */
    public static HttpExecutionResult doPost(String url, Map<String, String> params) throws Exception {
        return doPost(url, null, params);
    }

    /**
     * 发送 POST 请求，带请求头和请求参数（form-encoded）。
     */
    public static HttpExecutionResult doPost(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        String body = formEncode(params);
        Map<String, String> hh = withContentType(headers, "application/x-www-form-urlencoded");
        return HttpExecutor.getDefault().executeByMethodUrl("POST", url, hh, body);
    }

    /**
     * 发送 PUT 请求，不带请求参数。
     */
    public static HttpExecutionResult doPut(String url) throws Exception {
        return doPut(url, null);
    }

    /**
     * 发送 PUT 请求，带请求参数（form-encoded）。
     */
    public static HttpExecutionResult doPut(String url, Map<String, String> params) throws Exception {
        String body = formEncode(params);
        Map<String, String> hh = withContentType(null, "application/x-www-form-urlencoded");
        return HttpExecutor.getDefault().executeByMethodUrl("PUT", url, hh, body);
    }

    /**
     * 发送 DELETE 请求，不带请求参数。
     */
    public static HttpExecutionResult doDelete(String url) throws Exception {
        return HttpExecutor.getDefault().executeByMethodUrl("DELETE", url, null, null);
    }

    /**
     * 发送 DELETE 请求，带请求参数。
     * <p>
     * 兼容旧实现：把 _method=delete 写入 form 参数，底层走 POST。
     * 这是因为部分老后端只接受 form-encoded + POST 来实现"伪 DELETE"。
     */
    public static HttpExecutionResult doDelete(String url, Map<String, String> params) throws Exception {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        params.put("_method", "delete");
        return doPost(url, params);
    }

    // ===================== 内部工具 =====================

    /**
     * URL 编码（UTF-8）。Java 8 的 URLEncoder.encode(String, String) 声明了
     * checked UnsupportedEncodingException，UTF-8 始终支持，所以包成 unchecked。
     */
    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 不可用", e);
        }
    }

    private static String appendQueryString(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) return url;
        StringBuilder sb = new StringBuilder(url);
        sb.append(url.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append("&");
            sb.append(urlEncode(e.getKey()));
            sb.append("=");
            sb.append(urlEncode(e.getValue() == null ? "" : e.getValue()));
            first = false;
        }
        return sb.toString();
    }

    private static String formEncode(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append("&");
            sb.append(urlEncode(e.getKey()));
            sb.append("=");
            sb.append(urlEncode(e.getValue() == null ? "" : e.getValue()));
            first = false;
        }
        return sb.toString();
    }

    private static Map<String, String> withContentType(Map<String, String> headers, String contentType) {
        java.util.LinkedHashMap<String, String> m = new java.util.LinkedHashMap<>();
        if (headers != null) m.putAll(headers);
        m.put("Content-Type", contentType);
        return m;
    }
}
