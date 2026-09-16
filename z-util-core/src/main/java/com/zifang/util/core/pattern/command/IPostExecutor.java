package com.zifang.util.core.pattern.command;

/**
 * 后置执行器接口，定义命令执行后的清理和后续处理操作。
 *
 * @author zifang
 */
public interface IPostExecutor {

    /**
     * 在命令核心逻辑执行之后调用的后置处理方法。
     * 可用于资源释放、日志记录、结果处理等收尾工作。
     */
    void postExecute();

}
