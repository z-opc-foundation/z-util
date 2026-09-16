package com.zifang.util.core.parser.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * W3C DOM 方式 XML 解析器
 * <p>
 * DOM (Document Object Model) 解析方式是将整个XML文档加载到内存中，
 * 构建为树形结构进行操作。这种方式适合：
 * <ul>
 *   <li>XML文档较小，需要随机访问</li>
 *   <li>需要多次访问XML内容</li>
 *   <li>需要修改XML结构</li>
 * </ul>
 * </p>
 * <p>
 * 该解析器使用标准的W3C DOM API实现，提供XML文档结构的打印功能，
 * 可用于调试和查看XML解析结果。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * InputStream is = new FileInputStream("test.xml");
 * XmlParserByDOM.parse(is);
 * </pre>
 * </p>
 *
 * @author zifang
 * @see XmlParserBySAX
 * @see <a href="https://docs.oracle.com/javase/tutorial/jaxp/dom/index.html">Java DOM Tutorial</a>
 */
public class XmlParserByDOM {

    /**
     * 解析 XML 文件并打印文档结构到标准输出
     * <p>
     * 该方法将XML文档完全加载到内存，构建DOM树，
     * 然后递归打印所有元素、属性和文本内容。
     * </p>
     * <p>
     * 输出格式示例：
     * <pre>
     * &lt;root&gt;
     *   @attr1=value1
     *   @attr2=value2
     *   &lt;child&gt;
     *     TEXT: some text
     *   &lt;/child&gt;
     * &lt;/root&gt;
     * </pre>
     * </p>
     *
     * @param xmlStream XML输入流，不能为null
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     * @throws SAXException                 if parse error occurs
     * @throws IOException                  if an I/O error occurs
     * @throws NullPointerException         if xmlStream is null
     */
    public static void parse(InputStream xmlStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);
        document.getDocumentElement().normalize();
        printElement(document.getDocumentElement(), 0);
    }

    /**
     * 递归打印元素及其内容
     *
     * @param element 要打印的元素
     * @param indent  缩进级别
     */
    private static void printElement(Element element, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        String prefix = sb.toString();
        System.out.println(prefix + "<" + element.getTagName() + ">");

        // 打印属性
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            System.out.println(prefix + "  @" + attr.getNodeName() + "=" + attr.getNodeValue());
        }

        // 打印子节点
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                printElement((Element) child, indent + 1);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getNodeValue().trim();
                if (!text.isEmpty()) {
                    System.out.println(prefix + "  TEXT: " + text);
                }
            } else if (child.getNodeType() == Node.COMMENT_NODE) {
                System.out.println(prefix + "  <!-- -->");
            }
        }
    }
}
