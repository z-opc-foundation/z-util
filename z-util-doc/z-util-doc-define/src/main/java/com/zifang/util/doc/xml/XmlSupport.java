package com.zifang.util.doc.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OOXML/ODF 共用的 DOM 取数工具：按 local-name 遍历，与命名空间前缀无关。
 * 禁 DTD 与外部实体，防 XXE。
 */
public final class XmlSupport {

    private XmlSupport() {
    }

    public static Document parse(byte[] xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("XML 解析失败: " + e.getMessage(), e);
        }
    }

    /** 直接子节点里 local-name 匹配的全部元素。 */
    public static List<Element> children(Node parent, String localName) {
        List<Element> result = new ArrayList<>();
        if (parent == null) {
            return result;
        }
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && localName.equals(localName(element))) {
                result.add(element);
            }
        }
        return result;
    }

    /** 直接子节点里第一个 local-name 匹配的元素。 */
    public static Element child(Node parent, String localName) {
        List<Element> found = children(parent, localName);
        return found.isEmpty() ? null : found.get(0);
    }

    /** 任意层级后代里 local-name 匹配的元素。 */
    public static List<Element> descendants(Element root, String localName) {
        List<Element> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        NodeList nodes = root.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && localName.equals(localName(element))) {
                result.add(element);
            }
        }
        return result;
    }

    public static String localName(Element element) {
        String local = element.getLocalName();
        return local != null ? local : element.getTagName();
    }

    /** 元素的全部文本内容（含子元素文本拼接）。 */
    public static String text(Node node) {
        if (node == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(child.getNodeValue());
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                sb.append(text(child));
            }
        }
        return sb.toString();
    }

    /** 直接子元素里指定 local-name 的文本列表。 */
    public static List<String> childTexts(Node parent, String localName) {
        List<String> result = new ArrayList<>();
        for (Element element : children(parent, localName)) {
            result.add(text(element));
        }
        return result;
    }

    public static String attr(Element element, String name) {
        if (element == null) {
            return null;
        }
        String value = element.getAttribute(name);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // 前缀形式（如 r:id）再试一次
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if (localNameOf(attr) != null && localNameOf(attr).equals(name)) {
                return attr.getNodeValue();
            }
        }
        return null;
    }

    /**
     * 按命名空间 + 局部名精确取属性。
     * OOXML 里同一个元素常同时有 id="256"（普通属性）与 r:id="rId2"（关系属性），必须区分。
     */
    public static String attrNs(Element element, String namespaceUri, String localName) {
        if (element == null) {
            return null;
        }
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if (namespaceUri.equals(attr.getNamespaceURI()) && localName.equals(attr.getLocalName())) {
                return attr.getNodeValue();
            }
        }
        return null;
    }

    private static String localNameOf(Node attr) {
        String local = attr.getLocalName();
        return local == null ? attr.getNodeName() : local;
    }
}
