package com.zifang.util.http.server;

import com.zifang.util.proxy.ProxyUtil;

/**
 * HTTP服务端代理
 * <p>
 * 用于创建服务端接口的动态代理对象。通过代理对象调用方法时，
 * 会自动将方法调用转换为HTTP请求定义。
 * </p>
 *
 * @author zifang
 * @see HttpServerInvocationHandler
 */
public class HttpServerProxy {

    /**
     * 创建服务端接口的代理对象。
     *
     * @param serverInterface 服务端接口类，必须带有 {@link RestController} 注解
     * @param <T>             服务接口的类型
     * @return 服务接口的代理对象
     * @throws IllegalArgumentException 如果 serverInterface 为 null
     */
    public static <T> T proxy(Class<T> serverInterface) {
        return ProxyUtil.newProxyInstance(new HttpServerInvocationHandler(serverInterface), serverInterface);
    }
}
