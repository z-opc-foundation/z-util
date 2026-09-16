package com.zifang.util.workflow.engine.runtime;

import com.zifang.util.workflow.config.Connector;
import com.zifang.util.workflow.config.ExecutableWorkflowNode;
import com.zifang.util.workflow.config.WorkflowConfiguration;
import com.zifang.util.workflow.config.WorkflowNode;
import com.zifang.util.workflow.engine.interfaces.AbstractEngine;
import com.zifang.util.workflow.engine.interfaces.AbstractEngineService;
import com.zifang.util.workflow.engine.interfaces.EngineFactory;
import com.zifang.util.workflow.persistence.WorkflowPersistencePlugin;
import com.zifang.util.workflow.persistence.WorkflowSnapshot;
import com.zifang.util.json.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流运行时执行引擎。
 * 负责管理工作流执行、节点连线、网关评估和状态流转。
 * 支持并行网关和排他网关的条件分支执行。
 *
 * @see ExecutionResult
 * @see GatewayEvaluator
 * @see ExecutableWorkflowNode
 */
public class WorkflowRuntimeEngine {

    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PAUSED = "PAUSED";

    private WorkflowConfiguration configuration;
    private Map<String, ExecutableWorkflowNode> nodeMap;
    private Map<String, Object> variables;
    private ExecutionResult currentResult;
    private String processId;
    private AtomicInteger nodeIdGenerator;
    private AbstractEngine abstractEngine;
    private WorkflowPersistencePlugin persistencePlugin;

    /**
     * 构造函数，初始化运行时引擎。
     */
    public WorkflowRuntimeEngine() {
        this.nodeMap = new HashMap<>();
        this.variables = new HashMap<>();
        this.currentResult = new ExecutionResult();
        this.nodeIdGenerator = new AtomicInteger(0);
    }

    /**
     * 使用工作流配置初始化运行时引擎。
     * 构建节点映射、建立节点连线、设置CountDownLatch。
     *
     * @param configuration 工作流配置
     * @throws IllegalArgumentException 如果配置为空或节点列表为空
     */
    public void initialize(WorkflowConfiguration configuration) {
        this.configuration = configuration;
        this.processId = "process-" + nodeIdGenerator.getAndIncrement();
        this.nodeMap.clear();
        this.variables.clear();

        // Build nodeMap from configuration.nodes
        buildNodeMap();

        // Wire up pre/post connections
        wireConnections();

        // Set countDownLatch on each node based on pre node count
        initializeCountDownLatches();

        // Initialize execution result
        initializeExecutionResult();
    }

    private void buildNodeMap() {
        if (configuration.getWorkflowNodeList() == null) {
            return;
        }

        // Initialize engine
        if (configuration.getConfigurations() != null) {
            this.abstractEngine = EngineFactory.getEngine(configuration.getConfigurations().getEngine());
        }

        for (WorkflowNode workflowNode : configuration.getWorkflowNodeList()) {
            ExecutableWorkflowNode executableNode = new ExecutableWorkflowNode(workflowNode);
            executableNode.setAbstractEngine(abstractEngine);
            executableNode.setStatus("prepared");
            nodeMap.put(workflowNode.getNodeId(), executableNode);
        }
    }

    private void wireConnections() {
        for (ExecutableWorkflowNode node : nodeMap.values()) {
            Connector connector = node.getConnector();
            if (connector == null) {
                continue;
            }

            List<String> preList = connector.getPre();
            List<String> postList = connector.getPost();

            // Wire pre nodes
            if (preList != null) {
                for (String preId : preList) {
                    ExecutableWorkflowNode preNode = nodeMap.get(preId);
                    if (preNode != null) {
                        node.putPre(preNode);
                        preNode.putPost(node);
                    }
                }
            }

            // Wire post nodes
            if (postList != null) {
                for (String postId : postList) {
                    ExecutableWorkflowNode postNode = nodeMap.get(postId);
                    if (postNode != null) {
                        node.putPost(postNode);
                    }
                }
            }
        }
    }

    private void initializeCountDownLatches() {
        for (ExecutableWorkflowNode node : nodeMap.values()) {
            CountDownLatch latch = new CountDownLatch(node.getPre().size());
            node.setCountDownLatch(latch);
        }

        // Initialize post count down latches
        for (ExecutableWorkflowNode node : nodeMap.values()) {
            for (ExecutableWorkflowNode postNode : node.getPost()) {
                node.getPostCountDownLatchList().add(postNode.getCountDownLatch());
            }
        }
    }

