package com.zifang.util.workflow.engine.interfaces;

import com.zifang.util.workflow.config.ExecutableWorkflowNode;
import com.zifang.util.workflow.conponents.WorkFlowApplicationContext;

import java.util.Objects;

/**
 * 执行引擎服务的抽象基类。
 * 所有具体的服务实现都应继承此类。
 * 负责执行具体的工作流节点任务。
 *
 * @see ExecutableWorkflowNode
 * @see WorkFlowApplicationContext
 */
public abstract class AbstractEngineService {

    /**
     * 因为调用的方法的参数多种多样，因此参数不能是简单的json格式，
     * 因此都会转化为object，由工作的类对此进行改造
     */
    protected Object invokeParameter;
    /**
     * 每个执行服务都要传入执行上下文
     */
    private WorkFlowApplicationContext workFlowApplicationContext;

    /**
     * AbstractEngineService方法。
     */
    public AbstractEngineService() {
    }

    /**
     * 执行引擎服务的执行方法。
     *
     * @param executableWorkflowNode 可执行的工作流节点
     */
    public abstract void exec(ExecutableWorkflowNode executableWorkflowNode);

    /**
     * 获取工作流应用上下文。
     *
     * @return 工作流应用上下文
     */
    public WorkFlowApplicationContext getWorkFlowApplicationContext() {
        return workFlowApplicationContext;
    }

    /**
     * 设置工作流应用上下文。
     *
     * @param workFlowApplicationContext 工作流应用上下文
     */
    public void setWorkFlowApplicationContext(WorkFlowApplicationContext workFlowApplicationContext) {
        this.workFlowApplicationContext = workFlowApplicationContext;
    }

    /**
     * 获取调用参数。
     *
     * @return 调用参数
     */
    public Object getInvokeParameter() {
        return invokeParameter;
    }

    /**
     * 设置调用参数。
     *
     * @param invokeParameter 调用参数
     */
    public void setInvokeParameter(Object invokeParameter) {
        this.invokeParameter = invokeParameter;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "AbstractEngineService{workFlowApplicationContext=" + workFlowApplicationContext + ", invokeParameter=" + invokeParameter + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEngineService that = (AbstractEngineService) o;
        return Objects.equals(workFlowApplicationContext, that.workFlowApplicationContext) &&
                Objects.equals(invokeParameter, that.invokeParameter);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(workFlowApplicationContext, invokeParameter);
    }
}
