package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.pojo.HttpRequestDefinition;

import java.util.List;

/**
 * cURL 解析器工厂
 * 提供统一的解析器实例获取方式
 */
public class CurlParserFactory {

    private CurlParserFactory() {
        // 工具类，禁止实例化
    }

    /**
     * 获取默认解析器（基于正则表达式）
     *
     * @return 解析后的 HttpRequestDefinition
     */
    public static HttpRequestDefinition parse(String curlCommand) {
        return CurlParser.parse(curlCommand);
    }

    /**
     * 使用词法分析器解析
     *
     * @param curlCommand cURL 命令文本
     * @return 解析后的 HttpRequestDefinition
     */
    public static HttpRequestDefinition parseWithLexer(String curlCommand) {
        List<CurlLexer.Token> tokens = CurlLexer.tokenize(curlCommand);
        return CurlTokenParser.parse(tokens);
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

}
