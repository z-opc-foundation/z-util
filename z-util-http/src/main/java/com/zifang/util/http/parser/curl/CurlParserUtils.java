package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.pojo.HttpRequestDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * cURL 解析工具类
 * 提供便捷的解析方法
 */
public class CurlParserUtils {

    // 匹配多个连续 cURL 命令的正则（以 curl 开头，直到下一个 curl 或结束）
    private static final Pattern MULTI_CURL_PATTERN = Pattern.compile(
            "(curl\\s+.*?)(?=\\s*curl\\s|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 解析单个 cURL 命令
     *
     * @param curlCommand cURL 命令文本
     * @return HttpRequestDefinition
     */
    public static HttpRequestDefinition parse(String curlCommand) {
        return CurlParser.parse(curlCommand);
    }

    /**
     * 解析多个 cURL 命令
     *
     * @param curlCommands 多个 cURL 命令文本
     * @return HttpRequestDefinition 列表
     */
    public static List<HttpRequestDefinition> parseMultiple(String curlCommands) {
        List<HttpRequestDefinition> definitions = new ArrayList<>();

        if (curlCommands == null || curlCommands.trim().isEmpty()) {
            return definitions;
        }

        // 尝试匹配多个 curl 命令
        Matcher matcher = MULTI_CURL_PATTERN.matcher(curlCommands);
        while (matcher.find()) {
            String command = matcher.group(1).trim();
            if (!command.isEmpty()) {
                try {
                    definitions.add(CurlParser.parse(command));
                } catch (Exception e) {
                    // 记录解析失败的命令，但继续处理其他命令
                    System.err.println("Failed to parse cURL command: " + command.substring(0, Math.min(50, command.length())) + "...");
                }
            }
        }

        // 如果没有匹配到多个命令，尝试作为单个命令解析
        if (definitions.isEmpty()) {
            try {
                definitions.add(CurlParser.parse(curlCommands));
            } catch (Exception e) {
                throw new CurlParseException("Failed to parse cURL command", e);
            }
        }

        return definitions;
    }

    /**
     * 将 HttpRequestDefinition 转换为 cURL 命令
     *
     * @param definition HTTP 请求定义
     * @return cURL 命令文本
     */
    public static String toCurlCommand(HttpRequestDefinition definition) {
        return CurlBuilder.build(definition);
    }

    /**
     * 将 HttpRequestDefinition 转换为格式化的 cURL 命令
     *
     * @param definition HTTP 请求定义
     * @return 格式化的 cURL 命令文本
     */
    public static String toPrettyCurlCommand(HttpRequestDefinition definition) {
        return CurlBuilder.buildPretty(definition);
    }

    /**
     * 验证 cURL 命令是否有效
     *
     * @param curlCommand cURL 命令文本
     * @return 是否有效
     */
    public static boolean isValid(String curlCommand) {
        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            return false;
        }

        try {
            CurlParser.parse(curlCommand);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取 cURL 命令中的 URL
     *
     * @param curlCommand cURL 命令文本
     * @return URL 字符串，如果不存在返回 null
     */
    public static String extractUrl(String curlCommand) {
        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            return null;
        }

        try {
            HttpRequestDefinition definition = CurlParser.parse(curlCommand);
            if (definition.getHttpRequestLine() != null) {
                return definition.getHttpRequestLine().getUrl();
            }
        } catch (Exception e) {
            // 解析失败，返回 null
        }

        return null;
    }

}
