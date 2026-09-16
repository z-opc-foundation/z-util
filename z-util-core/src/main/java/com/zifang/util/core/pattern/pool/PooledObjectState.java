package com.zifang.util.core.pattern.pool;

/**
 * 池化对象状态
 */
public enum PooledObjectState {
    /**
     * 空闲状态，可被借用
     */
    IDLE,

    /**
     * 已被分配使用中
     */
    ALLOCATED,

    /**
     * 正在被 eviction 验证
     */
    EVICTION,

    /**
     * 正在返回池中
     */
    RETURNING,

    /**
     * 已废弃/无效
     */
    INVALID
}
