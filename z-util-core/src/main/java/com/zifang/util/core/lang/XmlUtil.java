package com.zifang.util.core.lang;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * XML 处理工具类。
 * <p>
 * 提供 XML 字符串与 DOM Document 之间转换、Map 与 XML 互转、
 * JavaBean 与 XML 互转（基于 JAXB，JDK 11+ 需自行引入 javax.xml.bind:jaxb-api）等功能。
 *
 * @author zifang
 * @see Document
 */
public class XmlUtil {

    public static final String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";

    private static final String DEFAULT_DOCUMENT_BUILDER_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

    private static boolean namespaceAware = true;

    synchronized public static void setNamespaceAware(boolean isNamespaceAware) {
        namespaceAware = isNamespaceAware;
    }

    /**
     * xmlToMap方法。
     * * @param xmlStr String类型参数
     *
     * @return static Map<String, Object>类型返回值
     */
    public static Map<String, Object> xmlToMap(String xmlStr) {
        return xmlToMap(xmlStr, new HashMap<>());
    }

    /**
     * mapToXml方法。
     * * @param data Map?,类型参数
     *
     * @param rootName String类型参数
     * @return static Document类型返回值
     */
    public static Document mapToXml(Map<?, ?> data, String rootName) {
        return mapToXml(data, rootName, null);
    }

    /**
     * mapToXml方法。
     * * @param data Map?,类型参数
     *
     * @param rootName  String类型参数
     * @param namespace String类型参数
     * @return static Document类型返回值
     */
    public static Document mapToXml(Map<?, ?> data, String rootName, String namespace) {
        final Document doc = createXml();
        final Element root = appendChild(doc, rootName, namespace);

        appendMap(doc, root, data);
        return doc;
    }

    /**
     * appendChild方法。
     * * @param node Node类型参数
     *
     * @param tagName   String类型参数
     * @param namespace String类型参数
     * @return static Element类型返回值
     */
    public static Element appendChild(Node node, String tagName, String namespace) {
        final Document doc = getOwnerDocument(node);
        final Element child =
                (null == namespace) ? doc.createElement(tagName) : doc.createElementNS(namespace, tagName);
        node.appendChild(child);
        return child;
    }

    /**
     * getOwnerDocument方法。
     * * @param node Node类型参数
     *
     * @return static Document类型返回值
     */
    public static Document getOwnerDocument(Node node) {
        return (node instanceof Document) ? (Document) node : node.getOwnerDocument();
    }

    private static void appendMap(Document doc, Node node, Map data) {
        data.forEach((key, value) -> {
            if (null != key) {
                final Element child = appendChild(node, key.toString());
                if (null != value) {
                    append(doc, child, value);
                }
            }
        });
    }

    private static void append(Document doc, Node node, Object data) {
        if (data instanceof Map) {
            // 如果值依旧为map，递归继续
            appendMap(doc, node, (Map<?, ?>) data);
        } else if (data instanceof Iterator) {
            // 如果值依旧为map，递归继续
            appendIterator(doc, node, (Iterator<?>) data);
        } else if (data instanceof Iterable) {
            // 如果值依旧为map，递归继续
            appendIterator(doc, node, ((Iterable<?>) data).iterator());
        } else {
            appendText(doc, node, data.toString());
        }
    }

    private static void appendIterator(Document doc, Node node, Iterator<?> data) {
        final Node parentNode = node.getParentNode();
        boolean isFirst = true;
        Object eleData;
        while (data.hasNext()) {
            eleData = data.next();
            if (isFirst) {
                append(doc, node, eleData);
                isFirst = false;
            } else {
                final Node cloneNode = node.cloneNode(false);
                parentNode.appendChild(cloneNode);
                append(doc, cloneNode, eleData);
            }
        }
    }

    private static Node appendText(Document doc, Node node, CharSequence text) {
        return node.appendChild(doc.createTextNode(StringUtil.str(text)));
    }

    /**
     * appendChild方法。
     * * @param node Node类型参数
     *
     * @param tagName String类型参数
     * @return static Element类型返回值
     */
    public static Element appendChild(Node node, String tagName) {
        return appendChild(node, tagName, null);
    }

    /**
     * xmlToMap方法。
     * * @param xmlStr String类型参数
     *
     * @param result MapString,类型参数
     * @return static Map<String, Object>类型返回值
     */
    public static Map<String, Object> xmlToMap(String xmlStr, Map<String, Object> result) {
        final Document doc = parseXml(xmlStr);
        final Element root = getRootElement(doc);
        root.normalize();

        return xmlToMap(root, result);
    }

    /**
     * xmlToMap方法。
     * * @param node Node类型参数
     *
     * @param result MapString,类型参数
     * @return static Map<String, Object>类型返回值
     */
    public static Map<String, Object> xmlToMap(Node node, Map<String, Object> result) {
        if (null == result) {
            result = new HashMap<>();
        }
        final NodeList nodeList = node.getChildNodes();
        final int length = nodeList.getLength();
        Node childNode;
        Element childEle;
        for (int i = 0; i < length; ++i) {
            childNode = nodeList.item(i);
            if (!isElement(childNode)) {
                continue;
            }

            childEle = (Element) childNode;
            final Object value = result.get(childEle.getNodeName());
            Object newValue;
            if (childEle.hasChildNodes()) {
                // 子节点继续递归遍历
                final Map<String, Object> map = xmlToMap(childEle);
                if (MapUtil.isNotEmpty(map)) {
                    newValue = map;
                } else {
                    newValue = childEle.getTextContent();
                }
            } else {
                newValue = childEle.getTextContent();
            }

            if (null != newValue) {
                if (null != value) {
                    if (value instanceof List) {
                        ((List<Object>) value).add(newValue);
                    } else {
                        result.put(childEle.getNodeName(), CollectionUtil.newArrayList(value, newValue));
                    }
                } else {
                    result.put(childEle.getNodeName(), newValue);
                }
            }
        }
        return result;
    }

