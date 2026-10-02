package com.zifang.util.xml.binding;

import com.zifang.util.xml.model.*;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 使用 JDK 内置 SAX 解析器直接构建 XDocument，绕过 G4 词法分析。
 * <p>
 * 用于 {@link XmlBinding#fromXml} 的快速路径，比 G4 路径快 ~25x（大 XML 场景）。
 * 生成的 XDocument 与 {@link com.zifang.util.xml.XmlUtil#parse} 产出完全兼容，
 * 可直接传给 {@link XmlBinder#unmarshal} 做 Bean 填充。
 * <p>
 * 限制：不处理 XML 声明、PI、CDATA、注释（绑定场景不需要）。
 *
 * @author zifang
 */
final class SaxXDocumentBuilder {

    private SaxXDocumentBuilder() {
    }

    /**
     * 将 XML 字符串解析为 XDocument。
     *
     * @param xml XML 字符串
     * @return XDocument
     * @throws XmlBindingException 解析失败
     */
    static XDocument parse(String xml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            // 禁用外部实体（安全）
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser parser = factory.newSAXParser();

            XDocument doc = new XDocument();
            Handler handler = new Handler(doc);
            parser.parse(new InputSource(new StringReader(xml)), handler);
            return doc;
        } catch (XmlBindingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlBindingException("SAX 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * SAX 事件处理器：将 SAX 事件流转换为 XDocument 树。
     * <p>
     * 使用栈式设计，每个 XML 元素层级对应栈中的一个 XElement。
     */
    private static class Handler extends DefaultHandler {
        private final XDocument doc;
        private final Deque<XElement> elementStack = new ArrayDeque<>();
        private final StringBuilder textBuffer = new StringBuilder();

        Handler(XDocument doc) {
            this.doc = doc;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            flushText();
            XElement element = new XElement(qName);
            // 复制属性
            for (int i = 0; i < attrs.getLength(); i++) {
                element.setAttribute(attrs.getQName(i), attrs.getValue(i));
            }
            if (elementStack.isEmpty()) {
                doc.setRoot(element);
            } else {
                elementStack.peek().addChild(element);
            }
            elementStack.push(element);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            flushText();
            if (!elementStack.isEmpty()) {
                elementStack.pop();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            textBuffer.append(ch, start, length);
        }

        private void flushText() {
            if (textBuffer.length() == 0) return;
            String text = textBuffer.toString();
            textBuffer.setLength(0);
            if (!elementStack.isEmpty()) {
                // 保留空白文本（与 G4 preserveWhitespace 行为一致）
                elementStack.peek().addChild(new XText(text));
            }
        }
    }
}
