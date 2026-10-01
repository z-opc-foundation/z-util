package com.zifang.util.bc.aopgen;

/**
 * 源码级代理的方法钩子契约：织入生成子类的每个可覆写方法前后。
 */
public interface MethodHook {

    void before(String method, Object[] args);

    void after(String method, Object result);

    /**
     * 目标方法抛异常时的回调（默认 no-op，向后兼容）。
     */
    default void afterException(String method, Throwable t) {
    }
}
