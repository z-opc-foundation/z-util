package com.zifang.util.http.client;

import com.zifang.util.http.base.define.RestController;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 客户端工厂 - 注解驱动 HTTP 调用的统一入口。
 * <p>
 * 用户只需定义一个带注解的接口（类级别 {@link RestController} + 方法级别 {@code @GetMapping/@PostMapping/...}），
 * 通过本工厂创建代理实例，即可像调用本地方法一样发起 HTTP 请求。
 * <p>
 * 示例:
 * <pre>
 * {@code
 * @RestController("https://api.github.com")
 * public interface GitHubApi {
 *     @GetMapping("/users/{username}")
 *     User getUser(@PathVariable("username") String username);
 *
 *     @PostMapping("/users/repos")
 *     Repo createRepo(@RequestBody RepoRequest req, @RequestParam("token") String token);
 * }
 *
 * GitHubApi api = HttpClientFactory.create(GitHubApi.class);
 * User user = api.getUser("octocat");
 * }
 * </pre>
 * <p>
 * 底层基于 JDK 动态代理 ({@link Proxy}) + {@link HttpRequestInvocationHandler} + {@link HttpExecutor}。
 * 所有请求统一经过 {@link HttpExecutor}，享受连接池、超时、重试等能力。
 *
 * @param <T> 被代理的 HTTP 接口类型
 * @author zifang
 * @see HttpRequestInvocationHandler
 * @see HttpExecutor
 */
public class HttpClientFactory {

    private HttpClientFactory() {
    }

    /**
     * 创建 HTTP 接口代理实例（使用默认配置）。
     *
     * @param requestInterface 带 {@link RestController} 注解的 HTTP 调用接口
     * @param <T>              接口类型
     * @return 代理实例，调用其方法即发起对应 HTTP 请求
     * @throws IllegalArgumentException if interface is null or not annotated with {@link RestController}
     */
    public static <T> T create(Class<T> requestInterface) {
        return create(requestInterface, new HashMap<>());
    }

    /**
     * 创建 HTTP 接口代理实例（带全局上下文参数）。
     * <p>
     * contextParams 中可传入:
     * <ul>
     *   <li>{@code baseUrl} - 覆盖接口级别的根路径，适用于多环境切换</li>
     *   <li>{@code timeout} - 全局超时（单位：毫秒）</li>
     *   <li>其他自定义 key，在请求过程中可被 {@link com.zifang.util.http.base.helper.HttpDefinitionSolver} 引用</li>
     * </ul>
     *
     * @param requestInterface 带 {@link RestController} 注解的 HTTP 调用接口
     * @param contextParams    全局上下文参数（可被方法级参数覆盖）
     * @param <T>              接口类型
     * @return 代理实例
     * @throws IllegalArgumentException if interface is null, not annotated, or contextParams is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> requestInterface, Map<String, Object> contextParams) {
        if (requestInterface == null) {
            throw new IllegalArgumentException("requestInterface cannot be null");
        }
        if (!requestInterface.isAnnotationPresent(RestController.class)) {
            throw new IllegalArgumentException(
                    requestInterface.getName() + " must be annotated with @" + RestController.class.getSimpleName());
        }
        if (contextParams == null) {
            throw new IllegalArgumentException("contextParams cannot be null");
        }

        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(requestInterface, contextParams);
        return (T) Proxy.newProxyInstance(
                requestInterface.getClassLoader(),
                new Class<?>[]{requestInterface},
                handler
        );
    }

    /**
     * 重新创建代理实例（当需要刷新上下文时）。
     *
     * @param requestInterface 带 {@link RestController} 注解的 HTTP 调用接口
     * @param contextParams    新的上下文参数
     * @param <T>              接口类型
     * @return 新代理实例
     */
    public static <T> T recreate(Class<T> requestInterface, Map<String, Object> contextParams) {
        return create(requestInterface, contextParams);
    }
}
