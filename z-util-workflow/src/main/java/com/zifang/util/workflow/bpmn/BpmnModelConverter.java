package com.zifang.util.workflow.bpmn;

import com.zifang.util.workflow.config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BPMN模型到工作流配置的转换器。
 * <p>
 * 将BpmnDiagram模型转换为WorkflowConfiguration，实现BPMN流程定义到工作流配置的转换。
 * <p>
 * 支持的BPMN节点类型：
 * <ul>
 *   <li>startEvent, endEvent, userTask, serviceTask, scriptTask, manualTask</li>
 *   <li>exclusiveGateway, parallelGateway, inclusiveGateway</li>
 *   <li>callActivity (子流程)</li>
 * </ul>
 * <p>
 * 节点类型映射：
 * <ul>
 *   <li>startEvent -> "startEvent"</li>
 *   <li>endEvent -> "endEvent"</li>
 *   <li>userTask -> "userTask"</li>
 *   <li>serviceTask -> "serviceTask"</li>
 *   <li>scriptTask -> "scriptTask"</li>
 *   <li>manualTask -> "manualTask"</li>
 *   <li>exclusiveGateway -> "exclusiveGateway"</li>
 *   <li>parallelGateway -> "parallelGateway"</li>
 *   <li>inclusiveGateway -> "inclusiveGateway"</li>
 *   <li>callActivity -> "callActivity"</li>
 * </ul>
 *
 * @see BpmnDiagram
 * @see WorkflowConfiguration
 */
public class BpmnModelConverter {

    /**
     * Default engine type when converting BPMN.
     * Can be overridden via setEngineType() or constructor.
     */
    private String engineType = "java";

    /**
     * Default cache engine type.
     */
    private String cacheEngineType = "memory";

    /**
     * 默认构造函数，使用 "java" 作为默认引擎类型。
     */
    public BpmnModelConverter() {
    }

    /**
     * 使用指定的引擎类型构造转换器。
     *
     * @param engineType 引擎类型，如 "java"、"spark"、"python" 等
     * @throws IllegalArgumentException 如果 engineType 为空或 null
     */
    public BpmnModelConverter(String engineType) {
        if (engineType != null && !engineType.trim().isEmpty()) {
            this.engineType = engineType;
        }
    }

    /**
     * Set the engine type for the converted WorkflowConfiguration.
     *
     * @param engineType engine type (e.g., "java", "spark", "python")
     */
    public void setEngineType(String engineType) {
        if (engineType != null && !engineType.trim().isEmpty()) {
            this.engineType = engineType;
        }
    }

    /**
     * Set the cache engine type.
     *
     * @param cacheEngineType cache engine type (e.g., "memory", "redis")
     */
    public void setCacheEngineType(String cacheEngineType) {
        if (cacheEngineType != null && !cacheEngineType.trim().isEmpty()) {
            this.cacheEngineType = cacheEngineType;
        }
    }

    /**
     * Convert BpmnDiagram to WorkflowConfiguration
     *
     * @param diagram BPMN diagram model
     * @return WorkflowConfiguration
     */
    public WorkflowConfiguration convert(BpmnDiagram diagram) {
        WorkflowConfiguration config = new WorkflowConfiguration();

        // Set up global configurations
        config.setConfigurations(createConfigurations(diagram));

        // Build lookup map and convert nodes
        List<WorkflowNode> workflowNodes = convertNodes(diagram);

        // Wire up connections based on sequence flows
        wireConnections(workflowNodes, diagram);

        config.setWorkflowNodeList(workflowNodes);

        return config;
    }

    private Configurations createConfigurations(BpmnDiagram diagram) {
        Configurations configurations = new Configurations();

        Engine engine = new Engine();
        engine.setType(engineType);
        engine.setMode("local");
        configurations.setEngine(engine);

        CacheEngine cacheEngine = new CacheEngine();
        cacheEngine.setEngineType(cacheEngineType);
        configurations.setCacheEngine(cacheEngine);

        // Store BPMN process id as workflow configuration id hint
        if (diagram.getId() != null) {
            configurations.setWorkflowConfigurationId(diagram.getId().hashCode());
        }

        return configurations;
    }