    private void initializeExecutionResult() {
        currentResult = ExecutionResult.builder()
                .processId(processId)
                .status(STATUS_RUNNING)
                .variables(new HashMap<>(variables))
                .completedNodes(new ArrayList<>())
                .pendingNodes(new ArrayList<>(nodeMap.keySet()))
                .pendingUserTaskNodes(findUserTaskNodes())
                .build();
    }

    private List<String> findUserTaskNodes() {
        List<String> userTaskNodes = new ArrayList<>();
        for (Map.Entry<String, ExecutableWorkflowNode> entry : nodeMap.entrySet()) {
            String type = entry.getValue().getType();
            if ("userTask".equalsIgnoreCase(type)) {
                userTaskNodes.add(entry.getKey());
            }
        }
        return userTaskNodes;
    }

    /**
     * 启动工作流执行，从起始节点开始执行（没有前置节点的节点）。
     *
     * @return 执行结果
     * @see #resume(String)
     */
    public ExecutionResult start() {
        List<ExecutableWorkflowNode> startNodes = findStartNodes();
        if (startNodes.isEmpty()) {
            currentResult.setStatus(STATUS_COMPLETED);
            return currentResult;
        }

        currentResult.setStatus(STATUS_RUNNING);

        // Execute start nodes
        for (ExecutableWorkflowNode startNode : startNodes) {
            executeNode(startNode);
        }

        // Check if workflow completed
        checkWorkflowCompletion();

        return currentResult;
    }

    /**
     * 在指定节点恢复执行（例如用户完成任务后）。
     *
     * @param nodeId 要恢复执行的节点ID
     * @return 执行结果
     * @throws IllegalStateException 如果节点不存在
     */
    public ExecutionResult resume(String nodeId) {
        ExecutableWorkflowNode node = nodeMap.get(nodeId);
        if (node == null) {
            currentResult.setStatus(STATUS_FAILED);
            currentResult.setErrorMessage("Node not found: " + nodeId);
            return currentResult;
        }

        // Mark node as ready to execute
        executeNode(node);

        // Check if workflow completed
        checkWorkflowCompletion();

        return currentResult;
    }

    private List<ExecutableWorkflowNode> findStartNodes() {
        List<ExecutableWorkflowNode> startNodes = new ArrayList<>();
        for (ExecutableWorkflowNode node : nodeMap.values()) {
            if (node.getPre().isEmpty()) {
                startNodes.add(node);
            }
        }
        return startNodes;
    }

    private void executeNode(ExecutableWorkflowNode node) {
        String nodeId = node.getNodeId();
        String type = node.getType();

        currentResult.setCurrentNodeId(nodeId);
        node.setStatus("started");

        // Handle gateway evaluation
        if ("exclusiveGateway".equalsIgnoreCase(type)) {
            executeGateway(node);
        } else if ("parallelGateway".equalsIgnoreCase(type)) {
            executeParallelGateway(node);
        } else {
            // Regular node execution
            executeServiceNode(node);
        }
    }

    private void executeGateway(ExecutableWorkflowNode gatewayNode) {
        // Evaluate conditions to determine the outgoing path
        List<ExecutableWorkflowNode> postNodes = gatewayNode.getPost();
        GatewayEvaluator evaluator = new GatewayEvaluator();

        String condition = (String) gatewayNode.getInvokeParameter();
        ExecutableWorkflowNode selectedNode = null;

        for (ExecutableWorkflowNode postNode : postNodes) {
            String postCondition = (String) postNode.getInvokeParameter();
            if (evaluator.evaluate(postCondition, variables)) {
                selectedNode = postNode;
                break;
            }
        }

        gatewayNode.setStatus("executed");
        currentResult.addCompletedNode(gatewayNode.getNodeId());
        currentResult.removePendingNode(gatewayNode.getNodeId());

        if (selectedNode != null) {
            executeNode(selectedNode);
        }
    }

    private void executeParallelGateway(ExecutableWorkflowNode gatewayNode) {
        // For parallel gateway, activate all outgoing nodes
        List<ExecutableWorkflowNode> postNodes = gatewayNode.getPost();

        gatewayNode.setStatus("executed");
        currentResult.addCompletedNode(gatewayNode.getNodeId());
        currentResult.removePendingNode(gatewayNode.getNodeId());

        // Notify all post nodes
        for (ExecutableWorkflowNode postNode : postNodes) {
            postNode.getCountDownLatch().countDown();
            executeNode(postNode);
        }
    }

