package com.zifang.util.core.parser.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

/**
 * SAX 方式 XML 解析器
 * <p>
 * SAX (Simple API for XML) 是一种基于事件的流式解析方式。
 * 这种方式不需要将整个文档加载到内存，适合：
 * <ul>
 *   <li>大型XML文档的解析</li>
 *   <li>只需要顺序读取XML内容</li>
 *   <li>内存受限的环境</li>
 * </ul>
 * </p>
 * <p>
 * 该解析器使用SAX2 API实现，提供XML文档结构的打印功能，
 * 通过继承DefaultHandler并实现LexicalHandler接口来处理各种XML事件。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * InputStream is = new FileInputStream("test.xml");
 * XmlParserBySAX.parse(is);
 * </pre>
 * </p>
 *
 * @author zifang
 * @see XmlParserByDOM
 * @see <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/index.html">Java SAX Tutorial</a>
 */
public class XmlParserBySAX {

    /**
     * 使用 SAX 解析器解析 XML 并打印结构到标准输出
     * <p>
     * 该方法使用SAX解析器以流式方式解析XML，
     * 通过事件处理器打印开始标签、结束标签、属性和文本内容。
     * </p>
     * <p>
     * 输出格式示例：
     * <pre>
     * START: root
     *   @attr1 = value1
     *   @attr2 = value2
     *   TEXT: some text
     * END: root
     * </pre>
     * </p>
     *
     * @param xmlStream XML输入流，不能为null
     * @throws Exception            如果解析过程中发生任何异常
     * @throws NullPointerException if xmlStream is null
     */
    public static void parse(InputStream xmlStream) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(xmlStream, new MyHandler());
    }

    /**
     * SAX事件处理器内部类
     * <p>
     * 负责处理XML解析过程中的各种事件，包括：
     * <ul>
     *   <li>元素开始/结束</li>
     *   <li>文本内容</li>
     *   <li>DTD声明、实体引用、CDATA节、注释（通过LexicalHandler）</li>
     * </ul>
     */
    private static class MyHandler extends DefaultHandler implements LexicalHandler {

        /**
         * 当前文本内容缓冲区
         */
        private StringBuilder currentText = new StringBuilder();

        /**
         * 处理元素开始事件
         *
         * @param uri        命名空间URI（如果未使用命名空间则为空字符串）
         * @param localName  本地名称（如果命名空间处理启用）
         * @param qName      带命名空间前缀的限定名称
         * @param attributes 元素的属性列表
         */
        @Override
        /**
         * startElement方法。
         *      * @param uri String类型参数
         * @param localName String类型参数
         * @param qName String类型参数
         * @param attributes Attributes类型参数
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            System.out.println("START: " + qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                System.out.println("  @" + attributes.getQName(i) + " = " + attributes.getValue(i));
            }
            currentText.setLength(0);
        }

        /**
         * 处理文本内容事件
         *
         * @param ch     字符数组
         * @param start  起始位置
         * @param length 字符数量
         */
        @Override
        /**
         * characters方法。
         *      * @param ch char[]类型参数
         * @param start int类型参数
         * @param length int类型参数
         */
        public void characters(char[] ch, int start, int length) {
            currentText.append(new String(ch, start, length));
        }

        /**
         * 处理元素结束事件
         *
         * @param uri       命名空间URI
         * @param localName 本地名称
         * @param qName     带命名空间前缀的限定名称
         */
        @Override
        /**
         * endElement方法。
         *      * @param uri String类型参数
         * @param localName String类型参数
         * @param qName String类型参数
         */
        public void endElement(String uri, String localName, String qName) {
            String text = currentText.toString().trim();
            if (!text.isEmpty()) {
                System.out.println("  TEXT: " + text);
            }
            System.out.println("END: " + qName);
            currentText.setLength(0);
        }

        // LexicalHandler 实现（为空，SAX2 已够用）

        /**
         * 处理DTD开始事件
         *
         * @param name     文档类型名称
         * @param publicId 公共标识符
         * @param systemId 系统标识符
         */
        @Override
        /**
         * startDTD方法。
         *      * @param name String类型参数
         * @param publicId String类型参数
         * @param systemId String类型参数
         */
        public void startDTD(String name, String publicId, String systemId) {
        }

        /**
         * 处理DTD结束事件
         */
        @Override
        /**
         * endDTD方法。
         */
        public void endDTD() {
        }

        /**
         * 处理实体引用开始事件
         *
         * @param name 实体名称
         */
        @Override
        /**
         * startEntity方法。
         *      * @param name String类型参数
         */
        public void startEntity(String name) {
        }

        /**
         * 处理实体引用结束事件
         *
         * @param name 实体名称
         */
        @Override
        /**
         * endEntity方法。
         *      * @param name String类型参数
         */
        public void endEntity(String name) {
        }

        /**
         * 处理CDATA节开始事件
         */
        @Override
        /**
         * startCDATA方法。
         */
        public void startCDATA() {
        }

        /**
         * 处理CDATA节结束事件
         */
        @Override
        /**
         * endCDATA方法。
         */
        public void endCDATA() {
        }

        /**
         * 处理注释事件
         *
         * @param ch     字符数组
         * @param start  起始位置
         * @param length 字符数量
         */
        @Override
        /**
         * comment方法。
         *      * @param ch char[]类型参数
         * @param start int类型参数
         * @param length int类型参数
         */
        public void comment(char[] ch, int start, int length) {
        }
    }
}
