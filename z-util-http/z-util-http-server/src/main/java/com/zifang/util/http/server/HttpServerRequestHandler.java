package com.zifang.util.http.server;

import com.zifang.util.http.base.define.*;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 服务端请求处理器
 * 负责将 HTTP 请求分派到对应的方法
 */
public class HttpServerRequestHandler {

    private final Object target;
    private final Map<String, MethodMapping> methodMappings;
    private final String contextPath;

    /**
     * HttpServerRequestHandler方法。
     * * @param target Object类型参数
     */
    public HttpServerRequestHandler(Object target) {
        this.target = target;
        this.methodMappings = new HashMap<>();
        this.contextPath = extractContextPath();
        scanMethods();
    }

    private String extractContextPath() {
        Class<?> clazz = target.getClass();
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> iface : interfaces) {
            RestController rc = iface.getAnnotation(RestController.class);
            if (rc != null) {
                String path = rc.value();
                return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            }
        }
        return "";
    }

    /**
     * 扫描目标类的方法，建立映射
     * 优先扫描接口（因为注解在接口上），若无接口则扫描实现类
     */
    private void scanMethods() {
        Class<?> clazz = target.getClass();

        // 优先从接口上扫描注解（target 是实现类，需要找到对应的接口）
        Class<?>[] interfaces = clazz.getInterfaces();
        Class<?> annotationSource = null;
        for (Class<?> iface : interfaces) {
            if (iface.isAnnotationPresent(RestController.class)) {
                annotationSource = iface;
                break;
            }
        }
        // 如果没找到接口（例如 target 本身是接口），直接扫描 target
        if (annotationSource == null) {
            annotationSource = clazz;
        }

        for (Method method : annotationSource.getDeclaredMethods()) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);

            if (mapping != null || getMapping != null || postMapping != null ||
                    putMapping != null || deleteMapping != null) {

                MethodMapping methodMapping = new MethodMapping(method, mapping, getMapping,
                        postMapping, putMapping, deleteMapping);

                String key = generateKey(methodMapping);
                methodMappings.put(key, methodMapping);
            }
        }
    }

    /**
     * 生成方法映射的 key
     */
    private String generateKey(MethodMapping mapping) {
        return mapping.getHttpMethod() + ":" + mapping.getPath();
    }

    /**
     * 处理 HTTP 请求
     *
     * @param requestDefinition HTTP 请求定义
     * @return 处理结果
     */
    public Object handleRequest(HttpRequestDefinition requestDefinition) {
        if (requestDefinition == null || requestDefinition.getHttpRequestLine() == null) {
            throw new IllegalArgumentException("Invalid request definition");
        }

        String httpMethod = requestDefinition.getHttpRequestLine().getRequestMethod().name();
        String url = requestDefinition.getHttpRequestLine().getUrl();

        // 从 URL 中提取路径部分（去除协议+主机+端口）
        String path = url;
        int protoEnd = path.indexOf("://");
        if (protoEnd > 0) {
            int slashAfterHost = path.indexOf('/', protoEnd + 3);
            if (slashAfterHost >= 0) {
                path = path.substring(slashAfterHost);
            }
        }
        // 去除查询参数
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        // 去除 context path 前缀
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        // 查找匹配的方法
        String key = httpMethod + ":" + path;
        MethodMapping mapping = methodMappings.get(key);
        Map<String, String> pathParams = new HashMap<>();

        if (mapping == null) {
            // 尝试模糊匹配，同时提取路径变量
            java.util.Map.Entry<MethodMapping, Map<String, String>> result = findMatchingMethodWithParams(httpMethod, url);
            if (result != null) {
                mapping = result.getKey();
                pathParams = result.getValue();
            }
        }

        if (mapping == null) {
            throw new RuntimeException("No handler found for " + httpMethod + " " + path);
        }

        // 准备方法参数
        Object[] args = prepareArguments(mapping, requestDefinition, pathParams);

        // 调用方法
        try {
            mapping.getMethod().setAccessible(true);
            return mapping.getMethod().invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handler method", e);
        }
    }

    /**
     * 查找匹配的方法（支持路径变量）
     */
    private MethodMapping findMatchingMethod(String httpMethod, String path) {
        for (MethodMapping mapping : methodMappings.values()) {
            if (!mapping.getHttpMethod().equals(httpMethod)) {
                continue;
            }

            if (matchPath(mapping.getPath(), path)) {
                return mapping;
            }
        }
        return null;
    }

    /**
     * 查找匹配的方法并同时提取路径变量
     */
    private java.util.Map.Entry<MethodMapping, Map<String, String>> findMatchingMethodWithParams(String httpMethod, String url) {
        // 从 URL 中提取路径部分（去除 host:port 和查询参数）
        String path = extractPathFromUrl(url);

        for (MethodMapping mapping : methodMappings.values()) {
            if (!mapping.getHttpMethod().equals(httpMethod)) {
                continue;
            }

            Map<String, String> params = matchPathWithParams(mapping.getPath(), path);
            if (params != null) {
                return new java.util.AbstractMap.SimpleEntry<>(mapping, params);
            }
        }
        return null;
    }

    /**
     * 从完整 URL 中提取路径部分
     */
    private String extractPathFromUrl(String url) {
        // 去除协议和 host:port，得到路径部分
        // 例如: http://localhost:8080/api/users/123 -> /api/users/123
        int pathStart = url.indexOf("//");
        if (pathStart >= 0) {
            int hostEnd = url.indexOf("/", pathStart + 2);
            if (hostEnd >= 0) {
                return url.substring(hostEnd);
            }
        }
        // 如果没有协议，直接返回（去除查询参数）
        int queryStart = url.indexOf("?");
        return queryStart >= 0 ? url.substring(0, queryStart) : url;
    }

    /**
     * 匹配路径（支持路径变量如 /users/{id}），同时提取变量值
     *
     * @return 路径变量名->值的 Map，若不匹配则返回 null
     */
    private Map<String, String> matchPathWithParams(String pattern, String path) {
        if (pattern.equals(path)) {
            return new HashMap<>();
        }

        // 支持路径变量 {var}
        String regex = pattern.replaceAll("\\{([^/]+)\\}", "([^/]+)");

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^" + regex + "$");
        java.util.regex.Matcher m = p.matcher(path);
        if (!m.matches()) {
            return null;
        }

        // 提取路径变量名和对应的值
        Map<String, String> params = new HashMap<>();
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile("\\{([^/]+)\\}");
        java.util.regex.Matcher nameMatcher = namePattern.matcher(pattern);
        int groupIndex = 1;
        while (nameMatcher.find()) {
            String varName = nameMatcher.group(1);
            String varValue = m.group(groupIndex);
            params.put(varName, varValue);
            groupIndex++;
        }
        return params;
    }

    /**
     * 匹配路径（支持路径变量如 /users/{id}）
     */
    private boolean matchPath(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }

        // 支持路径变量 {var}
        String regex = pattern.replaceAll("\\{[^/]+\\}", "([^/]+)");
        return path.matches("^" + regex + "$");
    }

    /**
     * 准备方法参数
     */
    private Object[] prepareArguments(MethodMapping mapping, HttpRequestDefinition request, Map<String, String> pathParams) {
        Method method = mapping.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            // @PathVariable
            PathVariable pathVar = param.getAnnotation(PathVariable.class);
            if (pathVar != null) {
                String name = pathVar.value();
                String value = pathParams.get(name);
                args[i] = convertType(value, param.getType());
                continue;
            }

            // @RequestParam
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                args[i] = extractRequestParam(request, requestParam.value(), param.getType());
                continue;
            }

            // @RequestBody
            RequestBody requestBody = param.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                args[i] = extractRequestBody(request, param.getType());
                continue;
            }

            // 默认：尝试从请求体转换
            args[i] = extractRequestBody(request, param.getType());
        }

        return args;
    }

    /**
     * 提取路径变量
     */
    private Object extractPathVariable(String pattern, String url, Class<?> type) {
        // 简化实现，实际应该解析路径变量
        return convertType("1", type);
    }

    /**
     * 提取请求参数
     */
    private Object extractRequestParam(HttpRequestDefinition request, String name, Class<?> type) {
        String url = request.getHttpRequestLine().getUrl();
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return null;
        }

        String query = url.substring(queryStart + 1);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = pair.substring(0, eq);
                if (key.equals(name)) {
                    String value = pair.substring(eq + 1);
                    return convertType(value, type);
                }
            }
        }
        return null;
    }

    /**
     * 提取请求体
     */
    private Object extractRequestBody(HttpRequestDefinition request, Class<?> type) {
        if (request.getHttpRequestBody() == null || request.getHttpRequestBody().getBody() == null) {
            return null;
        }

        String bodyStr = new String(request.getHttpRequestBody().getBody());

        // 如果是 String 类型，直接返回
        if (type == String.class) {
            return bodyStr;
        }

        // 其他类型转换（简化实现）
        return convertType(bodyStr, type);
    }

    /**
     * 类型转换
     */
    @SuppressWarnings("unchecked")
    private <T> T convertType(String value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return (T) value;
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(value);
        }

        return null;
    }

    /**
     * 方法映射信息
     */
    private static class MethodMapping {
        private final Method method;
        private final RequestMapping mapping;
        private final GetMapping getMapping;
        private final PostMapping postMapping;
        private final PutMapping putMapping;
        private final DeleteMapping deleteMapping;

        /**
         * MethodMapping方法。
         * * @param method Method类型参数
         *
         * @param mapping       RequestMapping类型参数
         * @param getMapping    GetMapping类型参数
         * @param postMapping   PostMapping类型参数
         * @param putMapping    PutMapping类型参数
         * @param deleteMapping DeleteMapping类型参数
         */
        public MethodMapping(Method method, RequestMapping mapping, GetMapping getMapping,
                             PostMapping postMapping, PutMapping putMapping, DeleteMapping deleteMapping) {
            this.method = method;
            this.mapping = mapping;
            this.getMapping = getMapping;
            this.postMapping = postMapping;
            this.putMapping = putMapping;
            this.deleteMapping = deleteMapping;
        }

        /**
         * getMethod方法。
         *
         * @return Method类型返回值
         */
        public Method getMethod() {
            return method;
        }

        /**
         * getHttpMethod方法。
         *
         * @return String类型返回值
         */
        public String getHttpMethod() {
            if (getMapping != null) return "GET";
            if (postMapping != null) return "POST";
            if (putMapping != null) return "PUT";
            if (deleteMapping != null) return "DELETE";
            if (mapping != null) return mapping.method().name();
            return "GET";
        }

        /**
         * getPath方法。
         *
         * @return String类型返回值
         */
        public String getPath() {
            if (getMapping != null) return getMapping.value();
            if (postMapping != null) return postMapping.value();
            if (putMapping != null) return putMapping.value();
            if (deleteMapping != null) return deleteMapping.value();
            if (mapping != null) return mapping.value();
            return "/";
        }
    }

}
