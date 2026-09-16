package com.zifang.util.core.pattern.command;

/**
 * 命令接口，定义了命令执行的基本行为。
 * 继承自IPostExecutor和IPreExecutor，提供前处理、执行、后处理的完整生命周期。
 *
 * @author zifang
 */
public interface ICommand extends IPostExecutor, IPreExecutor {

    /**
     * 执行命令的核心逻辑。
     * 该方法在{@link #preExecute()}之后、{@link #postExecute()}之前被调用。
     */
    void execute();

}
