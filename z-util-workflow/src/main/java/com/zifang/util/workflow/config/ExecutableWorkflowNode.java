package com.zifang.util.workflow.config;

import com.zifang.util.workflow.engine.interfaces.AbstractEngine;
import com.zifang.util.workflow.engine.interfaces.AbstractEngineService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 可执行的工作流节点。
 * 继承自WorkflowNode，实现了NodeLifeCycle接口。
 * 包含节点执行状态、执行引擎、前后置节点引用和CountDownLatch同步机制。
 *
 * @see WorkflowNode
 * @see NodeLifeCycle
 * @see AbstractEngine
 * @see AbstractEngineService
 */
public class ExecutableWorkflowNode extends WorkflowNode implements NodeLifeCycle {

    // 描述当前节点的状态
    private String status;

    // 描述当前节点的执行引擎
    private AbstractEngine abstractEngine;

    // 描述当前的执行单元服务
    private AbstractEngineService abstractEngineService;

    //每个可执行节点会有两种方式与其他节点交流：
    //1. 主动将数据集塞给后置节点，表现为主动遍历所有前置节点得到数据集合
    //2. 被动接收前置节点传入的，表现为获取当前节点内的datasetPre
//    private Dataset<Row> dataset;//当前的数据结果集合
//
//    private Dataset<Row> datasetPre; //上个节点强制push到这个节点的dataSet

    private CountDownLatch countDownLatch;//用于控制前置节点的

    private List<CountDownLatch> postCountDownLatchList = new ArrayList<>();//用于通知后置节点的

    /**
     * 集合所有的后置节点
     */
    private List<ExecutableWorkflowNode> post = new ArrayList<>();

    /**
     * 集合所有的前置节点
     */
    private List<ExecutableWorkflowNode> pre = new ArrayList<>();

    //是否被调用，因为会有很多个前置节点同时进行调用，那么就只会有一个节点会成功发起请求
    //真正开始执行需要使用countDownLatch 进行判断是否已经同时到达这个栏栅
    //0是没有被call,1是被call了
    private volatile int isCalled = 0;

    /**
     * 默认构造函数
     */
    public ExecutableWorkflowNode() {
    }

    /**
     * 初始化构造函数，将节点信息同步到可执行node内部
     *
     * @param workflowNode 工作流节点配置对象，从中复制节点相关属性
     */
    //初始化，将节点信息同步到可执行node内部

    /**
     * ExecutableWorkflowNode方法。
     * * @param workflowNode WorkflowNode类型参数
     */
    public ExecutableWorkflowNode(WorkflowNode workflowNode) {
        super.setNodeId(workflowNode.getNodeId());
        super.setConnector(workflowNode.getConnector());
        super.setGroupId(workflowNode.getGroupId());
        super.setName(workflowNode.getName());
        super.setInvokeParameter(workflowNode.getInvokeParameter());
        super.setServiceUnit(workflowNode.getServiceUnit());
        super.setInvokeDynamic(workflowNode.getInvokeDynamic());
        super.setType(workflowNode.getType());
        super.setCache(workflowNode.getCache());
        this.setStatus(NodeLifeCycle.PREPARED);
        this.init();
    }

    // -------- Getter and Setter for this class --------

    /**
     * 获取节点状态
     *
     * @return 节点状态，参考NodeLifeCycle接口定义的状态常量
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置节点状态
     *
     * @param status 节点状态，参考NodeLifeCycle接口定义的状态常量
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取抽象执行引擎
     *
     * @return 抽象执行引擎实例
     */
    public AbstractEngine getAbstractEngine() {
        return abstractEngine;
    }

    /**
     * 设置抽象执行引擎
     *
     * @param abstractEngine 抽象执行引擎实例
     */
    public void setAbstractEngine(AbstractEngine abstractEngine) {
        this.abstractEngine = abstractEngine;
    }

    /**
     * 获取抽象引擎服务
     *
     * @return 抽象引擎服务实例
     */
    public AbstractEngineService getAbstractEngineService() {
        return abstractEngineService;
    }

    /**
     * 设置抽象引擎服务
     *
     * @param abstractEngineService 抽象引擎服务实例
     */
    public void setAbstractEngineService(AbstractEngineService abstractEngineService) {
        this.abstractEngineService = abstractEngineService;
    }

    /**
     * 获取前置节点计数闩
     *
     * @return CountDownLatch实例，用于控制前置节点的同步
     */
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /**
     * 设置前置节点计数闩
     *
     * @param countDownLatch CountDownLatch实例，用于控制前置节点的同步
     */
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    /**
     * 获取后置节点计数闩列表
     *
     * @return 后置节点CountDownLatch列表，用于通知后置节点
     */
    public List<CountDownLatch> getPostCountDownLatchList() {
        return postCountDownLatchList;
    }

    /**
     * 设置后置节点计数闩列表
     *
     * @param postCountDownLatchList 后置节点CountDownLatch列表
     */
    public void setPostCountDownLatchList(List<CountDownLatch> postCountDownLatchList) {
        this.postCountDownLatchList = postCountDownLatchList;
    }

    /**
     * 获取后置可执行节点列表
     *
     * @return 后置可执行工作流节点列表
     */
    public List<ExecutableWorkflowNode> getPost() {
        return post;
    }

