package com.zifang.util.core.pattern.command;

/**
 * 命令接口
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public interface Command<C> {

    /**
     * 执行命令
     *
     * @param context 执行上下文
     */
    void execute(C context);

    /**
     * 获取命令名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取命令描述
     */
    default String getDescription() {
        return "";
    }

    /**
     * 是否支持撤销
     */
    default boolean supportsUndo() {
        return false;
    }

    /**
     * 撤销命令
     */
    default void undo(C context) {
        throw new UnsupportedOperationException("Undo not supported");
    }
}
