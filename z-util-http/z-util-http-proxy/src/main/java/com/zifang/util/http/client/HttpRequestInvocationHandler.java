package com.zifang.util.http.client;

import com.zifang.util.core.util.GsonUtil;
import com.zifang.util.http.base.define.RestController;
import com.zifang.util.http.base.helper.HttpDefinitionSolver;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * HTTP请求调用处理器，用于动态代理HTTP请求接口。
 * <p>
 * 该处理器实现了 {@link InvocationHandler} 接口，当代理对象的方法被调用时，
 * 会自动将请求转换为HTTP请求并发送。
 * </p>
 *
 * @author zifang
 * @see InvocationHandler
 */
public class HttpRequestInvocationHandler implements InvocationHandler {

    /**
     * 代理的目标接口类，即被代理的HTTP请求接口。
     */
    private final Class<?> target;
    /**
     * 调用过程中需要的上下文参数，用于在请求过程中传递额外数据。
     */
    private Map<String, Object> contextParams;

    /**
     * 构造一个HTTP请求调用处理器。
     *
     * @param requestInterface HTTP请求接口类，必须带有 {@link RestController} 注解
     */
    public HttpRequestInvocationHandler(Class<?> requestInterface) {
        this.target = requestInterface;
    }

    /**
     * 构造一个HTTP请求调用处理器，并指定上下文参数。
     *
     * @param requestInterface HTTP请求接口类，必须带有 {@link RestController} 注解
     * @param contextParams    上下文参数映射，用于在请求过程中传递额外数据
     */
    public HttpRequestInvocationHandler(Class<?> requestInterface, Map<String, Object> contextParams) {
        this.target = requestInterface;
        this.contextParams = contextParams;
    }

    /**
     * 处理代理对象的方法调用，将Java方法调用转换为HTTP请求。
     * <p>
     * 该方法会执行以下步骤：
     * <ol>
     *   <li>检查代理接口是否符合要求（必须带有 {@link RestController} 注解）</li>
     *   <li>解析方法定义，构建HTTP请求定义</li>
     *   <li>发送HTTP请求并获取响应</li>
     *   <li>将响应内容反序列化为方法的返回类型</li>
     * </ol>
     * </p>
     *
     * @param proxy  代理对象本身（该参数未使用）
     * @param method 被调用的方法对象
     * @param args   方法参数数组
     * @return HTTP响应反序列化后的对象
     * @throws RuntimeException 如果目标接口没有 {@link RestController} 注解
     * @throws Throwable        如果HTTP请求或反序列化过程中发生错误
     */
    @Override
    /**
     * invoke方法。
     *      * @param proxy Object类型参数
     * @param method Method类型参数
     * @param args Object[]类型参数
     * @return Object类型返回值
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 1 检查相关
        check(proxy, method, args);

        // 2 解释器
        HttpDefinitionSolver httpDefinitionSolver = new HttpDefinitionSolver();
        httpDefinitionSolver.set(target, proxy, method, args, contextParams); // 设入参数
        httpDefinitionSolver.solve();

        // 3 获得标准请求定义
        HttpRequestDefinition httpRequestDefinition = httpDefinitionSolver.getHttpRequestDefinition();

        // 4 产生请求
        HttpExecutor httpExecutor = HttpExecutor.getDefault();
        HttpExecutionResult response = httpExecutor.execute(httpRequestDefinition);
        String jsonStr = response.getBody() == null ? "" : response.getBody();

        // 如果返回类型是 String，处理 JSON 字符串值（可能带外层引号）
        if (method.getGenericReturnType() == String.class) {
            // 如果是 JSON 字符串（以 " 开头和结尾），去除引号
            if (jsonStr != null && jsonStr.startsWith("\"") && jsonStr.endsWith("\"") && jsonStr.length() >= 2) {
                return GsonUtil.jsonStrToObject(jsonStr, String.class);
            }
            return jsonStr;
        }

        // 非 String 类型才做 JSON 反序列化
        return GsonUtil.jsonStrToObject(jsonStr, method.getGenericReturnType());

    }

    private void check(Object proxy, Method method, Object[] args) {
        // 1. class 必须继承为 RestController
        if (!target.isAnnotationPresent(RestController.class)) {
            throw new RuntimeException(target.getName() + "类没有RestController注解");
        }
    }

    /**
     * 获取上下文参数。
     *
     * @return 上下文参数映射
     */
    public Map<String, Object> getContextParams() {
        return contextParams;
    }

    /**
     * 设置上下文参数。
     *
     * @param contextParams 上下文参数映射，用于在请求过程中传递额外数据
     */
    public void setContextParams(Map<String, Object> contextParams) {
        this.contextParams = contextParams;
    }

    /**
     * 获取代理的目标接口类。
     *
     * @return 目标接口类
     */
    public Class<?> getTarget() {
        return target;
    }

    /**
     * 返回该处理器的字符串表示。
     *
     * @return 包含 contextParams 和 target 的字符串表示
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "HttpRequestInvocationHandler{contextParams=" + contextParams + ", target=" + target + "}";
    }

    /**
     * 比较两个 HTTP 请求调用处理器是否相等。
     * <p>
     * 两个处理器相等当且仅当它们的 contextParams 和 target 都相等。
     * </p>
     *
     * @param o 待比较的对象
     * @return 如果相等则返回 true，否则返回 false
     */
    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequestInvocationHandler that = (HttpRequestInvocationHandler) o;
        if (contextParams != null ? !contextParams.equals(that.contextParams) : that.contextParams != null)
            return false;
        return target != null ? target.equals(that.target) : that.target == null;
    }

    /**
     * 返回该处理器的哈希码。
     *
     * @return 哈希码值
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = contextParams != null ? contextParams.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }
}
