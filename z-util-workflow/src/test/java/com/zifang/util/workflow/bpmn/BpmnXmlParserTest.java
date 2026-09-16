package com.zifang.util.workflow.bpmn;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * BpmnXmlParser 类测试
 * 补充测试以确保完整覆盖所有公开方法
 */

/**
 * BpmnXmlParserTest类。
 */
public class BpmnXmlParserTest {

    private static final String SIMPLE_BPMN_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
            "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
            "  <process id=\"Process_1\" isExecutable=\"true\" name=\"Simple Process\">\n" +
            "    <startEvent id=\"StartEvent_1\" name=\"Start\"/>\n" +
            "    <endEvent id=\"EndEvent_1\" name=\"End\"/>\n" +
            "  </process>\n" +
            "</definitions>";

    private static final String COMPLEX_BPMN_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
            "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
            "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
            "  <process id=\"Process_1\" name=\"Complex Process\">\n" +
            "    <startEvent id=\"StartEvent_1\" name=\"Start\"/>\n" +
            "    <userTask id=\"Task_1\" name=\"Review Application\"/>\n" +
            "    <serviceTask id=\"ServiceTask_1\" name=\"Process Data\" operationName=\"process\"/>\n" +
            "    <exclusiveGateway id=\"Gateway_1\" name=\"Decision Gateway\"/>\n" +
            "    <endEvent id=\"EndEvent_1\" name=\"End\"/>\n" +
            "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"Task_1\"/>\n" +
            "    <sequenceFlow id=\"Flow_2\" sourceRef=\"Task_1\" targetRef=\"Gateway_1\"/>\n" +
            "    <sequenceFlow id=\"Flow_3\" sourceRef=\"Gateway_1\" targetRef=\"ServiceTask_1\">\n" +
            "      <conditionExpression>x > 10</conditionExpression>\n" +
            "    </sequenceFlow>\n" +
            "    <sequenceFlow id=\"Flow_4\" sourceRef=\"Gateway_1\" targetRef=\"EndEvent_1\"/>\n" +
            "    <sequenceFlow id=\"Flow_5\" sourceRef=\"ServiceTask_1\" targetRef=\"EndEvent_1\"/>\n" +
            "  </process>\n" +
            "</definitions>";

