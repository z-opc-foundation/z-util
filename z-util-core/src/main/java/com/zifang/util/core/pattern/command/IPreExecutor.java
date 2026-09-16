package com.zifang.util.core.pattern.command;

/**
 * 前置执行器接口，定义命令执行前的预处理操作。
 *
 * @author zifang
 */
public interface IPreExecutor {

    /**
     * 在命令核心逻辑执行之前调用的前置处理方法。
     * 可用于参数校验、资源准备、日志记录等准备工作。
     */
    void preExecute();

}
