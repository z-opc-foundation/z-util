package com.zifang.util.http.server;

import com.zifang.util.http.base.define.RestController;
import com.zifang.util.http.base.helper.HttpDefinitionSolver;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.client.HttpExecutionResult;
import com.zifang.util.http.client.HttpExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * HTTP服务端调用处理器
 * <p>
 * 该处理器实现了 {@link InvocationHandler} 接口，用于动态代理服务端接口。
 * 当代理对象的方法被调用时，会自动将方法调用转换为HTTP请求定义。
 * </p>
 *
 * @author zifang
 * @see InvocationHandler
 */
public class HttpServerInvocationHandler implements InvocationHandler {

    // 代理的接口类
    private final Class<?> target;

    // 调用过程中需要的上下文参数
    private Map<String, Object> contextParams;

    /**
     * HttpServerInvocationHandler方法。
     * * @param requestInterface Class?类型参数
     */
    public HttpServerInvocationHandler(Class<?> requestInterface) {
        this.target = requestInterface;
    }

    /**
     * HttpServerInvocationHandler方法。
     * * @param requestInterface Class?类型参数
     *
     * @param contextParams MapString,Object类型参数
     */
    public HttpServerInvocationHandler(Class<?> requestInterface, Map<String, Object> contextParams) {
        this.target = requestInterface;
        this.contextParams = contextParams;
    }

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

        // 4 产生请求（统一走 HttpExecutor，OkHttp 实现）
        HttpExecutionResult result = HttpExecutor.getDefault().execute(httpRequestDefinition);
        return result.isSuccess() ? result.getBody() : null;

    }

    private void check(Object proxy, Method method, Object[] args) {
        // 1. class 必须继承为 RestController
        if (!target.isAnnotationPresent(RestController.class)) {
            throw new RuntimeException(target.getName() + "类没有RestController注解");
        }
    }
}