    /**
     * 设置后置可执行节点列表
     *
     * @param post 后置可执行工作流节点列表
     */
    public void setPost(List<ExecutableWorkflowNode> post) {
        this.post = post;
    }

    /**
     * 获取前置可执行节点列表
     *
     * @return 前置可执行工作流节点列表
     */
    public List<ExecutableWorkflowNode> getPre() {
        return pre;
    }

    /**
     * 设置前置可执行节点列表
     *
     * @param pre 前置可执行工作流节点列表
     */
    public void setPre(List<ExecutableWorkflowNode> pre) {
        this.pre = pre;
    }

    /**
     * 获取节点是否被调用标志
     *
     * @return 调用标志，0表示未被调用，1表示已被调用
     */
    public int getIsCalled() {
        return isCalled;
    }

    /**
     * 设置节点是否被调用标志
     *
     * @param isCalled 调用标志，0表示未被调用，1表示已被调用
     */
    public void setIsCalled(int isCalled) {
        this.isCalled = isCalled;
    }

    // -------- Methods from NodeLifeCycle interface --------

    /**
     * 前处理方法，在节点执行前调用
     * 子类可重写此方法实现具体的前置处理逻辑
     */
    @Override
    /**
     * preExecute方法。
     */
    public void preExecute() {
    }

    /**
     * 后处理方法，在节点执行后调用
     * 子类可重写此方法实现具体的后置处理逻辑
     */
    @Override
    /**
     * postExecute方法。
     */
    public void postExecute() {
    }

    /**
     * 初始化方法，在节点创建时调用
     * 子类可重写此方法实现具体的初始化逻辑
     */
    @Override
    /**
     * init方法。
     */
    public void init() {
    }

    // -------- Other methods --------

    /**
     * 开始执行节点任务
     * 默认为同步执行操作，内部调用阻塞执行方法
     */
    //开始执行

    /**
     * exec方法。
     */
    public void exec() {
        //默认为同步执行操作
        blockExec();
    }

    /**
     * 阻塞执行节点任务
     * 同步执行当前节点的任务，处理前置节点数据并触发后置节点执行
     */
    private synchronized void blockExec() {
//
//        //当当前节点是准备状态就开始执行当前的任务
//        if (PREPARED.equals(status)) {
//            //为当前节点的执行服务设置参数，并执行
//            abstractEngineService.setInvokeParameter(getInvokeParameter());
//            abstractEngineService.exec(this);
//            dataset = abstractEngineService.getDataset();
//
//            for (CountDownLatch countDownLatch : postCountDownLatchList) {
//                countDownLatch.countDown();
//            }
//
//            for (ExecutableWorkflowNode executableWorkNode : post) {
//                //将当前的结果强塞给下一个节点
//                executableWorkNode.setDatasetPre(dataset);
//
//                //如果下个节点已经准备好执行条件
//                if (executableWorkNode.getCountDownLatch().getCount() == 0) {
//                    executableWorkNode.exec();
//                }
//            }
//
//        } else if (EXECUTED.equals(status)) {
//            //如果当前是已经执行过了的，就不真正的去执行当前指令
//
//            //将当前的结果再强制往后面塞一遍，并去调用下一个节点，执行权交由下个节点
//            for (ExecutableWorkflowNode executableWorkNode : post) {
//
//                //将当前的结果强塞给下一个节点
//                executableWorkNode.setDatasetPre(dataset);
//
//                //如果下个节点已经准备好执行条件
//                if (executableWorkNode.getCountDownLatch().getCount() == 0) {
//                    executableWorkNode.exec();
//                }
//            }
//        }
    }

    /**
     * 添加后置可执行节点，防止重复添加
     *
     * @param executableWorkNode 待添加的后置可执行工作流节点
     */
    public void putPost(ExecutableWorkflowNode executableWorkNode) {
        if (!post.contains(executableWorkNode)) {
            post.add(executableWorkNode);
        }
    }

    /**
     * 添加前置可执行节点，防止重复添加
     *
     * @param executableWorkNode 待添加的前置可执行工作流节点
     */
    public void putPre(ExecutableWorkflowNode executableWorkNode) {
        if (!pre.contains(executableWorkNode)) {
            pre.add(executableWorkNode);
        }
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ExecutableWorkflowNode{status=" + status + ", abstractEngine=" + abstractEngine + ", abstractEngineService=" + abstractEngineService + ", countDownLatch=" + countDownLatch + ", postCountDownLatchList=" + postCountDownLatchList + ", post=" + post + ", pre=" + pre + ", isCalled=" + isCalled + "}";
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
        if (!super.equals(o)) return false;
        ExecutableWorkflowNode that = (ExecutableWorkflowNode) o;
        return isCalled == that.isCalled &&
                Objects.equals(status, that.status) &&
                Objects.equals(abstractEngine, that.abstractEngine) &&
                Objects.equals(abstractEngineService, that.abstractEngineService) &&
                Objects.equals(countDownLatch, that.countDownLatch) &&
                Objects.equals(postCountDownLatchList, that.postCountDownLatchList) &&
                Objects.equals(post, that.post) &&
                Objects.equals(pre, that.pre);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, abstractEngine, abstractEngineService, countDownLatch, postCountDownLatchList, post, pre, isCalled);
    }
}
