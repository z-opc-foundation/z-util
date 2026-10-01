package com.zifang.util.core.pattern.command;

/**
 * 命令抽象基类，提供了命令执行的模板方法实现。
 * 按照 前处理{@link #preExecute()} -> 核心执行{@link #execute()} -> 后处理{@link #postExecute()} 的顺序执行。
 *
 * @author zifang
 */
public abstract class AbstractCommand implements ICommand {

    /**
     * 执行命令的模板方法。
     * 该方法按照以下顺序调用：
     * 1. {@link #preExecute()} - 前置处理
     * 2. {@link #execute()} - 核心执行逻辑（由子类实现）
     * 3. {@link #postExecute()} - 后置处理
     */
    @Override
    /**
     * execute方法。
     */
    public void execute() {
        preExecute();
        execute();
        postExecute();
    }

    /**
     * 后置处理方法，默认实现为空。
     * 子类可重写此方法以执行清理、资源释放等操作。
     */
    @Override
    /**
     * postExecute方法。
     */
    public void postExecute() {

    }

    /**
     * 前置处理方法，默认实现为空。
     * 子类可重写此方法以执行参数校验、资源准备等操作。
     */
    @Override
    /**
     * preExecute方法。
     */
    public void preExecute() {

    }

}
