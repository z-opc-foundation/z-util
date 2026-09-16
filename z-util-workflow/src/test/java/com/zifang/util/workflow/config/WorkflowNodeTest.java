package com.zifang.util.workflow.config;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * WorkflowNode 类测试
 */

/**
 * WorkflowNodeTest类。
 */
public class WorkflowNodeTest {

    private WorkflowNode node;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        node = new WorkflowNode();
    }

    @Test
    /**
     * testWorkflowNodeCreation方法。
     */
    public void testWorkflowNodeCreation() {
        assertNotNull(node);
    }

    @Test
    /**
     * testNodeId方法。
     */
    public void testNodeId() {
        node.setNodeId("node-001");
        assertEquals("node-001", node.getNodeId());

        node.setNodeId("start-node");
        assertEquals("start-node", node.getNodeId());
    }

    @Test
    /**
     * testGroupId方法。
     */
    public void testGroupId() {
        node.setGroupId("group-001");
        assertEquals("group-001", node.getGroupId());

        node.setGroupId("data-processing");
        assertEquals("data-processing", node.getGroupId());
    }

    @Test
    /**
     * testName方法。
     */
    public void testName() {
        node.setName("数据读取");
        assertEquals("数据读取", node.getName());

        node.setName("Data Reader");
        assertEquals("Data Reader", node.getName());
    }

    @Test
    /**
     * testType方法。
     */
    public void testType() {
        node.setType("input");
        assertEquals("input", node.getType());

        node.setType("process");
        assertEquals("process", node.getType());

        node.setType("output");
        assertEquals("output", node.getType());
    }

    @Test
    /**
     * testServiceUnit方法。
     */
    public void testServiceUnit() {
        node.setServiceUnit("java-service");
        assertEquals("java-service", node.getServiceUnit());

        node.setServiceUnit("python-service");
        assertEquals("python-service", node.getServiceUnit());
    }

    @Test
    /**
     * testInvokeDynamic方法。
     */
    public void testInvokeDynamic() {
        node.setInvokeDynamic("handleData");
        assertEquals("handleData", node.getInvokeDynamic());

        node.setInvokeDynamic("processRecord");
        assertEquals("processRecord", node.getInvokeDynamic());
    }

    @Test
    /**
     * testInvokeParameter方法。
     */
    public void testInvokeParameter() {
        // 测试参数可以是不同类型
        node.setInvokeParameter("string-param");
        assertEquals("string-param", node.getInvokeParameter());

        node.setInvokeParameter(123);
        assertEquals(123, node.getInvokeParameter());

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 456);
        node.setInvokeParameter(map);
        assertEquals(map, node.getInvokeParameter());
    }

    @Test
    /**
     * testConnector方法。
     */
    public void testConnector() {
        Connector connector = new Connector();
        connector.setPre(new java.util.ArrayList<>());
        connector.setPost(new java.util.ArrayList<>());

        node.setConnector(connector);
        assertNotNull(node.getConnector());
    }

    @Test
    /**
     * testCache方法。
     */
    public void testCache() {
        HashMap<String, String> cache = new HashMap<>();
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        node.setCache(cache);
        assertNotNull(node.getCache());
        assertEquals("value1", node.getCache().get("key1"));
        assertEquals("value2", node.getCache().get("key2"));
    }

    @Test
    /**
     * testPutPost方法。
     */
    public void testPutPost() {
        WorkflowNode node1 = new WorkflowNode();
        node1.setNodeId("node-1");

        Connector connector = new Connector();
        connector.setPost(new java.util.ArrayList<>());
        node1.setConnector(connector);

        // 添加后置节点
        node1.putPost("node-2");
        assertTrue(node1.getConnector().getPost().contains("node-2"));

        // 重复添加不应该重复
        node1.putPost("node-2");
        assertEquals(1, node1.getConnector().getPost().size());

        // 不能添加自己作为后置节点
        node1.putPost("node-1");
        assertFalse(node1.getConnector().getPost().contains("node-1"));
    }

    @Test
    /**
     * testPutPre方法。
     */
    public void testPutPre() {
        WorkflowNode node1 = new WorkflowNode();
        node1.setNodeId("node-1");

        Connector connector = new Connector();
        connector.setPre(new java.util.ArrayList<>());
        node1.setConnector(connector);

        // 添加前置节点
        node1.putPre("node-0");
        assertTrue(node1.getConnector().getPre().contains("node-0"));

        // 重复添加不应该重复
        node1.putPre("node-0");
        assertEquals(1, node1.getConnector().getPre().size());

        // 不能添加自己作为前置节点
        node1.putPre("node-1");
        assertFalse(node1.getConnector().getPre().contains("node-1"));
    }

    @Test
    /**
     * testComplexWorkflowNode方法。
     */
    public void testComplexWorkflowNode() {
        // 创建一个复杂的工作流节点
        WorkflowNode node = new WorkflowNode();
        node.setNodeId("data-processing-001");
        node.setGroupId("etl-pipeline");
        node.setName("数据清洗与转换");
        node.setType("transform");
        node.setServiceUnit("java-etl-service");
        node.setInvokeDynamic("cleanAndTransform");

        // 设置参数
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("sourceTable", "raw_data");
        params.put("targetTable", "clean_data");
        params.put("batchSize", 1000);
        params.put("skipNulls", true);
        node.setInvokeParameter(params);

        // 设置连接器
        Connector connector = new Connector();
        connector.setPre(new java.util.ArrayList<>(java.util.Arrays.asList("data-extraction-001")));
        connector.setPost(new java.util.ArrayList<>(java.util.Arrays.asList("data-loading-001")));
        node.setConnector(connector);

        // 验证所有设置
        assertEquals("data-processing-001", node.getNodeId());
        assertEquals("etl-pipeline", node.getGroupId());
        assertEquals("数据清洗与转换", node.getName());
        assertEquals("transform", node.getType());
        assertEquals("java-etl-service", node.getServiceUnit());
        assertEquals("cleanAndTransform", node.getInvokeDynamic());
        assertNotNull(node.getInvokeParameter());
        assertNotNull(node.getConnector());
        assertEquals(1, node.getConnector().getPre().size());
        assertEquals(1, node.getConnector().getPost().size());
    }
}
