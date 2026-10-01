package com.zifang.util.core.pattern.strategy;

/**
 * GoF Strategy: 运行时可选算法.
 * <p>
 * 与 {@link com.zifang.util.core.pattern.factory.IFactory} 的区别: Factory 按 key 缓存单例,
 * Strategy 按 context 决定行为. 一个偏对象池/复用, 一个偏决策/分支.
 */
public interface Strategy<K, C, R> {

    /** 当前策略是否适用此 context. */
    boolean matches(K key, C context);

    /** 执行策略. matches 返回 true 才被调用. */
    R apply(K key, C context);
}