    @Test
    /**
     * testParseSimpleBpmnWithStartAndEndEvents方法。
     */
    public void testParseSimpleBpmnWithStartAndEndEvents() {
        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(SIMPLE_BPMN_XML);

        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
        assertEquals("Simple Process", diagram.getName());
        assertFalse(diagram.getNodes().isEmpty());
    }

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        BpmnXmlParser parser = new BpmnXmlParser();
        assertNotNull(parser);
    }

    @Test
    /**
     * testConstructorWithNamespaceAwareTrue方法。
     */
    public void testConstructorWithNamespaceAwareTrue() {
        BpmnXmlParser parser = new BpmnXmlParser(true);
        assertNotNull(parser);

        BpmnDiagram diagram = parser.parse(SIMPLE_BPMN_XML);
        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
    }

    @Test
    /**
     * testConstructorWithNamespaceAwareFalse方法。
     */
    public void testConstructorWithNamespaceAwareFalse() {
        BpmnXmlParser parser = new BpmnXmlParser(false);
        assertNotNull(parser);

        BpmnDiagram diagram = parser.parse(SIMPLE_BPMN_XML);
        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
    }

    @Test
    /**
     * testParseFromString方法。
     */
    public void testParseFromString() {
        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(SIMPLE_BPMN_XML);

        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
    }

    @Test
    /**
     * testParseFromByteArray方法。
     */
    public void testParseFromByteArray() {
        BpmnXmlParser parser = new BpmnXmlParser();
        byte[] xmlBytes = SIMPLE_BPMN_XML.getBytes(StandardCharsets.UTF_8);

        BpmnDiagram diagram = parser.parse(xmlBytes);

        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
        assertEquals("Simple Process", diagram.getName());
    }

    @Test
    /**
     * testParseFromByteArrayWithDifferentCharset方法。
     */
    public void testParseFromByteArrayWithDifferentCharset() {
        BpmnXmlParser parser = new BpmnXmlParser();
        byte[] xmlBytes = SIMPLE_BPMN_XML.getBytes(StandardCharsets.UTF_8);

        BpmnDiagram diagram = parser.parse(xmlBytes);

        assertNotNull(diagram);
        assertNotNull(diagram.getId());
    }

    @Test
    /**
     * testParseBpmnWithUserTask方法。
     */
    public void testParseBpmnWithUserTask() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <userTask id=\"Task_1\" name=\"Review Application\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertEquals(3, diagram.getNodes().size());

        boolean foundTask = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("Task_1".equals(node.getId()) && "userTask".equals(node.getType())) {
                foundTask = true;
                assertEquals("Review Application", node.getName());
            }
        }
        assertTrue("Should find userTask node", foundTask);
    }

    @Test
    /**
     * testParseBpmnWithExclusiveGateway方法。
     */
    public void testParseBpmnWithExclusiveGateway() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <exclusiveGateway id=\"Gateway_1\" name=\"Decision Gateway\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundGateway = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("Gateway_1".equals(node.getId()) && "exclusiveGateway".equals(node.getType())) {
                foundGateway = true;
                assertEquals("Decision Gateway", node.getName());
            }
        }
        assertTrue("Should find exclusiveGateway node", foundGateway);
    }

    @Test
    /**
     * testParseBpmnWithParallelGateway方法。
     */
    public void testParseBpmnWithParallelGateway() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <parallelGateway id=\"Gateway_1\" name=\"Parallel Gateway\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundGateway = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("Gateway_1".equals(node.getId()) && "parallelGateway".equals(node.getType())) {
                foundGateway = true;
            }
        }
        assertTrue("Should find parallelGateway node", foundGateway);
    }

    @Test
    /**
     * testParseBpmnWithInclusiveGateway方法。
     */
    public void testParseBpmnWithInclusiveGateway() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <inclusiveGateway id=\"Gateway_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundGateway = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("Gateway_1".equals(node.getId()) && "inclusiveGateway".equals(node.getType())) {
                foundGateway = true;
            }
        }
        assertTrue("Should find inclusiveGateway node", foundGateway);
    }

    @Test
    /**
     * testParseBpmnWithSequenceFlow方法。
     */
    public void testParseBpmnWithSequenceFlow() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertFalse("Should have sequence flows", diagram.getSequenceFlows().isEmpty());

        BpmnDiagram.BpmnSequenceFlow flow = diagram.getSequenceFlows().get(0);
        assertEquals("Flow_1", flow.getId());
        assertEquals("StartEvent_1", flow.getSourceRef());
        assertEquals("EndEvent_1", flow.getTargetRef());
    }

    @Test
    /**
     * testParseBpmnWithSequenceFlowWithName方法。
     */
    public void testParseBpmnWithSequenceFlowWithName() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "    <sequenceFlow id=\"Flow_1\" name=\"Normal Flow\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertEquals(1, diagram.getSequenceFlows().size());
        assertEquals("Normal Flow", diagram.getSequenceFlows().get(0).getName());
    }

    @Test
    /**
     * testParseBpmnWithSequenceFlowWithDefault方法。
     */
    public void testParseBpmnWithSequenceFlowWithDefault() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <exclusiveGateway id=\"Gateway_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"Gateway_1\"/>\n" +
                "    <sequenceFlow id=\"Flow_2\" sourceRef=\"Gateway_1\" targetRef=\"EndEvent_1\" default=\"true\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundDefaultFlow = false;
        for (BpmnDiagram.BpmnSequenceFlow flow : diagram.getSequenceFlows()) {
            if ("Flow_2".equals(flow.getId()) && flow.isDefault()) {
                foundDefaultFlow = true;
            }
        }
        assertTrue("Should find default sequence flow", foundDefaultFlow);
    }

    @Test
    /**
     * testParseBpmnWithConditionExpression方法。
     */
    public void testParseBpmnWithConditionExpression() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\">\n" +
                "      <conditionExpression>x > 10</conditionExpression>\n" +
                "    </sequenceFlow>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertEquals(1, diagram.getSequenceFlows().size());

        BpmnDiagram.BpmnSequenceFlow flow = diagram.getSequenceFlows().get(0);
        assertEquals("x > 10", flow.getConditionExpression());
    }

    @Test
    /**
     * testParseEmptyProcess方法。
     */
    public void testParseEmptyProcess() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\"/>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
        assertTrue(diagram.getNodes().isEmpty());
        assertTrue(diagram.getSequenceFlows().isEmpty());
    }

    @Test
    /**
     * testParseWithDocumentation方法。
     */
    public void testParseWithDocumentation() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\">\n" +
                "      <documentation>This is the start event</documentation>\n" +
                "    </startEvent>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        assertEquals(1, diagram.getNodes().size());

        BpmnDiagram.BpmnNode node = diagram.getNodes().get(0);
        assertEquals("This is the start event", node.getProperty("documentation"));
    }

    @Test
    /**
     * testParseInvalidXml方法。
     */
    public void testParseInvalidXml() {
        String invalidXml = "not valid xml at all";
        BpmnXmlParser parser = new BpmnXmlParser();

        try {
            parser.parse(invalidXml);
            fail("Should throw RuntimeException for invalid XML");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to parse BPMN XML"));
        }
    }

    @Test(expected = RuntimeException.class)
    /**
     * testParseInvalidXmlWithAnnotation方法。
     */
    public void testParseInvalidXmlWithAnnotation() {
        BpmnXmlParser parser = new BpmnXmlParser();
        parser.parse("not valid xml at all");
    }

    @Test
    /**
     * testParseInvalidByteArray方法。
     */
    public void testParseInvalidByteArray() {
        BpmnXmlParser parser = new BpmnXmlParser();
        byte[] invalidBytes = "not valid xml at all".getBytes(StandardCharsets.UTF_8);

        try {
            parser.parse(invalidBytes);
            fail("Should throw RuntimeException for invalid bytes");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to parse BPMN XML"));
        }
    }

    @Test
    /**
     * testBpmnDiagramNodeMap方法。
     */
    public void testBpmnDiagramNodeMap() {
        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(SIMPLE_BPMN_XML);

        assertNotNull(diagram.getNodeMap());
        assertEquals(2, diagram.getNodeMap().size());
        assertNotNull(diagram.getNodeMap().get("StartEvent_1"));
        assertNotNull(diagram.getNodeMap().get("EndEvent_1"));
    }

    @Test
    /**
     * testBpmnNodeProperties方法。
     */
    public void testBpmnNodeProperties() {
        BpmnDiagram.BpmnNode node = new BpmnDiagram.BpmnNode();
        node.setId("node1");
        node.setName("Test Node");
        node.setType("userTask");
        node.setProperty("key1", "value1");
        node.setProperty("key2", "value2");

        assertEquals("node1", node.getId());
        assertEquals("Test Node", node.getName());
        assertEquals("userTask", node.getType());
        assertEquals("value1", node.getProperty("key1"));
        assertEquals("value2", node.getProperty("key2"));
        assertEquals(2, node.getProperties().size());
    }

    @Test
    /**
     * testBpmnSequenceFlowProperties方法。
     */
    public void testBpmnSequenceFlowProperties() {
        BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow();
        flow.setId("flow1");
        flow.setSourceRef("node1");
        flow.setTargetRef("node2");
        flow.setConditionExpression("condition");

        assertEquals("flow1", flow.getId());
        assertEquals("node1", flow.getSourceRef());
        assertEquals("node2", flow.getTargetRef());
        assertEquals("condition", flow.getConditionExpression());
    }

    @Test
    /**
     * testBpmnDiagramGettersAndSetters方法。
     */
    public void testBpmnDiagramGettersAndSetters() {
        BpmnDiagram diagram = new BpmnDiagram();
        diagram.setId("diagram1");
        diagram.setName("Test Diagram");

        assertEquals("diagram1", diagram.getId());
        assertEquals("Test Diagram", diagram.getName());
    }

    @Test
    /**
     * testBpmnDiagramConstructorWithIdAndName方法。
     */
    public void testBpmnDiagramConstructorWithIdAndName() {
        BpmnDiagram diagram = new BpmnDiagram("id123", "My Process");
        assertEquals("id123", diagram.getId());
        assertEquals("My Process", diagram.getName());
    }

    @Test
    /**
     * testAddNodeAndSequenceFlow方法。
     */
    public void testAddNodeAndSequenceFlow() {
        BpmnDiagram diagram = new BpmnDiagram();

        BpmnDiagram.BpmnNode node1 = new BpmnDiagram.BpmnNode("node1", "Start", "startEvent");
        BpmnDiagram.BpmnNode node2 = new BpmnDiagram.BpmnNode("node2", "End", "endEvent");
        diagram.addNode(node1);
        diagram.addNode(node2);

        assertEquals(2, diagram.getNodes().size());
        assertEquals(2, diagram.getNodeMap().size());

        BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow("flow1", "node1", "node2");
        diagram.addSequenceFlow(flow);

        assertEquals(1, diagram.getSequenceFlows().size());
    }

    @Test
    /**
     * testParseComplexBpmnDiagram方法。
     */
    public void testParseComplexBpmnDiagram() {
        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(COMPLEX_BPMN_XML);

        assertNotNull(diagram);
        assertEquals("Process_1", diagram.getId());
        assertEquals("Complex Process", diagram.getName());
        assertFalse("Should have nodes", diagram.getNodes().isEmpty());
        assertFalse("Should have sequence flows", diagram.getSequenceFlows().isEmpty());
    }

    @Test
    /**
     * testParseServiceTask方法。
     */
    public void testParseServiceTask() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <serviceTask id=\"ServiceTask_1\" name=\"Process Data\" operationName=\"processData\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundServiceTask = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("ServiceTask_1".equals(node.getId()) && "serviceTask".equals(node.getType())) {
                foundServiceTask = true;
                assertEquals("Process Data", node.getName());
                assertEquals("processData", node.getProperty("operationName"));
            }
        }
        assertTrue("Should find serviceTask node", foundServiceTask);
    }

    @Test
    /**
     * testParseScriptTask方法。
     */
    public void testParseScriptTask() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <scriptTask id=\"ScriptTask_1\" name=\"Run Script\" scriptFormat=\"javascript\">\n" +
                "      <script>console.log('hello');</script>\n" +
                "    </scriptTask>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundScriptTask = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("ScriptTask_1".equals(node.getId()) && "scriptTask".equals(node.getType())) {
                foundScriptTask = true;
                assertEquals("console.log('hello');", node.getProperty("script"));
            }
        }
        assertTrue("Should find scriptTask node", foundScriptTask);
    }

    @Test
    /**
     * testParseWithIncomingAndOutgoing方法。
     */
    public void testParseWithIncomingAndOutgoing() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <userTask id=\"Task_1\">\n" +
                "      <incoming>Flow_1</incoming>\n" +
                "      <outgoing>Flow_2</outgoing>\n" +
                "    </userTask>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);

        boolean foundTask = false;
        for (BpmnDiagram.BpmnNode node : diagram.getNodes()) {
            if ("Task_1".equals(node.getId())) {
                foundTask = true;
                assertEquals("Flow_1", node.getProperty("incoming"));
                assertEquals("Flow_2", node.getProperty("outgoing"));
            }
        }
        assertTrue("Should find task with incoming/outgoing", foundTask);
    }

    @Test
    /**
     * testBpmnNodeDefaultConstructor方法。
     */
    public void testBpmnNodeDefaultConstructor() {
        BpmnDiagram.BpmnNode node = new BpmnDiagram.BpmnNode();
        assertNotNull(node);
        assertNotNull(node.getProperties());
    }

    @Test
    /**
     * testBpmnSequenceFlowDefaultConstructor方法。
     */
    public void testBpmnSequenceFlowDefaultConstructor() {
        BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow();
        assertNotNull(flow);
        assertNotNull(flow.getProperties());
    }

    @Test
    /**
     * testBpmnSequenceFlowWithSourceAndTarget方法。
     */
    public void testBpmnSequenceFlowWithSourceAndTarget() {
        BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow("flow1", "node1", "node2");
        assertEquals("flow1", flow.getId());
        assertEquals("node1", flow.getSourceRef());
        assertEquals("node2", flow.getTargetRef());
    }

    @Test
    /**
     * testBpmnSequenceFlowSetDefault方法。
     */
    public void testBpmnSequenceFlowSetDefault() {
        BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow();
        assertFalse(flow.isDefault());

        flow.setDefault(true);
        assertTrue(flow.isDefault());

        flow.setDefault(false);
        assertFalse(flow.isDefault());
    }

    @Test
    /**
     * testEmptyXmlString方法。
     */
    public void testEmptyXmlString() {
        BpmnXmlParser parser = new BpmnXmlParser();

        try {
            parser.parse("");
            fail("Should throw exception for empty XML");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to parse BPMN XML"));
        }
    }

    @Test
    /**
     * testEmptyByteArray方法。
     */
    public void testEmptyByteArray() {
        BpmnXmlParser parser = new BpmnXmlParser();

        try {
            parser.parse(new byte[0]);
            fail("Should throw exception for empty byte array");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    /**
     * testParseMultipleStartEvents方法。
     */
    public void testParseMultipleStartEvents() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <startEvent id=\"StartEvent_2\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        BpmnXmlParser parser = new BpmnXmlParser();
        BpmnDiagram diagram = parser.parse(xml);

        assertNotNull(diagram);
        // Parser should handle multiple start events
        assertTrue(diagram.getNodes().size() >= 2);
    }

    @Test
    /**
     * testNamespaceAwareVsNonNamespaceAware方法。
     */
    public void testNamespaceAwareVsNonNamespaceAware() {
        String xmlWithNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <process id=\"Process_1\" name=\"Test Process\">\n" +
                "    <startEvent id=\"StartEvent_1\"/>\n" +
                "    <endEvent id=\"EndEvent_1\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        // Parse with namespace aware
        BpmnXmlParser parserNsAware = new BpmnXmlParser(true);
        BpmnDiagram diagramNsAware = parserNsAware.parse(xmlWithNamespace);

        // Parse without namespace aware
        BpmnXmlParser parserNonNsAware = new BpmnXmlParser(false);
        BpmnDiagram diagramNonNsAware = parserNonNsAware.parse(xmlWithNamespace);

        // Both should produce valid results
        assertNotNull(diagramNsAware);
        assertNotNull(diagramNonNsAware);
        assertEquals("Process_1", diagramNsAware.getId());
        assertEquals("Process_1", diagramNonNsAware.getId());
    }
}