    private void executeServiceNode(ExecutableWorkflowNode node) {
        String nodeId = node.getNodeId();
        String serviceUnit = node.getServiceUnit();

        try {
            // Initialize engine service if not already done
            if (node.getAbstractEngineService() == null && abstractEngine != null) {
                AbstractEngineService engineService = abstractEngine.getRegisteredEngineService(serviceUnit);
                node.setAbstractEngineService(engineService);
            }

            // Execute the service
            if (node.getAbstractEngineService() != null) {
                node.getAbstractEngineService().setInvokeParameter(node.getInvokeParameter());
                node.getAbstractEngineService().exec(node);
            }

            node.setStatus("executed");
            currentResult.addCompletedNode(nodeId);
            currentResult.removePendingNode(nodeId);

            // Handle user task nodes specially
            if ("userTask".equalsIgnoreCase(node.getType())) {
                currentResult.addPendingUserTaskNode(nodeId);
                currentResult.setStatus(STATUS_PAUSED);
                saveSnapshot();
                return;
            }

            // Notify post nodes
            notifyPostNodes(node);

        } catch (Exception e) {
            node.setStatus("failed");
            currentResult.setStatus(STATUS_FAILED);
            currentResult.setErrorMessage("Node execution failed: " + nodeId + " - " + e.getMessage());
            saveSnapshot();
        }
    }

    private void notifyPostNodes(ExecutableWorkflowNode node) {
        List<ExecutableWorkflowNode> postNodes = node.getPost();

        for (ExecutableWorkflowNode postNode : postNodes) {
            // Decrement the post node's countdown latch
            CountDownLatch latch = postNode.getCountDownLatch();
            if (latch != null && latch.getCount() > 0) {
                latch.countDown();
            }

            // If all pre nodes are done, execute this post node
            if (postNode.getCountDownLatch().getCount() == 0) {
                // Check if node is not already executed
                if (!"executed".equals(postNode.getStatus())) {
                    executeNode(postNode);
                }
            }
        }
    }

    private void checkWorkflowCompletion() {
        boolean allCompleted = true;
        for (ExecutableWorkflowNode node : nodeMap.values()) {
            if (!"executed".equals(node.getStatus())) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            currentResult.setStatus(STATUS_COMPLETED);
            currentResult.setCurrentNodeId(null);
            saveSnapshot();
        }
    }

    /**
     * 获取当前执行结果。
     *
     * @return 当前执行结果
     */
    public ExecutionResult getCurrentResult() {
        return currentResult;
    }

    /**
     * 获取待处理的用户任务节点列表。
     *
     * @return 待处理用户任务节点ID列表
     */
    public List<String> getPendingUserTasks() {
        return currentResult.getPendingUserTaskNodes();
    }

    /**
     * 设置运行时变量。
     *
     * @param key   变量键名
     * @param value 变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
        if (currentResult.getVariables() != null) {
            currentResult.getVariables().put(key, value);
        }
    }

    /**
     * 获取运行时变量。
     *
     * @param key 变量键名
     * @return 变量值，如果不存在则返回null
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 获取所有运行时变量。
     *
     * @return 变量映射表的副本
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }

    // -------- Persistence Plugin Methods --------

    /**
     * Set the persistence plugin for snapshot save/restore.
     *
     * @param plugin the persistence plugin implementation
     */
    public void setPersistencePlugin(WorkflowPersistencePlugin plugin) {
        this.persistencePlugin = plugin;
    }

    /**
     * Save the current workflow state as a snapshot.
     * If no persistence plugin is set, this method does nothing.
     */
    public void saveSnapshot() {
        if (persistencePlugin == null) {
            return;
        }
        WorkflowSnapshot snapshot = buildSnapshot();
        persistencePlugin.save(snapshot);
    }

