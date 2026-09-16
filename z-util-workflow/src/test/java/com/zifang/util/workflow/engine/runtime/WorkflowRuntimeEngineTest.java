package com.zifang.util.workflow.engine.runtime;

import com.zifang.util.workflow.bpmn.BpmnDiagram;
import com.zifang.util.workflow.bpmn.BpmnModelConverter;
import com.zifang.util.workflow.bpmn.BpmnXmlParser;
import com.zifang.util.workflow.config.WorkflowConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * WorkflowRuntimeEngine 端到端集成测试。
 * 从 BPMN XML → 解析 → 转换 → 执行，验证完整流程。
 */

/**
 * WorkflowRuntimeEngineTest类。
 */
public class WorkflowRuntimeEngineTest {

    private BpmnXmlParser parser;
    private BpmnModelConverter converter;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        parser = new BpmnXmlParser();
        converter = new BpmnModelConverter();
    }

    private WorkflowConfiguration parseAndConvert(String xml) {
        BpmnDiagram diagram = parser.parse(xml);
        return converter.convert(diagram);
    }

    @Test
    /**
     * testInitializeAndStartLinearProcess方法。
     */
    public void testInitializeAndStartLinearProcess() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Linear_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Step1\" name=\"步骤1\"/>\n" +
                "    <serviceTask id=\"Step2\" name=\"步骤2\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Step1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Step1\" targetRef=\"Step2\"/>\n" +
                "    <sequenceFlow id=\"F3\" sourceRef=\"Step2\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        ExecutionResult result = runtimeEngine.start();

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getCompletedNodes().contains("Start"));
        assertTrue(result.getCompletedNodes().contains("Step1"));
        assertTrue(result.getCompletedNodes().contains("Step2"));
        assertTrue(result.getCompletedNodes().contains("End"));
    }

    @Test
    /**
     * testInitializeAndStartWithVariables方法。
     */
    public void testInitializeAndStartWithVariables() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Var_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Task1\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Task1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Task1\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        runtimeEngine.setVariable("amount", 5000);
        runtimeEngine.setVariable("userName", "张三");
        ExecutionResult result = runtimeEngine.start();

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(5000, runtimeEngine.getVariable("amount"));
        assertEquals("张三", runtimeEngine.getVariable("userName"));
    }

    @Test
    /**
     * testGetCurrentResult方法。
     */
    public void testGetCurrentResult() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Simple_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Task1\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Task1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Task1\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        runtimeEngine.start();

        ExecutionResult currentResult = runtimeEngine.getCurrentResult();
        assertNotNull(currentResult);
        assertEquals("COMPLETED", currentResult.getStatus());
    }

    @Test
    /**
     * testGetPendingUserTasks方法。
     */
    public void testGetPendingUserTasks() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"UserTask_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <userTask id=\"UserTask1\" name=\"用户任务\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"UserTask1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"UserTask1\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        ExecutionResult result = runtimeEngine.start();

        // userTask 会暂停执行
        assertEquals("PAUSED", result.getStatus());
        List<String> pendingTasks = runtimeEngine.getPendingUserTasks();
        assertNotNull(pendingTasks);
        assertEquals(1, pendingTasks.size());
        assertEquals("UserTask1", pendingTasks.get(0));
    }

    @Test
    /**
     * testVariablesArePreserved方法。
     */
    public void testVariablesArePreserved() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"VarPreserve_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Task1\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Task1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Task1\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        runtimeEngine.setVariable("testKey", "testValue");
        runtimeEngine.start();

        assertEquals("testValue", runtimeEngine.getVariable("testKey"));
        assertEquals(1, runtimeEngine.getVariables().size());
        assertTrue(runtimeEngine.getVariables().containsKey("testKey"));
    }

    @Test
    /**
     * testProcessIdIsGenerated方法。
     */
    public void testProcessIdIsGenerated() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Proc_ID\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        runtimeEngine.start();

        assertNotNull(runtimeEngine.getProcessId());
        assertTrue(runtimeEngine.getProcessId().startsWith("process-"));
    }

    @Test
    /**
     * testNodeMapIsBuilt方法。
     */
    public void testNodeMapIsBuilt() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"NodeMap_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Task1\"/>\n" +
                "    <serviceTask id=\"Task2\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Task1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Task1\" targetRef=\"Task2\"/>\n" +
                "    <sequenceFlow id=\"F3\" sourceRef=\"Task2\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);

        assertNotNull(runtimeEngine.getNodeMap());
        assertEquals(4, runtimeEngine.getNodeMap().size());
        assertTrue(runtimeEngine.getNodeMap().containsKey("Start"));
        assertTrue(runtimeEngine.getNodeMap().containsKey("Task1"));
        assertTrue(runtimeEngine.getNodeMap().containsKey("Task2"));
        assertTrue(runtimeEngine.getNodeMap().containsKey("End"));
    }

    @Test
    /**
     * testConfigurationIsStored方法。
     */
    public void testConfigurationIsStored() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Config_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);

        assertNotNull(runtimeEngine.getConfiguration());
        assertSame(config, runtimeEngine.getConfiguration());
    }

    @Test
    /**
     * testMultipleExecutions方法。
     */
    public void testMultipleExecutions() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Multi_Exec_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <serviceTask id=\"Task1\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"Task1\"/>\n" +
                "    <sequenceFlow id=\"F2\" sourceRef=\"Task1\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);

        // 多次执行应该都能正常工作
        for (int i = 0; i < 3; i++) {
            WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
            runtimeEngine.initialize(config);
            ExecutionResult result = runtimeEngine.start();

            assertEquals("COMPLETED", result.getStatus());
            assertEquals(3, result.getCompletedNodes().size());
        }
    }

    @Test
    /**
     * testEmptyWorkflow方法。
     */
    public void testEmptyWorkflow() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Empty_Proc\">\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);
        ExecutionResult result = runtimeEngine.start();

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(0, result.getCompletedNodes().size());
    }

    @Test
    /**
     * testErrorMessageOnResumeInvalidNode方法。
     */
    public void testErrorMessageOnResumeInvalidNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Invalid_Resume_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);
        WorkflowRuntimeEngine runtimeEngine = new WorkflowRuntimeEngine();
        runtimeEngine.initialize(config);

        // 尝试恢复一个不存在的节点
        ExecutionResult result = runtimeEngine.resume("NonExistentNode");

        assertEquals("FAILED", result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("NonExistentNode"));
    }

    @Test
    /**
     * testGatewayBranchingWithConditions方法。
     */
    public void testGatewayBranchingWithConditions() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <process id=\"Gateway_Branch_Proc\">\n" +
                "    <startEvent id=\"Start\"/>\n" +
                "    <exclusiveGateway id=\"GW1\"/>\n" +
                "    <serviceTask id=\"Task_A\" name=\"执行A\"/>\n" +
                "    <serviceTask id=\"Task_B\" name=\"执行B\"/>\n" +
                "    <endEvent id=\"End\"/>\n" +
                "    <sequenceFlow id=\"F1\" sourceRef=\"Start\" targetRef=\"GW1\"/>\n" +
                "    <sequenceFlow id=\"F_A\" sourceRef=\"GW1\" targetRef=\"Task_A\">\n" +
                "      <conditionExpression>${approved == true}</conditionExpression>\n" +
                "    </sequenceFlow>\n" +
                "    <sequenceFlow id=\"F_B\" sourceRef=\"GW1\" targetRef=\"Task_B\">\n" +
                "      <conditionExpression>${approved == false}</conditionExpression>\n" +
                "    </sequenceFlow>\n" +
                "    <sequenceFlow id=\"F_End\" sourceRef=\"Task_A\" targetRef=\"End\"/>\n" +
                "  </process>\n" +
                "</definitions>";

        WorkflowConfiguration config = parseAndConvert(xml);

        // Test approved = true branch
        WorkflowRuntimeEngine runtimeEngine1 = new WorkflowRuntimeEngine();
        runtimeEngine1.initialize(config);
        runtimeEngine1.setVariable("approved", true);
        ExecutionResult result1 = runtimeEngine1.start();

        assertTrue(result1.getCompletedNodes().contains("GW1"));
        assertTrue(result1.getCompletedNodes().contains("Task_A"));

        // Test approved = false branch
        WorkflowRuntimeEngine runtimeEngine2 = new WorkflowRuntimeEngine();
        runtimeEngine2.initialize(config);
        runtimeEngine2.setVariable("approved", false);
        ExecutionResult result2 = runtimeEngine2.start();

        assertTrue(result2.getCompletedNodes().contains("GW1"));
        assertTrue(result2.getCompletedNodes().contains("Task_B"));
    }
}
