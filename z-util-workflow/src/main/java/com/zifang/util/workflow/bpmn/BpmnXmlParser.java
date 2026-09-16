package com.zifang.util.workflow.bpmn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * BPMN 2.0 XML格式解析器。
 * <p>
 * 使用标准Java DOM API将BPMN XML解析为BpmnDiagram模型。
 * <p>
 * 支持的BPMN元素：
 * <ul>
 *   <li>流程：process, collaboration, subProcess, transaction</li>
 *   <li>事件：startEvent, endEvent, intermediateThrowEvent, intermediateCatchEvent</li>
 *   <li>任务：userTask, serviceTask, scriptTask, manualTask, sendTask, receiveTask, task</li>
 *   <li>网关：exclusiveGateway, parallelGateway, inclusiveGateway, eventBasedGateway</li>
 *   <li>连接：sequenceFlow (含conditionExpression, default, name)</li>
 *   <li>结构：callActivity, subProcess</li>
 *   <li>多实例：multiInstanceLoopCharacteristics, standardLoopCharacteristics</li>
 *   <li>定时器：timerEventDefinition</li>
 *   <li>文档：documentation (节点和流程上)</li>
 * </ul>
 *
 * @see BpmnDiagram
 * @see BpmnModelConverter
 */
public class BpmnXmlParser {

    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String BPMN_DI_NS = "http://www.omg.org/spec/BPMN/20100524/DI";
    private static final String BPMN_BIODI_NS = "http://www.omg.org/spec/BPMN/20100524/BPMNDI";

    private boolean namespaceAware = true;

    /**
     * BpmnXmlParser方法。
     */
    public BpmnXmlParser() {
    }

