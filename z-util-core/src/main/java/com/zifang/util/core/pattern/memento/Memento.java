package com.zifang.util.core.pattern.memento;

/**
 * 备忘录接口（Memento）
 *
 * @param <T> 状态类型
 * @author zifang
 */
public interface Memento<T> {

    /**
     * 获取存储的状态
     */
    T getState();

    /**
     * 获取状态创建时间戳
     */
    long getTimestamp();

    /**
     * 获取标签
     */
    default String getLabel() {
        return null;
    }

    /**
     * 获取描述
     */
    default String getDescription() {
        return null;
    }
}