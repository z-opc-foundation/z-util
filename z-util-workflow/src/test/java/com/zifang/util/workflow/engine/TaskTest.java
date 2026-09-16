package com.zifang.util.workflow.engine;

import com.zifang.util.workflow.config.Connector;
import com.zifang.util.workflow.config.ExecutableWorkflowNode;
import com.zifang.util.workflow.config.WorkflowNode;
import com.zifang.util.workflow.conponents.Task;
import com.zifang.util.workflow.conponents.WorkFlowApplicationContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Task 类测试
 * 测试工作流任务执行单元的各种功能
 */

/**
 * TaskTest类。
 */
public class TaskTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        Task task = new Task();
        assertNotNull(task);
        assertNull(task.getWorkFlowApplicationContext());
        assertNull(task.getStart());
        assertNull(task.getExecutableWorkNodes());
        assertNull(task.getExecutableWorkNodeIdMap());
    }

    @Test
    /**
     * testGetterSetterWorkFlowApplicationContext方法。
     */
    public void testGetterSetterWorkFlowApplicationContext() {
        Task task = new Task();
        WorkFlowApplicationContext context = new WorkFlowApplicationContext();

        task.setWorkFlowApplicationContext(context);

        assertNotNull(task.getWorkFlowApplicationContext());
        assertSame(context, task.getWorkFlowApplicationContext());
    }

    @Test
    /**
     * testGetterSetterStart方法。
     */
    public void testGetterSetterStart() {
        Task task = new Task();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();

        task.setStart(startNode);

        assertNotNull(task.getStart());
        assertSame(startNode, task.getStart());
    }

    @Test
    /**
     * testGetterSetterExecutableWorkNodes方法。
     */
    public void testGetterSetterExecutableWorkNodes() {
        Task task = new Task();
        List<ExecutableWorkflowNode> nodes = new ArrayList<>();
        ExecutableWorkflowNode node1 = new ExecutableWorkflowNode();
        ExecutableWorkflowNode node2 = new ExecutableWorkflowNode();
        nodes.add(node1);
        nodes.add(node2);

        task.setExecutableWorkNodes(nodes);

        assertNotNull(task.getExecutableWorkNodes());
        assertEquals(2, task.getExecutableWorkNodes().size());
        assertSame(node1, task.getExecutableWorkNodes().get(0));
        assertSame(node2, task.getExecutableWorkNodes().get(1));
    }

    @Test
    /**
     * testGetterSetterExecutableWorkNodeIdMap方法。
     */
    public void testGetterSetterExecutableWorkNodeIdMap() {
        Task task = new Task();
        Map<String, ExecutableWorkflowNode> nodeMap = new HashMap<>();
        ExecutableWorkflowNode node1 = new ExecutableWorkflowNode();
        nodeMap.put("node1", node1);

        task.setExecutableWorkNodeIdMap(nodeMap);

        assertNotNull(task.getExecutableWorkNodeIdMap());
        assertEquals(1, task.getExecutableWorkNodeIdMap().size());
        assertSame(node1, task.getExecutableWorkNodeIdMap().get("node1"));
    }

    @Test
    /**
     * testEmptyExecutableWorkNodesList方法。
     */
    public void testEmptyExecutableWorkNodesList() {
        Task task = new Task();
        task.setExecutableWorkNodes(new ArrayList<>());

        assertNotNull(task.getExecutableWorkNodes());
        assertTrue(task.getExecutableWorkNodes().isEmpty());
    }

    @Test
    /**
     * testEmptyExecutableWorkNodeIdMap方法。
     */
    public void testEmptyExecutableWorkNodeIdMap() {
        Task task = new Task();
        task.setExecutableWorkNodeIdMap(new HashMap<>());

        assertNotNull(task.getExecutableWorkNodeIdMap());
        assertTrue(task.getExecutableWorkNodeIdMap().isEmpty());
    }

    @Test
    /**
     * testExecWithNullStart方法。
     */
    public void testExecWithNullStart() {
        Task task = new Task();
        // When start is null, exec() throws NullPointerException
        try {
            task.exec();
            fail("Should throw NullPointerException when start is null");
        } catch (NullPointerException e) {
            assertTrue("Expected NPE when start is null", true);
        }
        assertNull(task.getStart());
    }

    @Test
    /**
     * testExecWithValidStartNode方法。
     */
    public void testExecWithValidStartNode() {
        Task task = new Task();

        // Create a simple WorkflowNode
        WorkflowNode workflowNode = new WorkflowNode();
        workflowNode.setNodeId("start");
        workflowNode.setType("startEvent");
        workflowNode.setConnector(new Connector(new ArrayList<>(), new ArrayList<>()));

        // Create ExecutableWorkflowNode from WorkflowNode
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode(workflowNode);
        task.setStart(startNode);

        // exec() should complete without throwing
        task.exec();
        assertNotNull(task.getStart());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        Task task = new Task();
        String str = task.toString();

        assertNotNull(str);
        assertTrue(str.contains("Task"));
        assertTrue(str.contains("workFlowApplicationContext"));
        assertTrue(str.contains("start"));
        assertTrue(str.contains("executableWorkNodes"));
        assertTrue(str.contains("executableWorkNodeIdMap"));
    }

    @Test
    /**
     * testToStringWithAllFieldsSet方法。
     */
    public void testToStringWithAllFieldsSet() {
        Task task = new Task();
        WorkFlowApplicationContext context = new WorkFlowApplicationContext();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();
        List<ExecutableWorkflowNode> nodes = new ArrayList<>();
        Map<String, ExecutableWorkflowNode> nodeMap = new HashMap<>();

        task.setWorkFlowApplicationContext(context);
        task.setStart(startNode);
        task.setExecutableWorkNodes(nodes);
        task.setExecutableWorkNodeIdMap(nodeMap);

        String str = task.toString();

        assertNotNull(str);
        assertTrue(str.contains("Task"));
    }

    @Test
    /**
     * testEqualsSameInstance方法。
     */
    public void testEqualsSameInstance() {
        Task task = new Task();
        assertTrue(task.equals(task));
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        Task task = new Task();
        assertFalse(task.equals(null));
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        Task task = new Task();
        assertFalse(task.equals("not a task"));
    }

    @Test
    /**
     * testEqualsWithSameFields方法。
     */
    public void testEqualsWithSameFields() {
        Task task1 = new Task();
        Task task2 = new Task();

        WorkFlowApplicationContext context = new WorkFlowApplicationContext();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();
        List<ExecutableWorkflowNode> nodes = new ArrayList<>();
        Map<String, ExecutableWorkflowNode> nodeMap = new HashMap<>();

        task1.setWorkFlowApplicationContext(context);
        task1.setStart(startNode);
        task1.setExecutableWorkNodes(nodes);
        task1.setExecutableWorkNodeIdMap(nodeMap);

        task2.setWorkFlowApplicationContext(context);
        task2.setStart(startNode);
        task2.setExecutableWorkNodes(nodes);
        task2.setExecutableWorkNodeIdMap(nodeMap);

        assertTrue(task1.equals(task2));
    }

    @Test
    /**
     * testEqualsWithDifferentContext方法。
     */
    public void testEqualsWithDifferentContext() {
        Task task1 = new Task();
        Task task2 = new Task();

        WorkFlowApplicationContext context1 = new WorkFlowApplicationContext();
        WorkFlowApplicationContext context2 = new WorkFlowApplicationContext();

        task1.setWorkFlowApplicationContext(context1);
        task2.setWorkFlowApplicationContext(context2);

        // Different contexts should not be equal (unless same reference)
        assertFalse(task1.equals(task2));
    }

    @Test
    /**
     * testEqualsWithDifferentStart方法。
     */
    public void testEqualsWithDifferentStart() {
        Task task1 = new Task();
        Task task2 = new Task();

        // Create WorkflowNodes with different properties to ensure resulting ExecutableWorkflowNodes are different
        WorkflowNode workflowNode1 = new WorkflowNode();
        workflowNode1.setNodeId("node1");
        workflowNode1.setType("type1");

        WorkflowNode workflowNode2 = new WorkflowNode();
        workflowNode2.setNodeId("node2");
        workflowNode2.setType("type2");

        ExecutableWorkflowNode start1 = new ExecutableWorkflowNode(workflowNode1);
        ExecutableWorkflowNode start2 = new ExecutableWorkflowNode(workflowNode2);

        task1.setStart(start1);
        task2.setStart(start2);

        // Different ExecutableWorkflowNode instances should not be equal
        assertFalse(task1.equals(task2));
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        Task task = new Task();
        WorkFlowApplicationContext context = new WorkFlowApplicationContext();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();

        task.setWorkFlowApplicationContext(context);
        task.setStart(startNode);

        int hash1 = task.hashCode();
        int hash2 = task.hashCode();

        assertEquals("hashCode should be consistent", hash1, hash2);
    }

    @Test
    /**
     * testHashCodeEqualsForEqualObjects方法。
     */
    public void testHashCodeEqualsForEqualObjects() {
        Task task1 = new Task();
        Task task2 = new Task();

        WorkFlowApplicationContext context = new WorkFlowApplicationContext();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();
        List<ExecutableWorkflowNode> nodes = new ArrayList<>();
        Map<String, ExecutableWorkflowNode> nodeMap = new HashMap<>();

        task1.setWorkFlowApplicationContext(context);
        task1.setStart(startNode);
        task1.setExecutableWorkNodes(nodes);
        task1.setExecutableWorkNodeIdMap(nodeMap);

        task2.setWorkFlowApplicationContext(context);
        task2.setStart(startNode);
        task2.setExecutableWorkNodes(nodes);
        task2.setExecutableWorkNodeIdMap(nodeMap);

        assertEquals("Equal objects should have equal hashCodes", task1.hashCode(), task2.hashCode());
    }

    @Test
    /**
     * testAllFieldsAccessible方法。
     */
    public void testAllFieldsAccessible() {
        Task task = new Task();

        // Test all getters return what was set
        WorkFlowApplicationContext context = new WorkFlowApplicationContext();
        ExecutableWorkflowNode startNode = new ExecutableWorkflowNode();
        List<ExecutableWorkflowNode> nodes = new ArrayList<>();
        Map<String, ExecutableWorkflowNode> nodeMap = new HashMap<>();

        task.setWorkFlowApplicationContext(context);
        task.setStart(startNode);
        task.setExecutableWorkNodes(nodes);
        task.setExecutableWorkNodeIdMap(nodeMap);

        assertSame(context, task.getWorkFlowApplicationContext());
        assertSame(startNode, task.getStart());
        assertSame(nodes, task.getExecutableWorkNodes());
        assertSame(nodeMap, task.getExecutableWorkNodeIdMap());
    }
}