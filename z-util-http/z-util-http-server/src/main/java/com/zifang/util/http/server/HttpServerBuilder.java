package com.zifang.util.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zifang.util.http.base.define.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP服务器构建器
 * 用于快速构建基于注解的HTTP服务器
 */
public class HttpServerBuilder {

    private final List<Object> controllers = new ArrayList<>();
    private final Map<String, RouteHandler> routeHandlers = new ConcurrentHashMap<>();
    private final List<RouteHandler> allHandlers = new ArrayList<>();
    private final List<Runnable> postStartCallbacks = new ArrayList<>();
    private int port = 8080;
    private HttpServer httpServer;

    /**
     * 绑定端口
     *
     * @param port 端口号
     * @return HttpServerBuilder实例
     */
    public static HttpServerBuilder bindPort(int port) {
        HttpServerBuilder builder = new HttpServerBuilder();
        builder.port = port;
        return builder;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    List<RouteHandler> getRouteHandlers() {
        return allHandlers;
    }

    /**
     * 注册完成后回调（用于直接操作底层 HttpServer 实现 SSE 等高级特性）。
     * 在所有路由注册完毕、HttpServer 创建之后调用。
     */
    public HttpServerBuilder afterRegister(Runnable callback) {
        postStartCallbacks.add(callback);
        return this;
    }

    /**
     * 注册控制器
     *
     * @param controller 控制器实例
     * @return HttpServerBuilder实例
     */
    public HttpServerBuilder proxy(Object controller) {
        this.controllers.add(controller);
        return this;
    }

    /**
     * 启动HTTP服务器
     *
     * @return HttpServerBuilder实例
     */
    public HttpServerBuilder start() {
        try {
            // 创建HTTP服务器
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);

            // 注册路由
            registerRoutes();

            // 创建根处理器
            httpServer.createContext("/", new RootHandler());

            // 设置线程池
            httpServer.setExecutor(Executors.newFixedThreadPool(10));

            // 启动服务器
            httpServer.start();

            System.out.println("HTTP Server started on port " + port);

            // 执行 afterRegister 回调（httpServer 已创建完毕）
            for (Runnable cb : postStartCallbacks) {
                cb.run();
            }

            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server", e);
        }
        return this;
    }

