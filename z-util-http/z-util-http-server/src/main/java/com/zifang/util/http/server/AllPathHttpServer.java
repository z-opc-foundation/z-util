package com.zifang.util.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全路径HTTP服务器
 * <p>
 * 一个简单的HTTP服务器示例，演示如何使用 {@link HttpServer} 处理所有路径的请求。
 * 该服务器监听在指定端口，接收并响应所有进入的HTTP请求。
 * </p>
 *
 * @author zifang
 * @see HttpServer
 * @see HttpHandler
 */
public class AllPathHttpServer {

    /**
     * 主方法，启动HTTP服务器。
     * <p>
     * 默认使用端口8848，绑定到所有路径（"/"）以接收所有请求。
     * </p>
     *
     * @param args 命令行参数（未使用）
     * @throws IOException 如果服务器创建或启动失败
     */
    public static void main(String[] args) throws IOException {
        int port = 8848;
        // 创建HTTP服务器，绑定到8080端口
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 注册全局处理器：空字符串""匹配所有路径
        server.createContext("/", new AllRequestHandler());

        // 启动服务器
        server.start();

    }

    // 处理所有路径和所有HTTP方法的处理器

    // 工具方法：手动读取输入流（替代Java 9+的readAllBytes()）
    private static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 缓冲区大小，可调整
        int len;
        // 循环读取流内容到缓冲区
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close(); // 关闭输入流
        return baos.toByteArray(); // 转换为字节数组返回
    }

    // 工具方法：解析参数（GET取URL参数，其他方法取请求体）
    private static Map<String, String> parseParams(String method, String query, String body) {
        Map<String, String> params = new HashMap<>();
        String paramStr = "GET".equals(method) ? query : body; // 根据方法选择参数来源
        if (paramStr == null || paramStr.isEmpty()) {
            return params;
        }

        // 分割参数（key=value&key2=value2格式）
        String[] pairs = paramStr.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2); // 最多分割为2部分（兼容value含=的情况）
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : ""; // 无value时设为空字符串
            params.put(key, value);
        }
        return params;
    }

    /**
     * 全局请求处理器
     * <p>
     * 该内部类实现了 {@link HttpHandler} 接口，用于处理所有进入的HTTP请求。
     * 它会解析请求方法、路径、查询参数和请求体，并返回详细的响应信息。
     * </p>
     *
     * @see HttpHandler
     */
    static class AllRequestHandler implements HttpHandler {
        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            // 1. 获取请求基本信息
            String method = exchange.getRequestMethod(); // 请求方法（GET/POST/PUT/DELETE等）
            String path = exchange.getRequestURI().getPath(); // 请求路径（如/hello）
            String query = exchange.getRequestURI().getQuery(); // URL参数（如?name=test）

            // 2. 读取请求体（Java 8及以下：手动读取输入流）
            byte[] bodyBytes = new byte[0];
            if (!"GET".equals(method) && !"HEAD".equals(method)) {
                // 非GET/HEAD方法才读取请求体
                InputStream requestBody = exchange.getRequestBody();
                bodyBytes = readInputStream(requestBody); // 调用手动读取方法
            }
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            // 3. 解析参数（URL参数或请求体参数）
            Map<String, String> params = parseParams(method, query, body);

            // 4. 构建响应内容
            String response = "=== 收到请求 ===\n" +
                    "请求方法：" + method + "\n" +
                    "请求路径：" + path + "\n" +
                    "URL参数：" + (query == null ? "无" : query) + "\n" +
                    "请求体内容：" + (body.isEmpty() ? "无" : body) + "\n" +
                    "解析后的参数：" + params;

            // 5. 发送响应（必须在读取请求体之后）
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            exchange.close();
        }
    }
}