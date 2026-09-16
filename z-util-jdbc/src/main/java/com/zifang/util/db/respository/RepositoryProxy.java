package com.zifang.util.db.respository;


import com.zifang.util.proxy.ProxyUtil;

/**
 * 仓储代理工厂，用于创建Repository接口的动态代理实例
 */
public class RepositoryProxy {

    /**
     * 为指定的Repository接口创建动态代理实例
     *
     * @param clazz Repository接口类型
     * @param <I>   接口类型
     * @return 代理后的接口实现实例
     */
    public static <I> I proxy(Class<I> clazz) {
        return ProxyUtil.newProxyInstance(new BaseRepositoryInvocationHandler(clazz), clazz);
    }
}
