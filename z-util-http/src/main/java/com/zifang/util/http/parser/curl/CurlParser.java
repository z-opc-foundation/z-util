package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * cURL 命令解析器
 * 将 cURL 命令文本解析为 HttpRequestDefinition 标准结构
 */
public class CurlParser {

    // 正则表达式匹配各种 cURL 参数
    private static final Pattern HEADER_PATTERN = Pattern.compile("-H\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern DATA_PATTERN = Pattern.compile("(--data|-d)\\s+(['\"])(.+?)\\2");
    private static final Pattern DATA_RAW_PATTERN = Pattern.compile("--data-raw\\s+(['\"])(.+?)\\1");
    private static final Pattern DATA_BINARY_PATTERN = Pattern.compile("--data-binary\\s+(['\"])(.+?)\\1");
    private static final Pattern USER_PATTERN = Pattern.compile("(-u|--user)\\s+(['\"]?)([^:\\s]+):([^\\s'\"]+)\\2");
    private static final Pattern METHOD_PATTERN = Pattern.compile("-X\\s+(['\"]?)([A-Z]+)\\1");
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(-b|--cookie)\\s+(['\"])([^'\"]+)\\2");
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("Content-Type:\\s*([^;\\s]+)");
    private static final Pattern URL_IN_QUOTES_PATTERN = Pattern.compile("['\"](https?://[^'\"]+)['\"]");
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[^\\s'\"]+)");

    /**
     * 解析 cURL 命令文本
     *
     * @param curlCommand cURL 命令文本
     * @return HttpRequestDefinition HTTP 请求定义
     */
    public static HttpRequestDefinition parse(String curlCommand) {
        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            throw new IllegalArgumentException("cURL command cannot be empty");
        }

        HttpRequestDefinition definition = new HttpRequestDefinition();
        definition.setHttpRequestLine(parseRequestLine(curlCommand));
        definition.setHttpRequestHeader(parseHeaders(curlCommand));
        definition.setHttpRequestBody(parseBody(curlCommand));

        return definition;
    }

    /**
     * 解析请求行
     */
    private static HttpRequestLine parseRequestLine(String curlCommand) {
        HttpRequestLine requestLine = new HttpRequestLine();

        // 解析方法
        RequestMethod method = RequestMethod.GET;
        Matcher methodMatcher = METHOD_PATTERN.matcher(curlCommand);
        if (methodMatcher.find()) {
            String methodStr = methodMatcher.group(2);
            try {
                method = RequestMethod.valueOf(methodStr);
            } catch (IllegalArgumentException e) {
                // 如果不是标准方法，仍然使用 GET
            }
        } else {
            // 如果有 -d 或 --data，默认为 POST
            if (curlCommand.contains(" -d ") || curlCommand.contains(" --data") || curlCommand.contains("--data-")) {
                method = RequestMethod.POST;
            }
        }
        requestLine.setRequestMethod(method);

        // 解析 URL
        String url = extractUrl(curlCommand);
        requestLine.setUrl(url);

        return requestLine;
    }

    /**
     * 提取 URL
     */
    private static String extractUrl(String curlCommand) {
        // 首先尝试匹配引号中的 URL
        Matcher quotedMatcher = URL_IN_QUOTES_PATTERN.matcher(curlCommand);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1);
        }

        // 然后尝试匹配普通 URL
        Matcher urlMatcher = URL_PATTERN.matcher(curlCommand);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }

        throw new IllegalArgumentException("Cannot find URL in cURL command");
    }

    /**
     * 解析请求头
     */
    private static HttpRequestHeader parseHeaders(String curlCommand) {
        HttpRequestHeader headers = new HttpRequestHeader();

        // 解析 -H 参数
        Matcher headerMatcher = HEADER_PATTERN.matcher(curlCommand);
        while (headerMatcher.find()) {
            String headerLine = headerMatcher.group(1);
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String name = headerLine.substring(0, colonIndex).trim();
                String value = headerLine.substring(colonIndex + 1).trim();
                headers.put(name, value);
            }
        }

        // 解析 -u/--user 参数（Basic Auth）
        Matcher userMatcher = USER_PATTERN.matcher(curlCommand);
        if (userMatcher.find()) {
            String username = userMatcher.group(3);
            String password = userMatcher.group(4);
            String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + basicAuth);
        }

        // 解析 -b/--cookie 参数
        Matcher cookieMatcher = COOKIE_PATTERN.matcher(curlCommand);
        if (cookieMatcher.find()) {
            headers.put("Cookie", cookieMatcher.group(3));
        }

        // 处理 Content-Type（从 data 参数推断或从 headers 中提取）
        if (!headers.containsKey("Content-Type")) {
            String contentType = inferContentType(curlCommand, headers);
            if (contentType != null) {
                headers.put("Content-Type", contentType);
            }
        }

        return headers;
    }

    /**
     * 推断 Content-Type
     */
    private static String inferContentType(String curlCommand, HttpRequestHeader headers) {
        // 检查是否有显式的 Content-Type header
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Content-Type")) {
                Matcher ctMatcher = CONTENT_TYPE_PATTERN.matcher(entry.getValue());
                if (ctMatcher.find()) {
                    return ctMatcher.group(1);
                }
            }
        }

        // 根据 data 内容推断
        String data = extractData(curlCommand);
        if (data != null) {
            if (data.trim().startsWith("{") || data.trim().startsWith("[")) {
                return "application/json";
            }
            if (data.trim().startsWith("<")) {
                return "application/xml";
            }
            if (data.contains("=") && !data.contains("{")) {
                return "application/x-www-form-urlencoded";
            }
        }

        return null;
    }

    /**
     * 提取 data 内容
     */
    private static String extractData(String curlCommand) {
        // 匹配 --data 或 -d
        Matcher dataMatcher = DATA_PATTERN.matcher(curlCommand);
        if (dataMatcher.find()) {
            return dataMatcher.group(3);
        }

        // 匹配 --data-raw
        Matcher rawMatcher = DATA_RAW_PATTERN.matcher(curlCommand);
        if (rawMatcher.find()) {
            return rawMatcher.group(2);
        }

        // 匹配 --data-binary
        Matcher binaryMatcher = DATA_BINARY_PATTERN.matcher(curlCommand);
        if (binaryMatcher.find()) {
            return binaryMatcher.group(2);
        }

        return null;
    }

    /**
     * 解析请求体
     */
    private static HttpRequestBody parseBody(String curlCommand) {
        HttpRequestBody body = new HttpRequestBody();

        String data = extractData(curlCommand);
        if (data != null) {
            try {
                // 尝试 URL 解码
                String decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8.name());
                body.setBody(decodedData.getBytes(StandardCharsets.UTF_8));
            } catch (UnsupportedEncodingException e) {
                body.setBody(data.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            body.setBody(null);
        }

        return body;
    }

    /**
     * 解析 cURL 命令列表（多个命令）
     *
     * @param curlCommands cURL 命令列表
     * @return HttpRequestDefinition 列表
     */
    public static List<HttpRequestDefinition> parseList(List<String> curlCommands) {
        List<HttpRequestDefinition> definitions = new ArrayList<>();
        for (String command : curlCommands) {
            if (command != null && !command.trim().isEmpty()) {
                definitions.add(parse(command));
            }
        }
        return definitions;
    }

}
