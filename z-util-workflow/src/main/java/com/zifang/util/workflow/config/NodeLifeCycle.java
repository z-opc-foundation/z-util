package com.zifang.util.workflow.config;

/**
 * 节点生命周期接口。
 * <p>
 * 定义工作流节点的生命周期状态和回调方法。
 * 实现此接口的类可以响应节点状态变化事件。
 * <p>
 * 状态常量：
 * <ul>
 *   <li>PREPARED - 准备就绪状态</li>
 *   <li>STARTED - 运行状态</li>
 *   <li>INTERRRUPTED - 被强行中止状态</li>
 *   <li>EXECUTED - 执行完毕状态</li>
 * </ul>
 *
 * @see ExecutableWorkflowNode
 */
public interface NodeLifeCycle {

    /**
     * 表达该节点准备就绪：
     * 条件：
     * 初始化之后，前置节点全部结束之后，本节点触发过设置参数操作之后
     */
    String PREPARED = "prepared";

    /**
     * 该节点处在运行状态，按道理是一个原子态
     */
    String STARTED = "started";

    /**
     * 被强行的中止操作的情况下
     */
    String INTERRRUPTED = "interrupted";

    /**
     * 已经执行完毕
     */
    String EXECUTED = "executed";

    /**
     * 获得得到当前节点的状况
     */
    String getStatus();

    /**
     * 赋值
     */
    void setStatus(String status);

    /**
     * 前处理
     */
    void preExecute();

    /**
     * 后处理
     */
    void postExecute();

    /**
     * 初始化
     */
    void init();

    /**
     * 将这个节点重置为可执行状态
     * <p>
     * 注意，这个方法只是单纯的对自身节点进行重置，上下文内节点的重置将由上下文进行
     */
    default void setRestart() {
        this.setStatus(PREPARED);
    }
}
