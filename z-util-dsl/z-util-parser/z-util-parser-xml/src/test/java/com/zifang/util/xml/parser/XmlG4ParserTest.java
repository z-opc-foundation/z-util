package com.zifang.util.xml.parser;

import com.zifang.util.xml.model.XCData;
import com.zifang.util.xml.model.XComment;
import com.zifang.util.xml.model.XDeclaration;
import com.zifang.util.xml.model.XDocument;
import com.zifang.util.xml.model.XElement;
import com.zifang.util.xml.model.XProcessingInstruction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * XmlG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 XDocument。
 * <p>
 * 更全面的覆盖（XML 声明、CDATA、注释、PI、实体、属性、嵌套、错误处理等）
 * 由 XmlParserTest / XmlFormatterTest / XPathQueryTest 在 XmlUtil 入口处验证。
 */
public class XmlG4ParserTest {

    private final XmlG4Parser parser = new XmlG4Parser();

    @Test
    public void testEmptyElement() {
        XDocument doc = parser.parse("<root/>");
        assertEquals("root", doc.getRoot().getName());
        assertTrue(doc.getRoot().getChildren().isEmpty());
    }

    @Test
    public void testElementWithAttributes() {
        XDocument doc = parser.parse("<root id=\"123\" name=\"test\"/>");
        assertEquals("123", doc.getRoot().getAttribute("id"));
        assertEquals("test", doc.getRoot().getAttribute("name"));
    }

    @Test
    public void testNestedElements() {
        XDocument doc = parser.parse("<root><child><grand>value</grand></child></root>");
        XElement child = doc.getRoot().getChildElement("child");
        assertNotNull(child);
        assertEquals("value", child.getChildElement("grand").getText());
    }

    @Test
    public void testCommentAndPi() {
        XDocument doc = parser.parse("<?xml-stylesheet type=\"text/xsl\"?><!-- top --><root/>");
        // PI + COMMENT 均进入 prependNodes
        assertEquals(2, doc.getPrependNodes().size());
        assertTrue(doc.getPrependNodes().get(0) instanceof XProcessingInstruction);
        assertTrue(doc.getPrependNodes().get(1) instanceof XComment);
        XProcessingInstruction pi = (XProcessingInstruction) doc.getPrependNodes().get(0);
        assertEquals("xml-stylesheet", pi.getTarget());
    }

    @Test
    public void testDeclarationAndCdata() {
        XDocument doc = parser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><![CDATA[<>\"'&]]></root>");
        XDeclaration decl = doc.getDeclaration();
        assertNotNull(decl);
        assertEquals("1.0", decl.getVersion());
        assertEquals("UTF-8", decl.getEncoding());
        assertEquals(1, doc.getRoot().getCDataNodes().size());
        assertEquals("<>\"'&", doc.getRoot().getCDataNodes().get(0).getData());
    }

    @Test
    public void testCommentContentPreserved() {
        XDocument doc = parser.parse("<!-- spaced comment --><root/>");
        assertEquals(1, doc.getPrependNodes().size());
        assertTrue(doc.getPrependNodes().get(0) instanceof XComment);
        XComment comment = (XComment) doc.getPrependNodes().get(0);
        assertEquals(" spaced comment ", comment.getContent());
    }
}
