package com.zifang.util.ioc.exception;

import java.util.List;

/**
 * 循环依赖异常，对标 Guice 的 {@code com.google.inject.CreationException}（循环场景）。
 * <p>
 * 当 A 依赖 B，B 依赖 A 时，在尝试创建 Bean 时抛出此异常。
 */
public class CircularDependencyException extends RuntimeException {

    private final List<String> chain;

    public CircularDependencyException(List<String> chain) {
        super("Circular dependency detected: " + String.join(" -> ", chain));
        this.chain = chain;
    }

    public List<String> getChain() {
        return chain;
    }
}
