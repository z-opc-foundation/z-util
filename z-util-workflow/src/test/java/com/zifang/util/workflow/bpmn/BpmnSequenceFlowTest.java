package com.zifang.util.workflow.bpmn;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * BpmnSequenceFlow 类测试
 */

/**
 * BpmnSequenceFlowTest类。
 */
public class BpmnSequenceFlowTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        assertNotNull(flow);
        assertNull(flow.getId());
        assertNull(flow.getSourceRef());
        assertNull(flow.getTargetRef());
        assertNull(flow.getName());
        assertNull(flow.getConditionExpression());
        assertFalse(flow.isDefault());
        assertNotNull(flow.getProperties());
    }

    @Test
    /**
     * testThreeParamConstructor方法。
     */
    public void testThreeParamConstructor() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow("flow-001", "node-start", "node-end");

        assertEquals("flow-001", flow.getId());
        assertEquals("node-start", flow.getSourceRef());
        assertEquals("node-end", flow.getTargetRef());
    }

    @Test
    /**
     * testSixParamConstructor方法。
     */
    public void testSixParamConstructor() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow(
                "flow-001", "node-start", "node-end",
                "Approve Flow", "${approved == true}", true);

        assertEquals("flow-001", flow.getId());
        assertEquals("node-start", flow.getSourceRef());
        assertEquals("node-end", flow.getTargetRef());
        assertEquals("Approve Flow", flow.getName());
        assertEquals("${approved == true}", flow.getConditionExpression());
        assertTrue(flow.isDefault());
    }

    @Test
    /**
     * testId方法。
     */
    public void testId() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setId("flow-new");
        assertEquals("flow-new", flow.getId());
    }

    @Test
    /**
     * testSourceRef方法。
     */
    public void testSourceRef() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setSourceRef("source-node");
        assertEquals("source-node", flow.getSourceRef());
    }

    @Test
    /**
     * testTargetRef方法。
     */
    public void testTargetRef() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setTargetRef("target-node");
        assertEquals("target-node", flow.getTargetRef());
    }

    @Test
    /**
     * testName方法。
     */
    public void testName() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setName("Review Approval");
        assertEquals("Review Approval", flow.getName());
    }

    @Test
    /**
     * testConditionExpression方法。
     */
    public void testConditionExpression() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setConditionExpression("${amount > 1000}");
        assertEquals("${amount > 1000}", flow.getConditionExpression());
    }

    @Test
    /**
     * testIsDefault方法。
     */
    public void testIsDefault() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        assertFalse(flow.isDefault());

        flow.setDefault(true);
        assertTrue(flow.isDefault());

        flow.setDefault(false);
        assertFalse(flow.isDefault());
    }

    @Test
    /**
     * testProperties方法。
     */
    public void testProperties() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        Map<String, String> props = new HashMap<>();
        props.put("priority", "high");
        props.put("assignee", "admin");

        flow.setProperties(props);

        assertEquals(2, flow.getProperties().size());
        assertEquals("high", flow.getProperties().get("priority"));
    }

    @Test
    /**
     * testSetProperty方法。
     */
    public void testSetProperty() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        flow.setProperty("key1", "value1");
        flow.setProperty("key2", "value2");

        assertEquals("value1", flow.getProperty("key1"));
        assertEquals("value2", flow.getProperty("key2"));
        assertNull(flow.getProperty("nonexistent"));
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        BpmnSequenceFlow flow1 = new BpmnSequenceFlow("flow-1", "a", "b");
        BpmnSequenceFlow flow2 = new BpmnSequenceFlow("flow-1", "a", "b");

        assertEquals(flow1, flow2);
    }

    @Test
    /**
     * testEqualsWithDifferentIds方法。
     */
    public void testEqualsWithDifferentIds() {
        BpmnSequenceFlow flow1 = new BpmnSequenceFlow("flow-1", "a", "b");
        BpmnSequenceFlow flow2 = new BpmnSequenceFlow("flow-2", "a", "b");

        assertNotEquals(flow1, flow2);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        assertNotEquals(flow, null);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        BpmnSequenceFlow flow1 = new BpmnSequenceFlow("flow-1", "a", "b");
        BpmnSequenceFlow flow2 = new BpmnSequenceFlow("flow-1", "a", "b");

        assertEquals(flow1.hashCode(), flow2.hashCode());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow("flow-1", "a", "b");
        String str = flow.toString();

        assertNotNull(str);
        assertTrue(str.contains("flow-1"));
    }

    @Test
    /**
     * testEqualsWithDifferentClasses方法。
     */
    public void testEqualsWithDifferentClasses() {
        BpmnSequenceFlow flow = new BpmnSequenceFlow();
        assertNotEquals(flow, "not a sequence flow");
    }
}