    private List<WorkflowNode> convertNodes(BpmnDiagram diagram) {
        List<WorkflowNode> workflowNodes = new ArrayList<>();

        for (BpmnDiagram.BpmnNode bpmnNode : diagram.getNodes()) {
            WorkflowNode workflowNode = convertNode(bpmnNode);
            workflowNodes.add(workflowNode);
        }

        return workflowNodes;
    }

    private WorkflowNode convertNode(BpmnDiagram.BpmnNode bpmnNode) {
        WorkflowNode workflowNode = new WorkflowNode();

        workflowNode.setNodeId(bpmnNode.getId());
        workflowNode.setName(bpmnNode.getName());
        workflowNode.setType(mapNodeType(bpmnNode.getType()));

        // Set up connector with empty pre/post lists (will be wired later)
        Connector connector = new Connector();
        connector.setPre(new ArrayList<>());
        connector.setPost(new ArrayList<>());
        workflowNode.setConnector(connector);

        // Set service unit based on node type
        workflowNode.setServiceUnit(getServiceUnit(bpmnNode.getType()));

        // Copy properties from BPMN node
        // documentation -> invokeParameter (used as description)
        if (bpmnNode.getProperty("documentation") != null) {
            workflowNode.setInvokeParameter(bpmnNode.getProperty("documentation"));
        }

        // loopCharacteristics -> stored in cache map
        if (bpmnNode.getProperty("isSequentialLoop") != null
                || bpmnNode.getProperty("isParallelLoop") != null
                || bpmnNode.getProperty("loopMaximum") != null) {
            HashMap<String, String> cache = new HashMap<>();
            if (bpmnNode.getProperty("isSequentialLoop") != null) {
                cache.put("loopType", "sequential");
            }
            if (bpmnNode.getProperty("isParallelLoop") != null) {
                cache.put("loopType", "parallel");
            }
            if (bpmnNode.getProperty("loopMaximum") != null) {
                cache.put("loopMaximum", bpmnNode.getProperty("loopMaximum"));
            }
            workflowNode.setCache(cache);
        }

        // calledElement (callActivity) -> stored in cache
        if (bpmnNode.getProperty("calledElement") != null) {
            HashMap<String, String> cache = workflowNode.getCache();
            if (cache == null) {
                cache = new HashMap<>();
            }
            cache.put("calledElement", bpmnNode.getProperty("calledElement"));
            workflowNode.setCache(cache);
        }

        // script (scriptTask) -> stored in cache
        if (bpmnNode.getProperty("script") != null) {
            HashMap<String, String> cache = workflowNode.getCache();
            if (cache == null) {
                cache = new HashMap<>();
            }
            cache.put("script", bpmnNode.getProperty("script"));
            workflowNode.setCache(cache);
        }

        // implementation (serviceTask) -> stored in cache
        if (bpmnNode.getProperty("implementation") != null) {
            HashMap<String, String> cache = workflowNode.getCache();
            if (cache == null) {
                cache = new HashMap<>();
            }
            cache.put("implementation", bpmnNode.getProperty("implementation"));
            workflowNode.setCache(cache);
        }

        return workflowNode;
    }

    private String mapNodeType(String bpmnType) {
        if (bpmnType == null) {
            return "task";
        }

        switch (bpmnType.toLowerCase()) {
            case "startevent":
                return "startEvent";
            case "endevent":
                return "endEvent";
            case "usertask":
                return "userTask";
            case "servicetask":
                return "serviceTask";
            case "scripttask":
                return "scriptTask";
            case "manualtask":
                return "manualTask";
            case "sendtask":
                return "sendTask";
            case "receivetask":
                return "receiveTask";
            case "exclusivegateway":
                return "exclusiveGateway";
            case "parallelgateway":
                return "parallelGateway";
            case "inclusivegateway":
                return "inclusiveGateway";
            case "callactivity":
                return "callActivity";
            case "task":
                return "task";
            case "subprocess":
            case "adhocsubprocess":
                return "subProcess";
            case "transaction":
                return "transaction";
            default:
                return bpmnType.toLowerCase();
        }
    }

