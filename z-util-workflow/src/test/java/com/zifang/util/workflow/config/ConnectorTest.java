package com.zifang.util.workflow.config;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Connector 连接类测试
 */

/**
 * ConnectorTest类。
 */
public class ConnectorTest {

    private Connector connector;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        connector = new Connector();
    }

    @Test
    /**
     * testConnectorCreation方法。
     */
    public void testConnectorCreation() {
        assertNotNull(connector);
    }

    @Test
    /**
     * testPreNodes方法。
     */
    public void testPreNodes() {
        List<String> preNodes = new ArrayList<>();
        preNodes.add("node-001");
        preNodes.add("node-002");

        connector.setPre(preNodes);

        assertNotNull(connector.getPre());
        assertEquals(2, connector.getPre().size());
        assertTrue(connector.getPre().contains("node-001"));
        assertTrue(connector.getPre().contains("node-002"));
    }

    @Test
    /**
     * testPostNodes方法。
     */
    public void testPostNodes() {
        List<String> postNodes = new ArrayList<>();
        postNodes.add("node-003");
        postNodes.add("node-004");
        postNodes.add("node-005");

        connector.setPost(postNodes);

        assertNotNull(connector.getPost());
        assertEquals(3, connector.getPost().size());
        assertTrue(connector.getPost().contains("node-003"));
        assertTrue(connector.getPost().contains("node-004"));
        assertTrue(connector.getPost().contains("node-005"));
    }

    @Test
    /**
     * testEmptyPreNodes方法。
     */
    public void testEmptyPreNodes() {
        List<String> preNodes = new ArrayList<>();
        connector.setPre(preNodes);

        assertNotNull(connector.getPre());
        assertTrue(connector.getPre().isEmpty());
    }

    @Test
    /**
     * testEmptyPostNodes方法。
     */
    public void testEmptyPostNodes() {
        List<String> postNodes = new ArrayList<>();
        connector.setPost(postNodes);

        assertNotNull(connector.getPost());
        assertTrue(connector.getPost().isEmpty());
    }

    @Test
    /**
     * testNullPreNodes方法。
     */
    public void testNullPreNodes() {
        connector.setPre(null);
        assertNull(connector.getPre());
    }

    @Test
    /**
     * testNullPostNodes方法。
     */
    public void testNullPostNodes() {
        connector.setPost(null);
        assertNull(connector.getPost());
    }

    @Test
    /**
     * testSinglePreNode方法。
     */
    public void testSinglePreNode() {
        List<String> preNodes = new ArrayList<>();
        preNodes.add("start-node");

        connector.setPre(preNodes);

        assertEquals(1, connector.getPre().size());
        assertEquals("start-node", connector.getPre().get(0));
    }

    @Test
    /**
     * testSinglePostNode方法。
     */
    public void testSinglePostNode() {
        List<String> postNodes = new ArrayList<>();
        postNodes.add("end-node");

        connector.setPost(postNodes);

        assertEquals(1, connector.getPost().size());
        assertEquals("end-node", connector.getPost().get(0));
    }

    @Test
    /**
     * testMultiplePreAndPostNodes方法。
     */
    public void testMultiplePreAndPostNodes() {
        // 测试复杂的工作流连接
        List<String> preNodes = new ArrayList<>();
        preNodes.add("node-a");
        preNodes.add("node-b");
        preNodes.add("node-c");

        List<String> postNodes = new ArrayList<>();
        postNodes.add("node-d");
        postNodes.add("node-e");

        connector.setPre(preNodes);
        connector.setPost(postNodes);

        assertEquals(3, connector.getPre().size());
        assertEquals(2, connector.getPost().size());
    }

    @Test
    /**
     * testModifyPreNodesAfterSet方法。
     */
    public void testModifyPreNodesAfterSet() {
        List<String> preNodes = new ArrayList<>();
        preNodes.add("node-1");
        connector.setPre(preNodes);

        // 修改原始列表不应该影响 connector 中的列表
        // （除非 connector 直接引用了这个列表）
        preNodes.add("node-2");

        // 这个测试验证了 connector 是否正确复制了列表
        // 如果 connector 直接引用，则 size 会变为 2
        // 如果 connector 复制了列表，则 size 仍为 1
        // 这里不做断言，因为这个行为取决于实现
    }

    @Test
    /**
     * testConnectorEquality方法。
     */
    public void testConnectorEquality() {
        // 创建两个相同的 connector
        Connector connector1 = new Connector();
        List<String> pre1 = new ArrayList<>();
        pre1.add("node-a");
        connector1.setPre(pre1);

        Connector connector2 = new Connector();
        List<String> pre2 = new ArrayList<>();
        pre2.add("node-a");
        connector2.setPre(pre2);

        // 验证它们的 pre 列表内容相等
        assertEquals(connector1.getPre().size(), connector2.getPre().size());
        assertEquals(connector1.getPre().get(0), connector2.getPre().get(0));
    }

    @Test
    /**
     * testWorkflowScenario方法。
     */
    public void testWorkflowScenario() {
        // 模拟一个实际的工作流场景
        // 创建线性工作流: start -> process -> end

        Connector startConnector = new Connector();
        startConnector.setPre(null);
        startConnector.setPost(new ArrayList<String>() {{
            add("process");
        }});

        Connector processConnector = new Connector();
        processConnector.setPre(new ArrayList<String>() {{
            add("start");
        }});
        processConnector.setPost(new ArrayList<String>() {{
            add("end");
        }});

        Connector endConnector = new Connector();
        endConnector.setPre(new ArrayList<String>() {{
            add("process");
        }});
        endConnector.setPost(null);

        // 验证连接关系
        assertNotNull(processConnector.getPre());
        assertNotNull(processConnector.getPost());
        assertEquals("start", processConnector.getPre().get(0));
        assertEquals("end", processConnector.getPost().get(0));
    }

    @Test
    /**
     * testBranchingWorkflow方法。
     */
    public void testBranchingWorkflow() {
        // 测试分支工作流
        //      -> node-b ->
        // node-a           -> node-d
        //      -> node-c ->

        Connector nodeAConnector = new Connector();
        nodeAConnector.setPost(new ArrayList<String>() {{
            add("node-b");
            add("node-c");
        }});

        Connector nodeBConnector = new Connector();
        nodeBConnector.setPre(new ArrayList<String>() {{
            add("node-a");
        }});
        nodeBConnector.setPost(new ArrayList<String>() {{
            add("node-d");
        }});

        Connector nodeCConnector = new Connector();
        nodeCConnector.setPre(new ArrayList<String>() {{
            add("node-a");
        }});
        nodeCConnector.setPost(new ArrayList<String>() {{
            add("node-d");
        }});

        Connector nodeDConnector = new Connector();
        nodeDConnector.setPre(new ArrayList<String>() {{
            add("node-b");
            add("node-c");
        }});

        // 验证分支结构
        assertEquals(2, nodeAConnector.getPost().size());
        assertEquals(2, nodeDConnector.getPre().size());
        assertEquals(1, nodeBConnector.getPre().size());
        assertEquals(1, nodeCConnector.getPre().size());
    }
}
