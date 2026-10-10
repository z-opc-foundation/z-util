package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * cURL Token 解析器
 * 将词法分析后的 token 列表解析为 HttpRequestDefinition
 */
public class CurlTokenParser {

    /**
     * 从 token 列表解析 HttpRequestDefinition
     *
     * @param tokens token 列表
     * @return HttpRequestDefinition
     */
    public static HttpRequestDefinition parse(List<CurlLexer.Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Token list cannot be empty");
        }

        HttpRequestDefinition definition = new HttpRequestDefinition();

        // 解析请求行
        definition.setHttpRequestLine(parseRequestLine(tokens));

        // 解析请求头
        definition.setHttpRequestHeader(parseHeaders(tokens));

        // 解析请求体
        definition.setHttpRequestBody(parseBody(tokens));

        return definition;
    }

    /**
     * 解析请求行
     */
    private static HttpRequestLine parseRequestLine(List<CurlLexer.Token> tokens) {
        HttpRequestLine requestLine = new HttpRequestLine();

        // 解析方法
        RequestMethod method = RequestMethod.GET;
        String methodStr = findOptionValue(tokens, "-X", "--request");
        if (methodStr != null) {
            try {
                method = RequestMethod.valueOf(methodStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果不是标准方法，保持 GET
            }
        } else {
            // 如果有 -d 或 --data，默认为 POST
            if (hasOption(tokens, "-d", "--data", "--data-raw", "--data-binary", "--data-urlencode")) {
                method = RequestMethod.POST;
            }
        }
        requestLine.setRequestMethod(method);

        // 解析 URL
        String url = extractUrl(tokens);
        requestLine.setUrl(url);

        return requestLine;
    }

    /**
     * 从 token 中提取 URL
     */
    private static String extractUrl(List<CurlLexer.Token> tokens) {
        // 首先查找显式的 URL token（以 http:// 或 https:// 开头）
        for (CurlLexer.Token token : tokens) {
            if (token.type == CurlLexer.TokenType.VALUE &&
                    (token.value.startsWith("http://") || token.value.startsWith("https://"))) {
                return token.value;
            }
        }

        // 如果没有找到 URL，抛出异常
        throw new CurlParseException("Cannot find URL in cURL command");
    }

    /**
     * 查找选项的值
     */
    private static String findOptionValue(List<CurlLexer.Token> tokens, String... optionNames) {
        for (int i = 0; i < tokens.size(); i++) {
            CurlLexer.Token token = tokens.get(i);
            if (token.type == CurlLexer.TokenType.OPTION_SHORT ||
                    token.type == CurlLexer.TokenType.OPTION_LONG) {

                for (String optionName : optionNames) {
                    if (token.value.equals(optionName) && i + 1 < tokens.size()) {
                        CurlLexer.Token nextToken = tokens.get(i + 1);
                        if (nextToken.type == CurlLexer.TokenType.VALUE) {
                            return nextToken.value;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检查是否存在某个选项
     */
    private static boolean hasOption(List<CurlLexer.Token> tokens, String... optionNames) {
        for (CurlLexer.Token token : tokens) {
            if (token.type == CurlLexer.TokenType.OPTION_SHORT ||
                    token.type == CurlLexer.TokenType.OPTION_LONG) {
                for (String optionName : optionNames) {
                    if (token.value.equals(optionName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 解析请求头
     */
    private static HttpRequestHeader parseHeaders(List<CurlLexer.Token> tokens) {
        HttpRequestHeader headers = new HttpRequestHeader();

        // 解析 -H/--header 选项
        for (int i = 0; i < tokens.size(); i++) {
            CurlLexer.Token token = tokens.get(i);
            if (token.type == CurlLexer.TokenType.OPTION_SHORT ||
                    token.type == CurlLexer.TokenType.OPTION_LONG) {

                if (token.value.equals("-H") || token.value.equals("--header")) {
                    if (i + 1 < tokens.size() && tokens.get(i + 1).type == CurlLexer.TokenType.VALUE) {
                        String headerLine = tokens.get(i + 1).value;
                        int colonIndex = headerLine.indexOf(':');
                        if (colonIndex > 0) {
                            String name = headerLine.substring(0, colonIndex).trim();
                            String value = headerLine.substring(colonIndex + 1).trim();
                            headers.put(name, value);
                        }
                    }
                }
            }
        }

        // 解析 -u/--user 参数（Basic Auth）
        String userValue = findOptionValue(tokens, "-u", "--user");
        if (userValue != null) {
            String[] parts = userValue.split(":", 2);
            if (parts.length == 2) {
                String basicAuth = Base64.getEncoder()
                        .encodeToString((parts[0] + ":" + parts[1]).getBytes(StandardCharsets.UTF_8));
                headers.put("Authorization", "Basic " + basicAuth);
            }
        }

        // 解析 -b/--cookie 参数
        String cookieValue = findOptionValue(tokens, "-b", "--cookie");
        if (cookieValue != null) {
            headers.put("Cookie", cookieValue);
        }

        // 如果没有 Content-Type 但有请求体，尝试推断
        if (!headers.containsKey("Content-Type") && hasRequestBody(tokens)) {
            String contentType = inferContentType(tokens);
            if (contentType != null) {
                headers.put("Content-Type", contentType);
            }
        }

        return headers;
    }

    /**
     * 检查是否有请求体
     */
    private static boolean hasRequestBody(List<CurlLexer.Token> tokens) {
        return hasOption(tokens, "-d", "--data", "--data-raw", "--data-binary", "--data-urlencode");
    }

    /**
     * 推断 Content-Type
     */
    private static String inferContentType(List<CurlLexer.Token> tokens) {
        String data = findDataValue(tokens);
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
     * 查找 data 值
     */
    private static String findDataValue(List<CurlLexer.Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            CurlLexer.Token token = tokens.get(i);
            if (token.type == CurlLexer.TokenType.OPTION_SHORT ||
                    token.type == CurlLexer.TokenType.OPTION_LONG) {

                if (token.value.equals("-d") || token.value.equals("--data") ||
                        token.value.equals("--data-raw") || token.value.equals("--data-binary")) {
                    if (i + 1 < tokens.size() && tokens.get(i + 1).type == CurlLexer.TokenType.VALUE) {
                        return tokens.get(i + 1).value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 解析请求体
     */
    private static HttpRequestBody parseBody(List<CurlLexer.Token> tokens) {
        HttpRequestBody body = new HttpRequestBody();

        String data = findDataValue(tokens);
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

}