    private String getServiceUnit(String nodeType) {
        if (nodeType == null) {
            return "empty";
        }

        switch (nodeType.toLowerCase()) {
            case "usertask":
                return "userTaskHandler";
            case "servicetask":
                return "serviceTaskHandler";
            case "scripttask":
                return "scriptTaskHandler";
            case "manualtask":
                return "manualTaskHandler";
            case "sendtask":
                return "sendTaskHandler";
            case "receivetask":
                return "receiveTaskHandler";
            case "exclusivegateway":
            case "inclusivegateway":
                return "gatewayHandler";
            case "parallelgateway":
                return "parallelGatewayHandler";
            case "callactivity":
                return "callActivityHandler";
            case "startevent":
            case "endevent":
                return "eventHandler";
            default:
                return "empty";
        }
    }

    /**
     * Wire connections between nodes based on sequence flows.
     * For exclusive gateways, the condition expression on the sequence flow
     * is set as the invokeParameter on the TARGET node (the next node after the gateway).
     * This is because the WorkflowRuntimeEngine evaluates conditions from post-nodes.
     * <p>
     * For parallel gateways, all post-nodes are activated unconditionally.
     * <p>
     * For inclusive gateways, all conditions are evaluated and all matching paths are taken.
     */
    private void wireConnections(List<WorkflowNode> workflowNodes, BpmnDiagram diagram) {
        // Build node lookup map by id
        Map<String, WorkflowNode> nodeMap = new HashMap<>();
        for (WorkflowNode node : workflowNodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        // Build sequence flow lookup by source to get default flow info
        Map<String, String> defaultFlowMap = new HashMap<>();
        Map<String, String> conditionFlowMap = new HashMap<>();
        for (BpmnDiagram.BpmnSequenceFlow flow : diagram.getSequenceFlows()) {
            if (flow.isDefault()) {
                defaultFlowMap.put(flow.getSourceRef(), flow.getId());
            }
            if (flow.getConditionExpression() != null && !flow.getConditionExpression().isEmpty()) {
                conditionFlowMap.put(flow.getSourceRef() + "->" + flow.getTargetRef(), flow.getConditionExpression());
            }
        }

        // Wire based on sequence flows
        for (BpmnDiagram.BpmnSequenceFlow flow : diagram.getSequenceFlows()) {
            WorkflowNode sourceNode = nodeMap.get(flow.getSourceRef());
            WorkflowNode targetNode = nodeMap.get(flow.getTargetRef());

            if (sourceNode != null && targetNode != null) {
                // Add target to source's post list
                if (!sourceNode.getConnector().getPost().contains(flow.getTargetRef())) {
                    sourceNode.getConnector().getPost().add(flow.getTargetRef());
                }

                // Add source to target's pre list
                if (!targetNode.getConnector().getPre().contains(flow.getSourceRef())) {
                    targetNode.getConnector().getPre().add(flow.getSourceRef());
                }

                // For exclusive gateway outgoing flows with condition:
                // Store the condition on the target node so the runtime can evaluate it.
                // The WorkflowRuntimeEngine.executeGateway() reads conditions from post-nodes.
                if (flow.getConditionExpression() != null && !flow.getConditionExpression().isEmpty()) {
                    // If target already has an invokeParameter (e.g., documentation), 
                    // preserve it and add condition as a cache entry instead
                    if (targetNode.getInvokeParameter() != null) {
                        HashMap<String, String> cache = (HashMap<String, String>) targetNode.getCache();
                        if (cache == null) {
                            cache = new HashMap<>();
                            targetNode.setCache(cache);
                        }
                        cache.put("conditionExpression", flow.getConditionExpression());
                    } else {
                        targetNode.setInvokeParameter(flow.getConditionExpression());
                    }
                }

                // Mark if this is a default flow for the source gateway
                if (flow.isDefault()) {
                    HashMap<String, String> cache = (HashMap<String, String>) sourceNode.getCache();
                    if (cache == null) {
                        cache = new HashMap<>();
                        sourceNode.setCache(cache);
                    }
                    cache.put("defaultFlow", flow.getTargetRef());
                }
            }
        }
    }
}