    /**
     * Restore workflow state from a previously saved snapshot.
     * This reconstructs the nodeMap, variables, execution result, etc.
     *
     * @param processId the process ID to restore
     */
    public void restoreFromSnapshot(String processId) {
        if (persistencePlugin == null) {
            throw new IllegalStateException("Persistence plugin is not set");
        }

        WorkflowSnapshot snapshot = persistencePlugin.load(processId);
        if (snapshot == null) {
            throw new IllegalArgumentException("No snapshot found for process: " + processId);
        }

        // Restore basic state
        this.processId = snapshot.getProcessId();
        this.variables = new HashMap<>();
        if (snapshot.getVariables() != null) {
            this.variables.putAll(snapshot.getVariables());
        }

        // Reconstruct configuration from JSON
        if (snapshot.getWorkflowConfigurationJson() != null) {
            try {
                this.configuration = JsonUtil.fromJson(snapshot.getWorkflowConfigurationJson(), WorkflowConfiguration.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize workflow configuration for process: " + processId, e);
            }
        }

        // Rebuild nodeMap from configuration
        this.nodeMap.clear();
        if (configuration != null && configuration.getWorkflowNodeList() != null) {
            for (WorkflowNode workflowNode : configuration.getWorkflowNodeList()) {
                ExecutableWorkflowNode executableNode = new ExecutableWorkflowNode(workflowNode);
                nodeMap.put(workflowNode.getNodeId(), executableNode);
            }
        }

        // Restore node statuses
        if (snapshot.getCompletedNodes() != null) {
            for (String nodeId : snapshot.getCompletedNodes()) {
                ExecutableWorkflowNode node = nodeMap.get(nodeId);
                if (node != null) {
                    node.setStatus("executed");
                }
            }
        }
        if (snapshot.getPendingNodes() != null) {
            for (String nodeId : snapshot.getPendingNodes()) {
                ExecutableWorkflowNode node = nodeMap.get(nodeId);
                if (node != null && node.getStatus() == null) {
                    node.setStatus("pending");
                }
            }
        }

        // Restore currentResult
        this.currentResult = new ExecutionResult();
        this.currentResult.setProcessId(snapshot.getProcessId());
        this.currentResult.setStatus(snapshot.getStatus());
        this.currentResult.setCurrentNodeId(snapshot.getCurrentNodeId());
        this.currentResult.setErrorMessage(snapshot.getErrorMessage());
        this.currentResult.setCompletedNodes(new ArrayList<>());
        this.currentResult.setPendingNodes(new ArrayList<>());
        this.currentResult.setPendingUserTaskNodes(new ArrayList<>());

        if (snapshot.getCompletedNodes() != null) {
            this.currentResult.getCompletedNodes().addAll(snapshot.getCompletedNodes());
        }
        if (snapshot.getPendingNodes() != null) {
            this.currentResult.getPendingNodes().addAll(snapshot.getPendingNodes());
        }
        if (snapshot.getPendingUserTaskNodes() != null) {
            this.currentResult.getPendingUserTaskNodes().addAll(snapshot.getPendingUserTaskNodes());
        }
        if (snapshot.getVariables() != null) {
            this.currentResult.setVariables(new HashMap<>(snapshot.getVariables()));
        }
    }

    /**
     * Build a WorkflowSnapshot from the current engine state.
     */
    private WorkflowSnapshot buildSnapshot() {
        WorkflowSnapshot snapshot = new WorkflowSnapshot();
        snapshot.setProcessId(processId);
        snapshot.setStatus(currentResult.getStatus());
        snapshot.setCompletedNodes(new ArrayList<>(currentResult.getCompletedNodes()));
        snapshot.setPendingNodes(new ArrayList<>(currentResult.getPendingNodes()));
        snapshot.setPendingUserTaskNodes(new ArrayList<>(currentResult.getPendingUserTaskNodes()));
        snapshot.setVariables(new HashMap<>(variables));
        snapshot.setCurrentNodeId(currentResult.getCurrentNodeId());
        snapshot.setErrorMessage(currentResult.getErrorMessage());
        snapshot.setLastUpdatedTime(System.currentTimeMillis());

        // Serialize configuration to JSON
        if (configuration != null) {
            try {
                snapshot.setWorkflowConfigurationJson(JsonUtil.toJson(configuration));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize workflow configuration for process: " + processId, e);
            }
        }

        return snapshot;
    }

    // -------- Getters and Setters --------

    /**
     * 获取工作流配置。
     *
     * @return 工作流配置
     */
    public WorkflowConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获取节点映射表。
     *
     * @return 节点ID到可执行节点的映射
     */
    public Map<String, ExecutableWorkflowNode> getNodeMap() {
        return nodeMap;
    }

    /**
     * 获取流程实例ID。
     *
     * @return 流程实例ID
     */
    public String getProcessId() {
        return processId;
    }
}