    /**
     * 停止HTTP服务器
     */
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("HTTP Server stopped");
        }
    }

    /**
     * 注册所有路由
     */
    private void registerRoutes() {
        for (Object controller : controllers) {
            registerController(controller);
        }
    }

    /**
     * 注册单个控制器
     *
     * @param controller 控制器实例
     */
    private void registerController(Object controller) {
        Class<?> clazz = controller.getClass();

        // 获取@RestController注解
        RestController restController = clazz.getAnnotation(RestController.class);
        if (restController == null) {
            return;
        }

        String basePath = restController.value();
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        // 遍历所有方法
        for (Method method : clazz.getDeclaredMethods()) {
            registerMethod(controller, method, basePath);
        }
    }

    /**
     * 注册单个方法
     *
     * @param controller 控制器实例
     * @param method     方法
     * @param basePath   基础路径
     */
    private void registerMethod(Object controller, Method method, String basePath) {
        String httpMethod = null;
        String path = null;

        // 检查各种HTTP方法注解
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            httpMethod = "GET";
            path = getMapping.value();
        }

        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            httpMethod = "POST";
            path = postMapping.value();
        }

        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            httpMethod = "PUT";
            path = putMapping.value();
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            httpMethod = "DELETE";
            path = deleteMapping.value();
        }

        if (httpMethod == null || path == null) {
            return;
        }

        // 构建完整路径
        String fullPath = basePath;
        if (!path.startsWith("/")) {
            fullPath += "/";
        }
        fullPath += path;

        // 规范化路径（去除多余斜杠）
        fullPath = fullPath.replaceAll("/+", "/");
        if (!fullPath.startsWith("/")) {
            fullPath = "/" + fullPath;
        }

        // 创建路由键
        String routeKey = httpMethod + ":" + fullPath;

        // 编译路径模式（用于路径参数匹配）
        Pattern pattern = compilePathPattern(fullPath);

        // 注册路由处理器
        RouteHandler handler = new RouteHandler(controller, method, pattern, fullPath);
        routeHandlers.put(routeKey, handler);
        allHandlers.add(handler);

        System.out.println("Registered route: " + routeKey);
    }

    /**
     * 编译路径模式
     *
     * @param path 路径
     * @return 正则表达式模式
     */
    private Pattern compilePathPattern(String path) {
        // 将路径参数 {id} 转换为正则表达式组（使用数字组，而非命名组，兼容性更好）
        String regex = path.replaceAll("\\{([^}]+)\\}", "([^/]+)");
        // 处理查询参数（移除它们，只匹配路径部分）
        if (regex.contains("?")) {
            regex = regex.substring(0, regex.indexOf("?"));
        }
        return Pattern.compile("^" + regex + "$");
    }

    /**
     * 查找带路径参数的处理器
     *
     * @param httpMethod HTTP方法
     * @param path       请求路径
     * @return 路由处理器
     */
    private RouteHandler findHandlerWithPathParams(String httpMethod, String path) {
        for (Map.Entry<String, RouteHandler> entry : routeHandlers.entrySet()) {
            String routeKey = entry.getKey();
            if (routeKey.startsWith(httpMethod + ":")) {
                RouteHandler handler = entry.getValue();
                if (handler.pattern.matcher(path).matches()) {
                    return handler;
                }
            }
        }
        return null;
    }

    /**
     * 处理请求
     *
     * @param exchange    HTTP交换对象
     * @param handler     路由处理器
     * @param queryString 查询字符串
     */
    private void handleRequest(HttpExchange exchange, RouteHandler handler, String queryString) {
        try {
            // 解析路径参数
            Map<String, String> pathParams = extractPathParams(handler, exchange.getRequestURI().getPath());

            // 解析查询参数
            Map<String, String> queryParams = parseQueryString(queryString);

            // 读取请求体
            String requestBody = readRequestBody(exchange);

            // 准备方法参数
            Object[] args = prepareMethodArguments(
                    handler.method,
                    pathParams,
                    queryParams,
                    requestBody
            );

            // 调用方法
            Object result = handler.method.invoke(handler.controller, args);

            // 发送响应
            if (result != null) {
                sendResponse(exchange, 200, result.toString());
            } else {
                sendResponse(exchange, 200, "");
            }

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            sendResponse(exchange, 500, "Internal Server Error: " + (cause != null ? cause.getMessage() : e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 提取路径参数
     *
     * @param handler     路由处理器
     * @param requestPath 请求路径
     * @return 路径参数映射
     */
    private Map<String, String> extractPathParams(RouteHandler handler, String requestPath) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = handler.pattern.matcher(requestPath);
        if (matcher.matches()) {
            // 从路径模板中提取参数名
            String patternStr = handler.path;
            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
            java.util.regex.Matcher paramMatcher = paramPattern.matcher(patternStr);

            int groupIndex = 1;
            while (paramMatcher.find()) {
                String paramName = paramMatcher.group(1);
                try {
                    String value = matcher.group(groupIndex);
                    if (value != null) {
                        params.put(paramName, value);
                    }
                } catch (IndexOutOfBoundsException e) {
                    // 忽略
                }
                groupIndex++;
            }
        }
        return params;
    }

    /**
     * 解析查询字符串
     *
     * @param queryString 查询字符串
     * @return 参数映射
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * 读取请求体
     *
     * @param exchange HTTP交换对象
     * @return 请求体字符串
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (java.io.InputStream is = exchange.getRequestBody();
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 准备方法参数
     *
     * @param method      方法
     * @param pathParams  路径参数
     * @param queryParams 查询参数
     * @param requestBody 请求体
     * @return 参数数组
     */
    private Object[] prepareMethodArguments(Method method,
                                            Map<String, String> pathParams,
                                            Map<String, String> queryParams,
                                            String requestBody) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();

            // 处理 @PathVariable
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String name = pathVariable.value();
                String value = pathParams.get(name);
                args[i] = convertValue(value, paramType);
                continue;
            }

            // 处理 @RequestParam
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String name = requestParam.value();
                String value = queryParams.get(name);
                args[i] = convertValue(value, paramType);
                continue;
            }

            // 处理 @RequestBody
            RequestBody requestBodyAnnotation = param.getAnnotation(RequestBody.class);
            if (requestBodyAnnotation != null) {
                args[i] = requestBody;
                continue;
            }

            // 默认：尝试从查询参数获取
            String value = queryParams.get(param.getName());
            if (value == null && paramType == String.class) {
                args[i] = requestBody;
            } else {
                args[i] = convertValue(value, paramType);
            }
        }

        return args;
    }

    /**
     * 转换值为目标类型
     *
     * @param value      字符串值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    /**
     * 发送HTTP响应
     *
     * @param exchange   HTTP交换对象
     * @param statusCode 状态码
     * @param response   响应内容
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) {
        try {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 路由处理器
     */
    private static class RouteHandler {
        final Object controller;
        final Method method;
        final Pattern pattern;
        final String path;

        RouteHandler(Object controller, Method method, Pattern pattern, String path) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            this.path = path;
        }
    }

    /**
     * 根请求处理器
     */
    private class RootHandler implements HttpHandler {
        @Override
        /**
         * handle方法。
         *      * @param exchange HttpExchange类型参数
         */
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            String queryString = exchange.getRequestURI().getQuery();

            // 构建路由键
            String routeKey = requestMethod + ":" + requestPath;

            // 查找处理器
            RouteHandler handler = routeHandlers.get(routeKey);

            // 如果没有精确匹配，尝试路径参数匹配
            if (handler == null) {
                handler = findHandlerWithPathParams(requestMethod, requestPath);
            }

            if (handler != null) {
                handleRequest(exchange, handler, queryString);
            } else {
                // 404 Not Found
                sendResponse(exchange, 404, "Not Found");
            }
        }
    }
}