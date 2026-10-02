package com.zifang.util.xml.util;

import com.zifang.util.xml.XmlUtil;
import com.zifang.util.xml.model.XDocument;
import com.zifang.util.xml.model.XElement;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * XmlFormatter 格式化功能测试。
 */

/**
 * XmlFormatterTest类。
 */
public class XmlFormatterTest {

    @Test
    /**
     * testFormat方法。
     */
    public void testFormat() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root id=\"1\"><child>text</child></root>";
        XDocument doc = XmlUtil.parse(xml);
        String formatted = XmlUtil.toXml(doc);
        assertTrue(formatted.contains("id=\"1\""));
        assertTrue(formatted.contains("<child>"));
        assertTrue(formatted.contains("</child>"));
    }

    @Test
    /**
     * testFormatCompact方法。
     */
    public void testFormatCompact() throws Exception {
        String xml = "<root id=\"1\"><child>text</child></root>";
        XDocument doc = XmlUtil.parse(xml);
        String compact = XmlUtil.toCompactXml(doc);
        assertFalse(compact.contains("\n"));
        assertFalse(compact.contains("  "));
    }

    @Test
    /**
     * testFormatWithChinese方法。
     */
    public void testFormatWithChinese() throws Exception {
        String xml = "<root>你好世界</root>";
        XDocument doc = XmlUtil.parse(xml);
        String output = XmlUtil.toXml(doc);
        assertTrue(output.contains("你好世界"));
    }

    @Test
    /**
     * testEscapeXmlText方法。
     */
    public void testEscapeXmlText() throws Exception {
        String escaped = XmlFormatter.escapeXmlText("a & b < c > d \" e ' f");
        assertEquals("a &amp; b &lt; c &gt; d &quot; e &apos; f", escaped);
    }

    @Test
    /**
     * testEscapeXmlAttr方法。
     */
    public void testEscapeXmlAttr() throws Exception {
        String escaped = XmlFormatter.escapeXmlAttr("a & b < c > d \" e ' f");
        assertEquals("a &amp; b &lt; c &gt; d &quot; e &apos; f", escaped);
    }

    @Test
    /**
     * testWrapCData方法。
     */
    public void testWrapCData() throws Exception {
        String wrapped = XmlFormatter.wrapCData("hello]]>world");
        assertEquals("hello]]><![CDATA[>world", wrapped);
    }

    @Test
    /**
     * testWrapCDataNormal方法。
     */
    public void testWrapCDataNormal() throws Exception {
        String wrapped = XmlFormatter.wrapCData("hello world");
        assertEquals("hello world", wrapped);
    }

    // ===== 序列化 API =====

    @Test
    /**
     * testElementBuilder方法。
     */
    public void testElementBuilder() throws Exception {
        XElement book = XmlUtil.element("book",
                XmlUtil.element("title", XmlUtil.text("Java编程思想")),
                XmlUtil.element("author", XmlUtil.text("Bruce Eckel"))
        );
        book.setAttribute("id", "b1");
        XDocument doc = XmlUtil.document("UTF-8", book);
        String xml = XmlUtil.toXml(doc);
        assertTrue(xml.contains("<title>Java编程思想</title>"));
        assertTrue(xml.contains("<author>Bruce Eckel</author>"));
        assertTrue(xml.contains("id=\"b1\""));
    }

    @Test
    /**
     * testElementWithText方法。
     */
    public void testElementWithText() throws Exception {
        XElement e = XmlUtil.elementWithText("title", "Java编程思想");
        XDocument doc = XmlUtil.document(e);
        String xml = XmlUtil.toXml(doc);
        assertTrue(xml.contains("Java编程思想"));
    }
}
