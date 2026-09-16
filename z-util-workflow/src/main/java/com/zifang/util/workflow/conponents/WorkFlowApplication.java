package com.zifang.util.workflow.conponents;

import com.zifang.util.workflow.config.WorkflowConfiguration;
import com.zifang.util.workflow.config.WorkflowNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流应用入口类。
 * <p>
 * 整个处理流的应用，负责创建和管理工作流上下文。
 * 提供工作流上下文的创建、节点管理（增删改）、执行控制等功能。
 * <p>
 * 主要功能：
 * <ul>
 *   <li>创建工作流上下文</li>
 *   <li>管理工作流节点（增加、删除、修改）</li>
 *   <li>控制工作流执行（暂停、恢复、状态查询）</li>
 *   <li>获取工作流上下文</li>
 * </ul>
 *
 * @see WorkFlowApplicationContext
 * @see com.zifang.util.workflow.config.WorkflowConfiguration
 */
public class WorkFlowApplication {

    /**
     * HashMap<>方法。
     *
     * @return static Map<Integer, WorkFlowApplicationContext> workFlowContextMap = new类型返回值
     */
    public static Map<Integer, WorkFlowApplicationContext> workFlowContextMap = new HashMap<>();

    /**
     * Executors.newFixedThreadPool方法。
     * * @param 100 Object类型参数
     *
     * @return static ExecutorService threadPool =类型返回值
     */
    public static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    /**
     * AtomicInteger方法。
     * * @param 0 Object类型参数
     *
     * @return static AtomicInteger workflowContextId = new类型返回值
     */
    public static AtomicInteger workflowContextId = new AtomicInteger(0);

    /**
     * 通过一个workflowConfiguration 主动创造一个workflow的上下文
     * <p>
     * return WorkFlowApplicationContextId
     */
    public synchronized Integer createWorkflowContext(WorkflowConfiguration workflowConfiguration) {

        //更新得到最新的Id值
        Integer currentWorkflowContextId = workflowContextId.incrementAndGet();

        //新建一个上下文
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext();

        //元数据赋值标识号
        workflowConfiguration.getConfigurations().setWorkflowConfigurationId(currentWorkflowContextId);

        //初始化上下文
        workFlowApplicationContext.initialByWorkflowConfigurationInstance(workflowConfiguration);

        //上下文进入内存缓存
        workFlowContextMap.put(currentWorkflowContextId, workFlowApplicationContext);

        //返回上下文id
        return currentWorkflowContextId;
    }

    /**
     * 增加一个游离节点
     * <p>
     * 参数需要提供全量
     */
    public synchronized Boolean addSimpleWorkflowNode(Integer workFlowApplicationContextId, WorkflowNode workflowNode) {

        //从共享上下文池内得到缓存
        WorkFlowApplicationContext workFlowApplicationContext = workFlowContextMap.get(workFlowApplicationContextId);

        //如果workflowNode传入已经有nodeId的情况下，就沿用
        //@TODO 需要检查nodeId是否冲撞，一般都应该是系统生成的nodeId,而不是让用户对nodeId进行赋值
        String nodeId = workflowNode.getNodeId() == null ? workFlowApplicationContext.produceNodeId() : workflowNode.getNodeId();

        //上下文自行分配nodeID
        workflowNode.setNodeId(nodeId);

        //上下文的 元配置信息内部增加 节点信息
        workFlowApplicationContext.getWorkflowConfiguration().getWorkflowNodeList().add(workflowNode);

        //通过上下文内的更新方法，更新源信息
        workFlowApplicationContext.refreshWorkflowConfiguration();

        //根据更新了的元信息，更新整个可执行workflow
        workFlowApplicationContext.refreshExecutableNodeByWorkflowConfiguration();

        return true;
    }

    /**
     * removeWorkflownNode方法。
     * * @param workFlowApplicationContextId int类型参数
     *
     * @param nodeId String类型参数
     * @return synchronized Boolean类型返回值
     */
    public synchronized Boolean removeWorkflownNode(Integer workFlowApplicationContextId, String nodeId) {
        //从共享上下文池内得到缓存
        WorkFlowApplicationContext workFlowApplicationContext = workFlowContextMap.get(workFlowApplicationContextId);

        //挑选出将要被移除的nodeId
        WorkflowNode workflowNodePrepareToRemove = null;
        for (WorkflowNode workflowNode : workFlowApplicationContext.getWorkflowConfiguration().getWorkflowNodeList()) {
            if (nodeId.equals(workflowNode.getNodeId())) {
                workflowNodePrepareToRemove = workflowNode;
                break;
            }
        }

        //清理这个Id相关的连接信息
        for (String preNodeId : workflowNodePrepareToRemove.getConnector().getPre()) {
            workFlowApplicationContext.getWorkflowNodeMap().get(preNodeId).getConnector().getPost().remove(nodeId);
        }

        for (String postNodeId : workflowNodePrepareToRemove.getConnector().getPost()) {
            workFlowApplicationContext.getWorkflowNodeMap().get(postNodeId).getConnector().getPre().remove(nodeId);
        }


        //删除这个节点
        workFlowApplicationContext.getWorkflowConfiguration().getWorkflowNodeList().remove(workflowNodePrepareToRemove);

//        List<WorkflowNode> workflowNodes = workFlowApplicationContext
//                .getWorkflowConfiguration()
//                .getWorkflowNodeList()
//                .stream()
//                .filter(e -> !nodeId.equals(e.getNodeId()))
//                .collect(Collectors.toList());
//
//        //更新一波，删掉指定nodeId
//        workFlowApplicationContext.getWorkflowConfiguration()
//                .setWorkflowNodeList(workflowNodes);

        //通过上下文内的更新方法，更新源信息
        workFlowApplicationContext.refreshWorkflowConfiguration();

        //根据更新了的元信息，更新整个可执行workflow
        workFlowApplicationContext.refreshExecutableNodeByWorkflowConfiguration();

        return true;
    }

    /**
     * 更新某一个节点的配置 配置包含 执行单元，执行单元所需参数，上下游连接情况
     */
    public synchronized Boolean modifyWorkflowNodeConfiguration(Integer workFlowApplicationContextId, WorkflowNode workflowNode) {

        //从共享上下文池内得到缓存
        WorkFlowApplicationContext workFlowApplicationContext = workFlowContextMap.get(workFlowApplicationContextId);

        workFlowApplicationContext.replaceWorkflowNode(workflowNode);

        //通过上下文内的更新方法，更新源信息
        workFlowApplicationContext.refreshWorkflowConfiguration();

        //根据更新了的元信息，更新整个可执行workflow
        workFlowApplicationContext.refreshExecutableNodeByWorkflowConfiguration();

        return null;
    }


    /**
     * 重置某个节点的状态，连带的所有的后续的节点全部回滚到初始状态
     */
    public synchronized Boolean resetWorkflowNode() {
        return null;
    }

    /***
     * 单步执行，执行到指定位置的node,中止
     * */
    public synchronized Boolean startReferTo() {
        return null;
    }

    /**
     * 强制这个上下文暂停，并返回状态
     */
    public synchronized Boolean forcePause() {
        return null;
    }

    /**
     * 再重新启动这个 工作流的上下文
     */
    public synchronized Boolean resume() {
        return null;
    }

    /**
     * 所有的上下文的状态情况
     */
    public Boolean status() {
        return null;
    }

    /**
     * 根据workflowContextId 得到对应的 WorkFlowApplicationContext
     */
    public WorkFlowApplicationContext getWorkFlowApplicationContext(Integer workflowContextId) {
        return workFlowContextMap.get(workflowContextId);
    }


}
