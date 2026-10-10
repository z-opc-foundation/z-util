package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.pojo.HttpRequestDefinition;

/**
 * cURL 解析器使用示例
 */

/**
 * CurlParserExample类。
 */
public class CurlParserExample {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("cURL 解析器使用示例");
        System.out.println("========================================\n");

        // 示例 1: 简单的 GET 请求
        example1SimpleGet();

        // 示例 2: 带请求头的 GET 请求
        example2GetWithHeaders();

        // 示例 3: POST 请求（JSON 数据）
        example3PostJson();

        // 示例 4: 使用词法分析器解析
        example4LexerParser();

        // 示例 5: 反向转换（HttpRequestDefinition -> cURL）
        example5BuildCurl();

        // 示例 6: 解析多个 cURL 命令
        example6ParseMultiple();

        System.out.println("\n========================================");
        System.out.println("所有示例执行完成！");
        System.out.println("========================================");
    }

    /**
     * 示例 1: 简单的 GET 请求
     */
    private static void example1SimpleGet() {
        System.out.println("【示例 1】简单的 GET 请求");
        System.out.println("------------------------------");

        String curl = "curl https://api.example.com/users";
        System.out.println("原始 cURL:");
        System.out.println(curl);

        HttpRequestDefinition definition = CurlParser.parse(curl);

        System.out.println("\n解析结果:");
        System.out.println("  方法: " + definition.getHttpRequestLine().getRequestMethod());
        System.out.println("  URL: " + definition.getHttpRequestLine().getUrl());
        System.out.println("  请求头数量: " + definition.getHttpRequestHeader().size());
        System.out.println("\n");
    }

    /**
     * 示例 2: 带请求头的 GET 请求
     */
    private static void example2GetWithHeaders() {
        System.out.println("【示例 2】带请求头的 GET 请求");
        System.out.println("------------------------------");

        String curl = "curl -H 'Accept: application/json' -H 'Authorization: Bearer token123' https://api.example.com/users";
        System.out.println("原始 cURL:");
        System.out.println(curl);

        HttpRequestDefinition definition = CurlParser.parse(curl);

        System.out.println("\n解析结果:");
        System.out.println("  方法: " + definition.getHttpRequestLine().getRequestMethod());
        System.out.println("  URL: " + definition.getHttpRequestLine().getUrl());
        System.out.println("  请求头:");
        definition.getHttpRequestHeader().forEach((k, v) -> {
            System.out.println("    " + k + ": " + v);
        });
        System.out.println("\n");
    }

    /**
     * 示例 3: POST 请求（JSON 数据）
     */
    private static void example3PostJson() {
        System.out.println("【示例 3】POST 请求（JSON 数据）");
        System.out.println("------------------------------");

        String curl = "curl -X POST -H 'Content-Type: application/json' -d '{\"name\":\"张三\",\"age\":25}' https://api.example.com/users";
        System.out.println("原始 cURL:");
        System.out.println(curl);

        HttpRequestDefinition definition = CurlParser.parse(curl);

        System.out.println("\n解析结果:");
        System.out.println("  方法: " + definition.getHttpRequestLine().getRequestMethod());
        System.out.println("  URL: " + definition.getHttpRequestLine().getUrl());
        System.out.println("  请求头:");
        definition.getHttpRequestHeader().forEach((k, v) -> {
            System.out.println("    " + k + ": " + v);
        });
        System.out.println("  请求体: " + new String(definition.getHttpRequestBody().getBody()));
        System.out.println("\n");
    }

    /**
     * 示例 4: 使用词法分析器解析
     */
    private static void example4LexerParser() {
        System.out.println("【示例 4】使用词法分析器解析");
        System.out.println("------------------------------");

        String curl = "curl -X POST -H 'Content-Type: application/json' -d '{\"test\":\"data\"}' https://api.example.com/test";
        System.out.println("原始 cURL:");
        System.out.println(curl);

        System.out.println("\n词法分析结果 (Tokens):");
        java.util.List<CurlLexer.Token> tokens = CurlLexer.tokenize(curl);
        for (CurlLexer.Token token : tokens) {
            System.out.println("  " + token);
        }

        System.out.println("\n解析结果:");
        HttpRequestDefinition definition = CurlTokenParser.parse(tokens);
        System.out.println("  方法: " + definition.getHttpRequestLine().getRequestMethod());
        System.out.println("  URL: " + definition.getHttpRequestLine().getUrl());
        System.out.println("\n");
    }

    /**
     * 示例 5: 反向转换（HttpRequestDefinition -> cURL）
     */
    private static void example5BuildCurl() {
        System.out.println("【示例 5】反向转换（HttpRequestDefinition -> cURL）");
        System.out.println("------------------------------");

        // 首先解析一个 cURL 命令
        String originalCurl = "curl -X POST -H 'Content-Type: application/json' -H 'Authorization: Bearer token' -d '{\"name\":\"test\"}' https://api.example.com/users";
        System.out.println("原始 cURL:");
        System.out.println(originalCurl);

        HttpRequestDefinition definition = CurlParser.parse(originalCurl);

        // 转换回 cURL 命令
        String rebuiltCurl = CurlBuilder.build(definition);
        System.out.println("\n重建的 cURL（单行）:");
        System.out.println(rebuiltCurl);

        // 格式化输出
        String prettyCurl = CurlBuilder.buildPretty(definition);
        System.out.println("\n重建的 cURL（格式化）:");
        System.out.println(prettyCurl);

        // 验证重建的命令可以正确解析
        HttpRequestDefinition reparsed = CurlParser.parse(rebuiltCurl);
        System.out.println("\n验证重建命令:");
        System.out.println("  原始方法: " + definition.getHttpRequestLine().getRequestMethod());
        System.out.println("  重建方法: " + reparsed.getHttpRequestLine().getRequestMethod());
        System.out.println("  匹配: " + definition.getHttpRequestLine().getRequestMethod().equals(reparsed.getHttpRequestLine().getRequestMethod()));
        System.out.println("\n");
    }

    /**
     * 示例 6: 解析多个 cURL 命令
     */
    private static void example6ParseMultiple() {
        System.out.println("【示例 6】解析多个 cURL 命令");
        System.out.println("------------------------------");

        String multipleCurls =
                "curl -X GET https://api.example.com/users\n" +
                        "curl -X POST -H 'Content-Type: application/json' -d '{\"name\":\"test\"}' https://api.example.com/users\n" +
                        "curl -X DELETE https://api.example.com/users/123";

        System.out.println("原始多个 cURL 命令:");
        System.out.println(multipleCurls);

        java.util.List<HttpRequestDefinition> definitions = CurlParserUtils.parseMultiple(multipleCurls);

        System.out.println("解析结果（共 " + definitions.size() + " 个请求）:");
        for (int i = 0; i < definitions.size(); i++) {
            HttpRequestDefinition def = definitions.get(i);
            System.out.println("\n  [" + (i + 1) + "]");
            System.out.println("    方法: " + def.getHttpRequestLine().getRequestMethod());
            System.out.println("    URL: " + def.getHttpRequestLine().getUrl());
        }
        System.out.println("\n");
    }

}
