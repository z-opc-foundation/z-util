package com.zifang.util.http.base.helper;

import com.zifang.util.core.lang.annoations.AnnotationUtil;
import com.zifang.util.core.util.GsonUtil;
import com.zifang.util.http.base.define.*;
import com.zifang.util.http.base.pojo.HttpRequestBody;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestHeader;
import com.zifang.util.http.base.pojo.HttpRequestLine;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 方法解析出http的请求
 */
public class HttpDefinitionSolver implements IDefinitionSolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private Object proxy;

    private Method method;

    private Object[] args;

    private Class<?> target;

    private Map<String, Object> contextParams;

    private List<ParameterValuePair> parameterValuePairList = new ArrayList<>();

    // 标准定义实例
    private HttpRequestDefinition httpRequestDefinition = new HttpRequestDefinition();

    /**
     * getHttpRequestDefinition方法。
     *
     * @return HttpRequestDefinition类型返回值
     */
    public HttpRequestDefinition getHttpRequestDefinition() {
        return httpRequestDefinition;
    }

    /**
     * set方法。
     * * @param target Class?类型参数
     *
     * @param proxy         Object类型参数
     * @param method        Method类型参数
     * @param args          Object[]类型参数
     * @param contextParams MapString,Object类型参数
     */
    public void set(Class<?> target, Object proxy, Method method, Object[] args, Map<String, Object> contextParams) {
        this.target = target;
        this.proxy = proxy;
        this.method = method;
        this.args = args;
        this.contextParams = contextParams;
    }

    /**
     * solve方法。
     */
    public void solve() {

        // 前处理
        pre();

        // 处理请求行
        handleHttpRequestLine();

        // 处理请求头
        handleHttpRequestHeader();

        // 处理请求体
        handleHttpRequestBody();
    }

    private void handleHttpRequestBody() {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        for (ParameterValuePair parameterValuePair : parameterValuePairList) {
            if (parameterValuePair.getParameter().isAnnotationPresent(RequestBody.class)) {
                byte[] bytes = GsonUtil.objectToJsonStr(parameterValuePair.getObj()).getBytes();
                httpRequestBody.setBody(bytes);
            }
        }
        httpRequestDefinition.setHttpRequestBody(httpRequestBody);
    }

    private void pre() {
        // 生成 parameter -> 值的对象列表
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            parameterValuePairList.add(new ParameterValuePair(parameters[i], args[i]));
        }
    }

    private void handleHttpRequestHeader() {

        HttpRequestHeader headers = new HttpRequestHeader();

        // 先处理对象类上的注解
        RequestHeaders requestHeadersFromObject = AnnotationUtil.getAnnotation(target, RequestHeaders.class);
        if (requestHeadersFromObject != null) {

        }

        // 再处理方法上的注解
        RequestHeaders requestHeadersFromMethod = AnnotationUtil.getAnnotation(method, RequestHeaders.class);
        if (requestHeadersFromMethod != null) {

        }

        httpRequestDefinition.setHttpRequestHeader(headers);
    }

    private void handleHttpRequestLine() {

        // 得到根路径
        String basicPath = AnnotationUtil.getAnnotationValue(target, RestController.class);

        // 解析 HTTP 方法和路径，支持 @GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@RequestMapping
        RequestMethod requestMethod = null;
        String requestPath = null;

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        if (getMapping != null) {
            requestMethod = RequestMethod.GET;
            requestPath = getMapping.value();
        } else if (postMapping != null) {
            requestMethod = RequestMethod.POST;
            requestPath = postMapping.value();
        } else if (putMapping != null) {
            requestMethod = RequestMethod.PUT;
            requestPath = putMapping.value();
        } else if (deleteMapping != null) {
            requestMethod = RequestMethod.DELETE;
            requestPath = deleteMapping.value();
        } else if (requestMapping != null) {
            requestMethod = requestMapping.method();
            requestPath = requestMapping.value();
        }

        // 检验是否有路径参数
        // 检验@requestParam
        List<String> requestParams = new ArrayList<>();
        for (ParameterValuePair parameterValuePair : parameterValuePairList) {
            String key = AnnotationUtil.getAnnotationValue(parameterValuePair.getParameter(), RequestParam.class);
            String value = String.valueOf(parameterValuePair.getObj());
            if (key != null) {
                try {
                    String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
                    String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
                    requestParams.add(String.format("%s=%s", encodedKey, encodedValue));
                } catch (Exception e) {
                    // fallback: 不编码
                    requestParams.add(String.format("%s=%s", key, value));
                }
            }
        }

        // 提取路径变量，替换 requestPath 中的 {var} 占位符
        String processedPath = requestPath;
        if (processedPath != null && processedPath.contains("{")) {
            Map<String, Object> pathVarMap = new LinkedHashMap<>();
            for (ParameterValuePair pvp : parameterValuePairList) {
                PathVariable pv = pvp.getParameter().getAnnotation(PathVariable.class);
                if (pv != null) {
                    String varName = pv.value();
                    pathVarMap.put(varName, pvp.getObj());
                }
            }
            // 替换 {varName} 为实际值
            for (Map.Entry<String, Object> entry : pathVarMap.entrySet()) {
                processedPath = processedPath.replace("{" + entry.getKey() + "}",
                        String.valueOf(entry.getValue()));
            }
        }

        String relativePath = basicPath + processedPath;
        if (!requestParams.isEmpty()) {
            relativePath = relativePath + "?" + String.join("&", requestParams);
        }

        // 支持 contextParams 中的 baseUrl 作为前缀
        String url = relativePath;
        if (contextParams != null && contextParams.containsKey("baseUrl")) {
            String baseUrl = String.valueOf(contextParams.get("baseUrl"));
            if (baseUrl != null && !baseUrl.isEmpty()) {
                // 确除末尾斜杠以避免双重斜杠
                if (!baseUrl.endsWith("/")) {
                    url = baseUrl + relativePath;
                } else {
                    url = baseUrl.substring(0, baseUrl.length() - 1) + relativePath;
                }
            }
        }

        // 存储值
        HttpRequestLine httpRequestLine = new HttpRequestLine(); // 请求行的实例
        httpRequestLine.setUrl(url);
        httpRequestLine.setRequestMethod(requestMethod);
        // 设入值
        httpRequestDefinition.setHttpRequestLine(httpRequestLine);
    }

    private String solveReplaceHolder(String originalText, Map<String, Object> contextParams) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(originalText);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            // 获取占位符名称（如 ${aaa} → "aaa"）
            String placeholderName = matcher.group(1);
            // 从 Map 中获取替换值，无对应值则保留原占位符
            String replacement = contextParams.getOrDefault(placeholderName, matcher.group(0)).toString();
            // 替换并追加到结果
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
