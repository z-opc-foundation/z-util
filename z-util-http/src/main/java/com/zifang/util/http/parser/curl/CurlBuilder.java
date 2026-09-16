package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;

import java.nio.charset.StandardCharsets;

/**
 * cURL 命令构建器
 * 将 HttpRequestDefinition 转换为 cURL 命令文本
 */
public class CurlBuilder {

    private static final String INDENT = "  ";

    /**
     * 将 HttpRequestDefinition 转换为 cURL 命令
     *
     * @param definition HTTP 请求定义
     * @return cURL 命令文本
     */
    public static String build(HttpRequestDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("HttpRequestDefinition cannot be null");
        }

        StringBuilder curl = new StringBuilder("curl");

        // 添加请求方法
        HttpRequestLine requestLine = definition.getHttpRequestLine();
        if (requestLine != null && requestLine.getRequestMethod() != null) {
            RequestMethod method = requestLine.getRequestMethod();
            if (method != RequestMethod.GET) {
                curl.append(" ").append(escapeForShell("-X")).append(" ").append(escapeForShell(method.name()));
            }
        }

        // 添加请求头
        HttpRequestHeader headers = definition.getHttpRequestHeader();
        if (headers != null) {
            for (java.util.Map.Entry<String, String> entry : headers.entrySet()) {
                String headerValue = entry.getKey() + ": " + entry.getValue();
                curl.append(" ").append(escapeForShell("-H")).append(" ").append(escapeForShell(headerValue));
            }
        }

        // 添加请求体
        HttpRequestBody body = definition.getHttpRequestBody();
        if (body != null && body.getBody() != null) {
            String bodyString = new String(body.getBody(), StandardCharsets.UTF_8);
            curl.append(" ").append(escapeForShell("-d")).append(" ").append(escapeForShell(bodyString));
        }

        // 添加 URL
        if (requestLine != null && requestLine.getUrl() != null) {
            curl.append(" ").append(escapeForShell(requestLine.getUrl()));
        }

        return curl.toString();
    }

    /**
     * 将 HttpRequestDefinition 转换为格式化的 cURL 命令（多行形式）
     *
     * @param definition HTTP 请求定义
     * @return 格式化的 cURL 命令文本
     */
    public static String buildPretty(HttpRequestDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("HttpRequestDefinition cannot be null");
        }

        StringBuilder curl = new StringBuilder("curl");

        // 添加请求方法
        HttpRequestLine requestLine = definition.getHttpRequestLine();
        if (requestLine != null && requestLine.getRequestMethod() != null) {
            RequestMethod method = requestLine.getRequestMethod();
            if (method != RequestMethod.GET) {
                curl.append(" \\\n").append(INDENT).append(escapeForShell("-X")).append(" ").append(escapeForShell(method.name()));
            }
        }

        // 添加请求头
        HttpRequestHeader headers = definition.getHttpRequestHeader();
        if (headers != null) {
            for (java.util.Map.Entry<String, String> entry : headers.entrySet()) {
                String headerValue = entry.getKey() + ": " + entry.getValue();
                curl.append(" \\\n").append(INDENT).append(escapeForShell("-H")).append(" ").append(escapeForShell(headerValue));
            }
        }

        // 添加请求体
        HttpRequestBody body = definition.getHttpRequestBody();
        if (body != null && body.getBody() != null) {
            String bodyString = new String(body.getBody(), StandardCharsets.UTF_8);
            curl.append(" \\\n").append(INDENT).append(escapeForShell("-d")).append(" ").append(escapeForShell(bodyString));
        }

        // 添加 URL
        if (requestLine != null && requestLine.getUrl() != null) {
            curl.append(" \\\n").append(INDENT).append(escapeForShell(requestLine.getUrl()));
        }

        return curl.toString();
    }

    /**
     * 为 shell 转义字符串
     */
    private static String escapeForShell(String input) {
        if (input == null) {
            return "''";
        }

        // 如果包含特殊字符，使用单引号包裹
        if (input.contains(" ") || input.contains("'") || input.contains("$") ||
                input.contains("&") || input.contains("|") || input.contains(";") ||
                input.contains("(") || input.contains(")") || input.contains("<") ||
                input.contains(">") || input.contains("`") || input.contains("\\") ||
                input.contains("\"") || input.contains("\n") || input.contains("\t")) {

            // 将单引号替换为 '\''
            String escaped = input.replace("'", "'\\''");
            return "'" + escaped + "'";
        }

        return input;
    }

}
