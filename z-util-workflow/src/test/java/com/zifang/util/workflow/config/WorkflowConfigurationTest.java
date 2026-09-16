package com.zifang.util.workflow.config;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * WorkflowConfiguration 类测试
 */

/**
 * WorkflowConfigurationTest类。
 */
public class WorkflowConfigurationTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        assertNotNull(config);
        assertNull(config.getConfigurations());
        assertNull(config.getWorkflowNodeList());
    }

    @Test
    /**
     * testConstructorWithParameters方法。
     */
    public void testConstructorWithParameters() {
        Configurations configurations = new Configurations();
        configurations.setWorkflowConfigurationId(1);

        List<WorkflowNode> nodes = new ArrayList<>();
        WorkflowNode node = new WorkflowNode();
        node.setNodeId("node-001");
        nodes.add(node);

        WorkflowConfiguration config = new WorkflowConfiguration(configurations, nodes);

        assertNotNull(config.getConfigurations());
        assertEquals(1, config.getConfigurations().getWorkflowConfigurationId().intValue());
        assertNotNull(config.getWorkflowNodeList());
        assertEquals(1, config.getWorkflowNodeList().size());
    }

    @Test
    /**
     * testConfigurationsGetterAndSetter方法。
     */
    public void testConfigurationsGetterAndSetter() {
        WorkflowConfiguration config = new WorkflowConfiguration();

        Configurations configurations = new Configurations();
        configurations.setWorkflowConfigurationId(100);

        config.setConfigurations(configurations);

        assertNotNull(config.getConfigurations());
        assertEquals(100, config.getConfigurations().getWorkflowConfigurationId().intValue());
    }

    @Test
    /**
     * testWorkflowNodeListGetterAndSetter方法。
     */
    public void testWorkflowNodeListGetterAndSetter() {
        WorkflowConfiguration config = new WorkflowConfiguration();

        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(new WorkflowNode());
        nodes.add(new WorkflowNode());

        config.setWorkflowNodeList(nodes);

        assertNotNull(config.getWorkflowNodeList());
        assertEquals(2, config.getWorkflowNodeList().size());
    }

    @Test
    /**
     * testWorkflowNodeListCanBeNull方法。
     */
    public void testWorkflowNodeListCanBeNull() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        config.setWorkflowNodeList(null);
        assertNull(config.getWorkflowNodeList());
    }

    @Test
    /**
     * testConfigurationsCanBeNull方法。
     */
    public void testConfigurationsCanBeNull() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        config.setConfigurations(null);
        assertNull(config.getConfigurations());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        Configurations configs1 = new Configurations();
        configs1.setWorkflowConfigurationId(1);

        Configurations configs2 = new Configurations();
        configs2.setWorkflowConfigurationId(1);

        List<WorkflowNode> nodes1 = new ArrayList<>();
        WorkflowNode node1 = new WorkflowNode();
        node1.setNodeId("node1");
        nodes1.add(node1);

        List<WorkflowNode> nodes2 = new ArrayList<>();
        WorkflowNode node2 = new WorkflowNode();
        node2.setNodeId("node1");
        nodes2.add(node2);

        WorkflowConfiguration config1 = new WorkflowConfiguration(configs1, nodes1);
        WorkflowConfiguration config2 = new WorkflowConfiguration(configs2, nodes2);

        assertEquals(config1, config2);
    }

    @Test
    /**
     * testEqualsWithDifferentIds方法。
     */
    public void testEqualsWithDifferentIds() {
        Configurations configs1 = new Configurations();
        configs1.setWorkflowConfigurationId(1);

        Configurations configs2 = new Configurations();
        configs2.setWorkflowConfigurationId(2);

        WorkflowConfiguration config1 = new WorkflowConfiguration(configs1, null);
        WorkflowConfiguration config2 = new WorkflowConfiguration(configs2, null);

        assertNotEquals(config1, config2);
    }

    @Test
    /**
     * testEqualsWithSameObject方法。
     */
    public void testEqualsWithSameObject() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        assertEquals(config, config);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        assertNotEquals(config, null);
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        assertNotEquals(config, "not a workflow configuration");
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        Configurations configs = new Configurations();
        configs.setWorkflowConfigurationId(1);

        List<WorkflowNode> nodes = new ArrayList<>();
        WorkflowNode node = new WorkflowNode();
        node.setNodeId("node1");
        nodes.add(node);

        WorkflowConfiguration config1 = new WorkflowConfiguration(configs, nodes);
        WorkflowConfiguration config2 = new WorkflowConfiguration(configs, nodes);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        Configurations configs = new Configurations();
        configs.setWorkflowConfigurationId(1);

        WorkflowConfiguration config = new WorkflowConfiguration(configs, null);

        int hashCode1 = config.hashCode();
        int hashCode2 = config.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        WorkflowConfiguration config = new WorkflowConfiguration();
        String str = config.toString();

        assertNotNull(str);
        assertTrue(str.contains("WorkflowConfiguration"));
    }

    @Test
    /**
     * testComplexWorkflowConfiguration方法。
     */
    public void testComplexWorkflowConfiguration() {
        // 创建 Engine 配置
        Engine engine = new Engine();
        engine.setType("spark");
        engine.setMode("cluster");

        Map<String, String> engineProps = new HashMap<>();
        engineProps.put("memory", "8g");
        engineProps.put("cores", "4");
        engine.setProperties(engineProps);

        // 创建 CacheEngine 配置
        CacheEngine cacheEngine = new CacheEngine();
        cacheEngine.setEngineType("redis");
        cacheEngine.setCacheEngineService("redis://localhost:6379");

        // 创建全局配置
        Configurations configurations = new Configurations();
        configurations.setWorkflowConfigurationId(1);
        configurations.setEngine(engine);
        configurations.setCacheEngine(cacheEngine);

        Map<String, String> personalEnv = new HashMap<>();
        personalEnv.put("user", "admin");
        configurations.setPersonalEnvironment(personalEnv);

        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("mode", "production");
        configurations.setRuntimeParameter(runtimeParams);

        // 创建工作流节点列表
        List<WorkflowNode> workflowNodes = new ArrayList<>();

        // 节点1: 开始节点
        WorkflowNode startNode = new WorkflowNode();
        startNode.setNodeId("start-node");
        startNode.setName("Start");
        startNode.setType("start");
        startNode.setServiceUnit("workflow-engine");
        startNode.setInvokeDynamic("start");

        Connector startConnector = new Connector();
        startConnector.setPre(null);
        startConnector.setPost(new ArrayList<>());
        startConnector.getPost().add("process-node");
        startNode.setConnector(startConnector);

        // 节点2: 处理节点
        WorkflowNode processNode = new WorkflowNode();
        processNode.setNodeId("process-node");
        processNode.setName("Process Data");
        processNode.setType("process");
        processNode.setServiceUnit("spark-service");
        processNode.setInvokeDynamic("processData");

        Map<String, Object> params = new HashMap<>();
        params.put("inputPath", "/data/input");
        params.put("outputPath", "/data/output");
        processNode.setInvokeParameter(params);

        Connector processConnector = new Connector();
        processConnector.setPre(new ArrayList<>());
        processConnector.getPre().add("start-node");
        processConnector.setPost(new ArrayList<>());
        processConnector.getPost().add("end-node");
        processNode.setConnector(processConnector);

        // 节点3: 结束节点
        WorkflowNode endNode = new WorkflowNode();
        endNode.setNodeId("end-node");
        endNode.setName("End");
        endNode.setType("end");
        endNode.setServiceUnit("workflow-engine");
        endNode.setInvokeDynamic("end");

        Connector endConnector = new Connector();
        endConnector.setPre(new ArrayList<>());
        endConnector.getPre().add("process-node");
        endConnector.setPost(null);
        endNode.setConnector(endConnector);

        workflowNodes.add(startNode);
        workflowNodes.add(processNode);
        workflowNodes.add(endNode);

        // 创建完整的工作流配置
        WorkflowConfiguration config = new WorkflowConfiguration(configurations, workflowNodes);

        // 验证配置
        assertNotNull(config.getConfigurations());
        assertEquals(1, config.getConfigurations().getWorkflowConfigurationId().intValue());
        assertEquals("spark", config.getConfigurations().getEngine().getType());
        assertEquals("redis", config.getConfigurations().getCacheEngine().getEngineType());
        assertEquals("admin", config.getConfigurations().getPersonalEnvironment().get("user"));

        assertNotNull(config.getWorkflowNodeList());
        assertEquals(3, config.getWorkflowNodeList().size());

        // 验证节点连接关系
        WorkflowNode retrievedStartNode = config.getWorkflowNodeList().get(0);
        assertEquals("start-node", retrievedStartNode.getNodeId());
        assertEquals(1, retrievedStartNode.getConnector().getPost().size());
        assertEquals("process-node", retrievedStartNode.getConnector().getPost().get(0));

        WorkflowNode retrievedProcessNode = config.getWorkflowNodeList().get(1);
        assertEquals("process-node", retrievedProcessNode.getNodeId());
        assertEquals(1, retrievedProcessNode.getConnector().getPre().size());
        assertEquals("start-node", retrievedProcessNode.getConnector().getPre().get(0));
    }
}