    /**
     * xmlToMap方法。
     * * @param node Node类型参数
     *
     * @return static Map<String, Object>类型返回值
     */
    public static Map<String, Object> xmlToMap(Node node) {
        return xmlToMap(node, new HashMap<>());
    }

    /**
     * @author: zifang
     * @description: 将String类型的XML转换为XML文档
     * @time: 2022-04-12 10:36:00
     * @params: [xmlStr] in XML格式字符串
     * @return: org.w3c.dom.Document out 出参
     */
    public static Document parseXml(String xmlStr) {
        if (StringUtil.isBlank(xmlStr)) {
            throw new IllegalArgumentException("Xml content string is empty");
        }
        xmlStr = cleanInvalid(xmlStr);
        return readXml(StringUtil.getReader(xmlStr));
    }

    /**
     * readXml方法。
     * * @param reader Reader类型参数
     *
     * @return static Document类型返回值
     */
    public static Document readXml(Reader reader) {
        return readXml(new InputSource(reader));
    }

    /**
     * readXml方法。
     * * @param source InputSource类型参数
     *
     * @return static Document类型返回值
     */
    public static Document readXml(InputSource source) {
        final DocumentBuilder builder = createDocumentBuilder();
        try {
            return builder.parse(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * createDocumentBuilder方法。
     *
     * @return static DocumentBuilder类型返回值
     */
    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder builder;
        try {
            builder = createDocumentBuilderFactory().newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder;
    }

    /**
     * createDocumentBuilderFactory方法。
     *
     * @return static DocumentBuilderFactory类型返回值
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        final DocumentBuilderFactory factory;
        if (StringUtil.isNotEmpty(DEFAULT_DOCUMENT_BUILDER_FACTORY)) {
            factory = DocumentBuilderFactory.newInstance(DEFAULT_DOCUMENT_BUILDER_FACTORY, null);
        } else {
            factory = DocumentBuilderFactory.newInstance();
        }
        // 默认打开NamespaceAware，getElementsByTagNameNS可以使用命名空间
        factory.setNamespaceAware(namespaceAware);
        return disableXXE(factory);
    }

    /**
     * @author: zifang
     * @description: 关闭XXE，避免漏洞攻击
     * @time: 2022-04-12 10:48:01
     * @params: [dbf] in 入参
     * @return: javax.xml.parsers.DocumentBuilderFactory out 出参
     */
    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        String feature;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            // ignore
        }
        return dbf;
    }

    /**
     * @author: zifang
     * @description: 去除XML文本中的无效字符
     * @time: 2022-04-12 10:36:37
     * @params: [xmlContent] in XML文本
     * @return: java.lang.String out 出参
     */
    public static String cleanInvalid(String xmlContent) {
        if (null == xmlContent) {
            return null;
        }
        return xmlContent.replaceAll(INVALID_REGEX, "");
    }

    /**
     * getRootElement方法。
     * * @param doc Document类型参数
     *
     * @return static Element类型返回值
     */
    public static Element getRootElement(Document doc) {
        return (null == doc) ? null : doc.getDocumentElement();
    }

    /**
     * isElement方法。
     * * @param node Node类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isElement(Node node) {
        return (null != node) && Node.ELEMENT_NODE == node.getNodeType();
    }

    /**
     * createXml方法。
     *
     * @return static Document类型返回值
     */
    public static Document createXml() {
        return createDocumentBuilder().newDocument();
    }

    /**
     * beanToXml方法。
     * 将 JavaBean 序列化为格式化的 XML 字符串（基于 JAXB）。
     *
     * @param bean Object类型参数，待序列化对象，其类需可被 JAXB 识别（建议标注 @XmlRootElement）
     * @return static String类型返回值，格式化 XML 字符串；bean 为 null 时返回 null
     * @throws RuntimeException 序列化失败时抛出
     */
    public static String beanToXml(Object bean) {
        if (bean == null) {
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(bean.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(bean, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("bean to xml error", e);
        }
    }

    /**
     * xmlToBean方法。
     * 将 XML 字符串反序列化为指定类型的对象（基于 JAXB）。
     *
     * @param xmlStr String类型参数，XML 字符串，根元素需与目标类型匹配（建议目标类标注 @XmlRootElement）
     * @param clazz  Class类型参数，目标类型
     * @param <T>    目标类型
     * @return static T类型返回值，反序列化对象；xmlStr 或 clazz 为 null 时返回 null
     * @throws RuntimeException 反序列化失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> T xmlToBean(String xmlStr, Class<T> clazz) {
        if (xmlStr == null || clazz == null) {
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xmlStr));
        } catch (JAXBException e) {
            throw new RuntimeException("xml to bean error", e);
        }
    }
}