    /**
     * 使用指定的命名空间感知模式构造解析器。
     *
     * @param namespaceAware 是否启用命名空间感知模式。
     *                       启用后，解析器会使用命名空间感知方式查找元素，
     *                       适用于标准 BPMN 2.0 XML；禁用后使用宽松匹配，
     *                       可能适用于某些非标准格式的 XML。
     */
    public BpmnXmlParser(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Parse BPMN XML content into BpmnDiagram
     *
     * @param xmlContent BPMN 2.0 XML string
     * @return BpmnDiagram model
     */
    public BpmnDiagram parse(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            // Disable external entity expansion for security
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
            return parseDocument(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse BPMN XML", e);
        }
    }

    /**
     * Parse from a file path (convenience method)
     *
     * @param xmlBytes BPMN XML bytes
     * @return BpmnDiagram model
     */
    public BpmnDiagram parse(byte[] xmlBytes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlBytes));
            return parseDocument(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse BPMN XML from bytes", e);
        }
    }

    private BpmnDiagram parseDocument(Document document) {
        BpmnDiagram diagram = new BpmnDiagram();

        Element definitions = document.getDocumentElement();
        diagram.setId(definitions.getAttribute("id"));
        // definitions name is optional, process name is the primary name
        diagram.setName(getAttributeWithFallback(definitions, "name", "name"));

        // Support both single process and collaboration diagrams
        NodeList collaborationList = definitions.getElementsByTagNameNS(BPMN_NS, "collaboration");
        if (collaborationList.getLength() == 0) {
            collaborationList = definitions.getElementsByTagName("collaboration");
        }

        if (collaborationList.getLength() > 0) {
            // Collaboration diagram: parse participants and their processes
            parseCollaboration((Element) collaborationList.item(0), diagram);
        }

        // Find process element(s)
        NodeList processList = definitions.getElementsByTagNameNS(BPMN_NS, "process");
        if (processList.getLength() == 0) {
            processList = definitions.getElementsByTagName("process");
        }

        for (int i = 0; i < processList.getLength(); i++) {
            Element processElement = (Element) processList.item(i);
            // Use first process id as diagram id if not set
            if (diagram.getId() == null || diagram.getId().isEmpty()) {
                diagram.setId(processElement.getAttribute("id"));
            }
            if (diagram.getName() == null || diagram.getName().isEmpty()) {
                diagram.setName(processElement.getAttribute("name"));
            }
            parseProcessElements(processElement, diagram);
        }

        return diagram;
    }

    private void parseCollaboration(Element collaboration, BpmnDiagram diagram) {
        // Parse participant processes within a collaboration
        NodeList participantList = collaboration.getElementsByTagNameNS(BPMN_NS, "participant");
        if (participantList.getLength() == 0) {
            participantList = collaboration.getElementsByTagName("participant");
        }

        for (int i = 0; i < participantList.getLength(); i++) {
            Element participant = (Element) participantList.item(i);
            String processRef = participant.getAttribute("processRef");
            // Store participant name for reference
            String participantName = participant.getAttribute("name");
            if (processRef != null && !processRef.isEmpty()) {
                // The actual process will be parsed separately
                // Add participant as a special property
            }
        }

        // Parse message flows (inter-participant connections)
        NodeList messageFlowList = collaboration.getElementsByTagNameNS(BPMN_NS, "messageFlow");
        if (messageFlowList.getLength() == 0) {
            messageFlowList = collaboration.getElementsByTagName("messageFlow");
        }
        // Note: messageFlow is not currently supported in WorkflowConfiguration
        // but we parse it for completeness
    }

    private void parseProcessElements(Element processElement, BpmnDiagram diagram) {
        // Parse all supported node types
        parseNodes(processElement, "startEvent", "startEvent", diagram);
        parseNodes(processElement, "endEvent", "endEvent", diagram);
        parseNodes(processElement, "intermediateThrowEvent", "intermediateThrowEvent", diagram);
        parseNodes(processElement, "intermediateCatchEvent", "intermediateCatchEvent", diagram);

        // Tasks
        parseNodes(processElement, "userTask", "userTask", diagram);
        parseNodes(processElement, "serviceTask", "serviceTask", diagram);
        parseNodes(processElement, "scriptTask", "scriptTask", diagram);
        parseNodes(processElement, "manualTask", "manualTask", diagram);
        parseNodes(processElement, "sendTask", "sendTask", diagram);
        parseNodes(processElement, "receiveTask", "receiveTask", diagram);
        parseNodes(processElement, "task", "task", diagram);

        // Gateways
        parseNodes(processElement, "exclusiveGateway", "exclusiveGateway", diagram);
        parseNodes(processElement, "parallelGateway", "parallelGateway", diagram);
        parseNodes(processElement, "inclusiveGateway", "inclusiveGateway", diagram);
        parseNodes(processElement, "eventBasedGateway", "eventBasedGateway", diagram);

        // Sub-processes
        parseNodes(processElement, "subProcess", "subProcess", diagram);
        parseNodes(processElement, "transaction", "transaction", diagram);
        parseNodes(processElement, "callActivity", "callActivity", diagram);
        parseNodes(processElement, "adHocSubProcess", "adhocSubProcess", diagram);

        // Sequence flows
        parseSequenceFlows(processElement, diagram);
    }

    private void parseNodes(Element parentElement, String elementTag, String type, BpmnDiagram diagram) {
        NodeList nodeList = parentElement.getElementsByTagNameNS(BPMN_NS, elementTag);
        if (nodeList.getLength() == 0) {
            nodeList = parentElement.getElementsByTagName(elementTag);
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);

            // Skip elements that are nested inside other elements (e.g., boundary events)
            // Only process direct children of the process element
            if (!isDirectChild(parentElement, element)) {
                continue;
            }

            String id = element.getAttribute("id");
            String name = getAttributeWithFallback(element, "name", "name");

            BpmnDiagram.BpmnNode node = new BpmnDiagram.BpmnNode(id, name, type);

            // Parse documentation
            String docText = extractDocumentation(element);
            if (docText != null && !docText.isEmpty()) {
                node.setProperty("documentation", docText);
            }

            // Parse timer event definition
            parseTimerEventDefinition(element, node);

            // Parse multi-instance loop characteristics
            parseLoopCharacteristics(element, node);

            // Parse called element (callActivity)
            String calledElement = element.getAttribute("calledElement");
            if (calledElement != null && !calledElement.isEmpty()) {
                node.setProperty("calledElement", calledElement);
            }

            // Parse operation name (for service tasks)
            String operationName = element.getAttribute("operationName");
            if (operationName != null && !operationName.isEmpty()) {
                node.setProperty("operationName", operationName);
            }

            // Parse implementation (serviceTask implementation attribute)
            String implementation = element.getAttribute("implementation");
            if (implementation != null && !implementation.isEmpty()) {
                node.setProperty("implementation", implementation);
            }

            // Parse script (scriptTask)
            parseScriptContent(element, node);

            // Parse incoming/outgoing (flow node UI/analysis info)
            parseFlowNodeRefs(element, node);

            // Parse boundary events (attached to task as boundary)
            parseBoundaryEvents(element, node, parentElement, diagram);

            diagram.addNode(node);
        }
    }

    /**
     * Parse boundary events attached to an activity.
     * Boundary events are child elements of the activity, not siblings.
     */
    private void parseBoundaryEvents(Element activityElement, BpmnDiagram.BpmnNode parentNode,
                                     Element processElement, BpmnDiagram diagram) {
        NodeList boundaryEventList = activityElement.getElementsByTagNameNS(BPMN_NS, "boundaryEvent");
        if (boundaryEventList.getLength() == 0) {
            boundaryEventList = activityElement.getElementsByTagName("boundaryEvent");
        }

        for (int i = 0; i < boundaryEventList.getLength(); i++) {
            Element boundaryEvent = (Element) boundaryEventList.item(i);
            String id = boundaryEvent.getAttribute("id");
            String name = getAttributeWithFallback(boundaryEvent, "name", "name");

            BpmnDiagram.BpmnNode node = new BpmnDiagram.BpmnNode(id, name, "boundaryEvent");

            // attachedToRef points to the activity this boundary is attached to
            String attachedToRef = boundaryEvent.getAttribute("attachedToRef");
            if (attachedToRef == null || attachedToRef.isEmpty()) {
                attachedToRef = activityElement.getAttribute("id");
            }
            node.setProperty("attachedToRef", attachedToRef);

            String docText = extractDocumentation(boundaryEvent);
            if (docText != null && !docText.isEmpty()) {
                node.setProperty("documentation", docText);
            }

            parseTimerEventDefinition(boundaryEvent, node);

            // Cancel activity attribute (for error/terminate boundary events)
            String cancelActivity = boundaryEvent.getAttribute("cancelActivity");
            if ("false".equals(cancelActivity)) {
                node.setProperty("cancelActivity", "false");
            }

            diagram.addNode(node);
        }
    }

    private void parseTimerEventDefinition(Element eventElement, BpmnDiagram.BpmnNode node) {
        // Timer can be inside eventDefinitions
        NodeList eventDefList = eventElement.getElementsByTagNameNS(BPMN_NS, "eventDefinition");
        if (eventDefList.getLength() == 0) {
            eventDefList = eventElement.getElementsByTagName("eventDefinition");
        }

        for (int i = 0; i < eventDefList.getLength(); i++) {
            Element eventDef = (Element) eventDefList.item(i);
            // Check if this is a timer event definition
            if (eventDef.getNamespaceURI() != null
                    && eventDef.getNamespaceURI().contains("BPMN")
                    || hasLocalName(eventDef, "timerEventDefinition")) {

                // Parse timer details: timeDate, timeDuration, timeCycle
                NodeList timeDateList = eventDef.getElementsByTagNameNS(BPMN_NS, "timeDate");
                if (timeDateList.getLength() == 0) {
                    timeDateList = eventDef.getElementsByTagName("timeDate");
                }
                if (timeDateList.getLength() > 0) {
                    String timeDate = getExpressionText((Element) timeDateList.item(0));
                    node.setProperty("timerTimeDate", timeDate);
                }

                NodeList timeDurationList = eventDef.getElementsByTagNameNS(BPMN_NS, "timeDuration");
                if (timeDurationList.getLength() == 0) {
                    timeDurationList = eventDef.getElementsByTagName("timeDuration");
                }
                if (timeDurationList.getLength() > 0) {
                    String timeDuration = getExpressionText((Element) timeDurationList.item(0));
                    node.setProperty("timerTimeDuration", timeDuration);
                }

                NodeList timeCycleList = eventDef.getElementsByTagNameNS(BPMN_NS, "timeCycle");
                if (timeCycleList.getLength() == 0) {
                    timeCycleList = eventDef.getElementsByTagName("timeCycle");
                }
                if (timeCycleList.getLength() > 0) {
                    String timeCycle = getExpressionText((Element) timeCycleList.item(0));
                    node.setProperty("timerTimeCycle", timeCycle);
                }

                node.setProperty("timerType",
                        (node.getProperty("timerTimeDate") != null ? "timeDate" :
                                node.getProperty("timerTimeDuration") != null ? "timeDuration" :
                                        node.getProperty("timerTimeCycle") != null ? "timeCycle" : "unknown"));
                break;
            }
        }
    }

    private void parseLoopCharacteristics(Element element, BpmnDiagram.BpmnNode node) {
        // multiInstanceLoopCharacteristics
        NodeList multiInstList = element.getElementsByTagNameNS(BPMN_NS, "multiInstanceLoopCharacteristics");
        if (multiInstList.getLength() == 0) {
            multiInstList = element.getElementsByTagName("multiInstanceLoopCharacteristics");
        }
        if (multiInstList.getLength() > 0) {
            Element multiInst = (Element) multiInstList.item(0);
            String isSequential = multiInst.getAttribute("isSequential");
            String loopCardinality = getElementText(multiInst, "loopCardinality");
            String completionCondition = getElementText(multiInst, "completionCondition");

            node.setProperty("isSequentialLoop", isSequential);
            if (loopCardinality != null) {
                node.setProperty("loopCardinality", loopCardinality);
            }
            if (completionCondition != null) {
                node.setProperty("completionCondition", completionCondition);
            }

            // Check for sequential = true/false
            if ("true".equals(isSequential)) {
                node.setProperty("isSequentialLoop", "true");
            } else if ("false".equals(isSequential)) {
                node.setProperty("isParallelLoop", "true");
            }
        }

        // standardLoopCharacteristics
        NodeList loopCharList = element.getElementsByTagNameNS(BPMN_NS, "standardLoopCharacteristics");
        if (loopCharList.getLength() == 0) {
            loopCharList = element.getElementsByTagName("standardLoopCharacteristics");
        }
        if (loopCharList.getLength() > 0) {
            Element loopChar = (Element) loopCharList.item(0);
            String testBefore = loopChar.getAttribute("testBefore");
            node.setProperty("loopTestBefore", testBefore);

            NodeList loopMaximumList = loopChar.getElementsByTagNameNS(BPMN_NS, "loopMaximum");
            if (loopMaximumList.getLength() == 0) {
                loopMaximumList = loopChar.getElementsByTagName("loopMaximum");
            }
            if (loopMaximumList.getLength() > 0) {
                node.setProperty("loopMaximum", loopMaximumList.item(0).getTextContent());
            }
        }
    }

    private void parseScriptContent(Element element, BpmnDiagram.BpmnNode node) {
        NodeList scriptList = element.getElementsByTagNameNS(BPMN_NS, "script");
        if (scriptList.getLength() == 0) {
            scriptList = element.getElementsByTagName("script");
        }
        if (scriptList.getLength() > 0) {
            String script = scriptList.item(0).getTextContent();
            node.setProperty("script", script);

            String scriptFormat = element.getAttribute("scriptFormat");
            if (scriptFormat != null && !scriptFormat.isEmpty()) {
                node.setProperty("scriptFormat", scriptFormat);
            }
        }
    }

    private void parseFlowNodeRefs(Element element, BpmnDiagram.BpmnNode node) {
        NodeList incomingList = element.getElementsByTagNameNS(BPMN_NS, "incoming");
        if (incomingList.getLength() == 0) {
            incomingList = element.getElementsByTagName("incoming");
        }
        List<String> incoming = new ArrayList<>();
        for (int i = 0; i < incomingList.getLength(); i++) {
            String text = incomingList.item(i).getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                incoming.add(text.trim());
            }
        }
        if (!incoming.isEmpty()) {
            node.setProperty("incoming", String.join(",", incoming));
        }

        NodeList outgoingList = element.getElementsByTagNameNS(BPMN_NS, "outgoing");
        if (outgoingList.getLength() == 0) {
            outgoingList = element.getElementsByTagName("outgoing");
        }
        List<String> outgoing = new ArrayList<>();
        for (int i = 0; i < outgoingList.getLength(); i++) {
            String text = outgoingList.item(i).getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                outgoing.add(text.trim());
            }
        }
        if (!outgoing.isEmpty()) {
            node.setProperty("outgoing", String.join(",", outgoing));
        }
    }

    private void parseSequenceFlows(Element parentElement, BpmnDiagram diagram) {
        NodeList flowList = parentElement.getElementsByTagNameNS(BPMN_NS, "sequenceFlow");
        if (flowList.getLength() == 0) {
            flowList = parentElement.getElementsByTagName("sequenceFlow");
        }

        for (int i = 0; i < flowList.getLength(); i++) {
            Element element = (Element) flowList.item(i);

            // Only process direct children
            if (!isDirectChild(parentElement, element)) {
                continue;
            }

            String id = element.getAttribute("id");
            String sourceRef = element.getAttribute("sourceRef");
            String targetRef = element.getAttribute("targetRef");
            String name = getAttributeWithFallback(element, "name", "name");

            BpmnDiagram.BpmnSequenceFlow flow = new BpmnDiagram.BpmnSequenceFlow(id, sourceRef, targetRef);
            flow.setName(name);

            // Parse default flow (for exclusive gateways)
            String defaultAttr = element.getAttribute("default");
            if (defaultAttr != null && !defaultAttr.isEmpty()) {
                flow.setDefault(true);
            }

            // Parse condition expression
            NodeList conditionList = element.getElementsByTagNameNS(BPMN_NS, "conditionExpression");
            if (conditionList.getLength() == 0) {
                conditionList = element.getElementsByTagName("conditionExpression");
            }
            if (conditionList.getLength() > 0) {
                Element conditionEl = (Element) conditionList.item(0);
                String condition = conditionEl.getTextContent();
                // BPMN 2.0 conditionExpression may have a type attribute
                String type = conditionEl.getAttribute("xsi:type");
                String expression = conditionEl.getAttribute("expression");
                // Use text content as the expression, with possible type info
                if (condition != null && !condition.isEmpty()) {
                    flow.setConditionExpression(condition.trim());
                }
                if (type != null && !type.isEmpty()) {
                    flow.setProperty("conditionType", type);
                }
            }

            // Parse documentation on flow
            NodeList docList = element.getElementsByTagNameNS(BPMN_NS, "documentation");
            if (docList.getLength() == 0) {
                docList = element.getElementsByTagName("documentation");
            }
            if (docList.getLength() > 0) {
                String docText = docList.item(0).getTextContent();
                if (docText != null && !docText.trim().isEmpty()) {
                    flow.setProperty("documentation", docText.trim());
                }
            }

            diagram.addSequenceFlow(flow);
        }
    }

    // -------- Helper Methods --------

    private boolean isDirectChild(Element parent, Element child) {
        Node parentNode = parent;
        Node childParent = child.getParentNode();
        if (childParent == null) {
            return false;
        }
        // Direct child means parent node is the same DOM node
        // (not a descendant)
        if (childParent.equals(parentNode)) {
            return true;
        }
        // Also check if the childParent is an Element and equals parent
        if (childParent instanceof Element && ((Element) childParent).getTagName().equals(parent.getTagName())) {
            return parentNode.equals(childParent);
        }
        return parentNode.equals(childParent);
    }

    private String extractDocumentation(Element element) {
        NodeList docList = element.getElementsByTagNameNS(BPMN_NS, "documentation");
        if (docList.getLength() == 0) {
            docList = element.getElementsByTagName("documentation");
        }
        if (docList.getLength() > 0) {
            return docList.item(0).getTextContent();
        }
        return null;
    }

    private String getAttributeWithFallback(Element element, String attr1, String attr2) {
        String value = element.getAttribute(attr1);
        if (value == null || value.isEmpty()) {
            value = element.getAttribute(attr2);
        }
        return value;
    }

    private String getExpressionText(Element element) {
        if (element == null) {
            return null;
        }
        String body = element.getTextContent();
        if (body != null) {
            body = body.trim();
        }
        return body;
    }

    private String getElementText(Element parent, String childTag) {
        NodeList list = parent.getElementsByTagNameNS(BPMN_NS, childTag);
        if (list.getLength() == 0) {
            list = parent.getElementsByTagName(childTag);
        }
        if (list.getLength() > 0) {
            String text = list.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }

    private boolean hasLocalName(Element element, String localName) {
        String ln = element.getLocalName();
        return ln != null && ln.equals(localName);
    }
}
