package com.zifang.util.proxy.aopgen;

/**
 * 源码级代理的方法钩子契约：织入生成子类的每个可覆写方法前后。
 */
public interface MethodHook {

    void before(String method, Object[] args);

    void after(String method, Object result);
}
