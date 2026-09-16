package com.zifang.util.xml.parser;

import com.zifang.util.xml.XmlUtil;
import com.zifang.util.xml.model.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * XmlParser 核心功能测试。
 */

/**
 * XmlParserTest类。
 */
public class XmlParserTest {

    // ===== 基础解析 =====

    @Test
    /**
     * testParseEmptyElement方法。
     */
    public void testParseEmptyElement() throws Exception {
        XDocument doc = XmlUtil.parse("<root/>");
        assertEquals("root", doc.getRoot().getName());
        assertTrue(doc.getRoot().getChildren().isEmpty());
    }

    @Test
    /**
     * testParseElementWithText方法。
     */
    public void testParseElementWithText() throws Exception {
        XDocument doc = XmlUtil.parse("<root>hello</root>");
        assertEquals("hello", doc.getRoot().getText());
    }

    @Test
    /**
     * testParseElementWithAttributes方法。
     */
    public void testParseElementWithAttributes() throws Exception {
        XDocument doc = XmlUtil.parse("<root id=\"123\" name=\"test\"/>");
        assertEquals("123", doc.getRoot().getAttribute("id"));
        assertEquals("test", doc.getRoot().getAttribute("name"));
    }

    @Test
    /**
     * testParseNestedElements方法。
     */
    public void testParseNestedElements() throws Exception {
        XDocument doc = XmlUtil.parse("<root><child><grand>value</grand></child></root>");
        XElement root = doc.getRoot();
        XElement child = root.getChildElement("child");
        assertNotNull(child);
        assertEquals("value", child.getChildElement("grand").getText());
    }

    @Test
    /**
     * testParseMultipleChildren方法。
     */
    public void testParseMultipleChildren() throws Exception {
        XDocument doc = XmlUtil.parse("<root><a>1</a><a>2</a><b>3</b></root>");
        List<XElement> aList = doc.getRoot().getChildElements("a");
        assertEquals(2, aList.size());
        assertEquals("1", aList.get(0).getText());
        assertEquals("2", aList.get(1).getText());
    }

    // ===== 自闭合标签 =====

    @Test
    /**
     * testParseSelfClosingTag方法。
     */
    public void testParseSelfClosingTag() throws Exception {
        XDocument doc = XmlUtil.parse("<root><br/><img src=\"x.png\"/></root>");
        List<XElement> children = doc.getRoot().getChildElements();
        assertEquals(2, children.size());
        assertEquals("br", children.get(0).getName());
        assertEquals("x.png", children.get(1).getAttribute("src"));
    }

    // ===== XML 声明 =====

    @Test
    /**
     * testParseDeclaration方法。
     */
    public void testParseDeclaration() throws Exception {
        XDocument doc = XmlUtil.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>");
        assertNotNull(doc.getDeclaration());
        assertEquals("1.0", doc.getDeclaration().getVersion());
        assertEquals("UTF-8", doc.getDeclaration().getEncoding());
    }

    @Test
    /**
     * testParseDeclarationWithStandalone方法。
     */
    public void testParseDeclarationWithStandalone() throws Exception {
        XDocument doc = XmlUtil.parse("<?xml version=\"1.0\" standalone=\"yes\"?><root/>");
        assertEquals("yes", doc.getDeclaration().getStandalone());
    }

    // ===== 文本与混合内容 =====

    @Test
    /**
     * testParseMixedContent方法。
     */
    public void testParseMixedContent() throws Exception {
        XDocument doc = XmlUtil.parse("<root>text<b>bold</b>more</root>");
        XElement root = doc.getRoot();
        assertEquals(3, root.getChildren().size());
        assertEquals("text", ((XText) root.getChildren().get(0)).getText());
        assertEquals("bold", root.getChildElement("b").getText());
        assertEquals("more", ((XText) root.getChildren().get(2)).getText());
    }

    @Test
    /**
     * testParseTextWithWhitespace方法。
     */
    public void testParseTextWithWhitespace() throws Exception {
        XDocument doc = XmlUtil.parse("<root>  spaces  </root>");
        assertEquals("  spaces  ", doc.getRoot().getText());
    }

    // ===== CDATA =====

    @Test
    /**
     * testParseCData方法。
     */
    public void testParseCData() throws Exception {
        XDocument doc = XmlUtil.parse("<root><![CDATA[<>\"'&文字]]></root>");
        List<XCData> cdataNodes = doc.getRoot().getCDataNodes();
        assertEquals(1, cdataNodes.size());
        assertEquals("<>\"'&文字", cdataNodes.get(0).getData());
    }

    @Test
    /**
     * testParseCDataInMixedContent方法。
     */
    public void testParseCDataInMixedContent() throws Exception {
        XDocument doc = XmlUtil.parse("<root>before<![CDATA[code]]>after</root>");
        assertEquals(3, doc.getRoot().getChildren().size());
        assertEquals("before", ((XText) doc.getRoot().getChildren().get(0)).getText());
        assertEquals("code", ((XCData) doc.getRoot().getChildren().get(1)).getData());
        assertEquals("after", ((XText) doc.getRoot().getChildren().get(2)).getText());
    }

    // ===== 注释 =====

    @Test
    /**
     * testParseComment方法。
     */
    public void testParseComment() throws Exception {
        XDocument doc = XmlUtil.parse("<!-- comment --><root/>");
        assertEquals(1, doc.getPrependNodes().size());
        assertTrue(doc.getPrependNodes().get(0) instanceof XComment);
        assertEquals(" comment ", ((XComment) doc.getPrependNodes().get(0)).getContent());
    }

    @Test
    /**
     * testParseCommentInElement方法。
     */
    public void testParseCommentInElement() throws Exception {
        XDocument doc = XmlUtil.parse("<root><!-- inner -->text</root>");
        List<XNode> children = doc.getRoot().getChildren();
        assertEquals(2, children.size());
        assertTrue(children.get(0) instanceof XComment);
    }

    // ===== 处理指令 =====

    @Test
    /**
     * testParseProcessingInstruction方法。
     */
    public void testParseProcessingInstruction() throws Exception {
        XDocument doc = XmlUtil.parse("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?><root/>");
        assertEquals(1, doc.getPrependNodes().size());
        assertTrue(doc.getPrependNodes().get(0) instanceof XProcessingInstruction);
        XProcessingInstruction pi = (XProcessingInstruction) doc.getPrependNodes().get(0);
        assertEquals("xml-stylesheet", pi.getTarget());
    }

    // ===== 实体引用 =====

    @Test
    /**
     * testParseEntityReferences方法。
     */
    public void testParseEntityReferences() throws Exception {
        XDocument doc = XmlUtil.parse("<root>&amp;&lt;&gt;&quot;&apos;</root>");
        assertEquals("&<>\"'", doc.getRoot().getText());
    }

    @Test
    /**
     * testParseNumericEntityReferences方法。
     */
    public void testParseNumericEntityReferences() throws Exception {
        XDocument doc = XmlUtil.parse("<root>&#65;&#x41;</root>");
        assertEquals("AA", doc.getRoot().getText());
    }

    @Test
    /**
     * testParseChineseCharacters方法。
     */
    public void testParseChineseCharacters() throws Exception {
        XDocument doc = XmlUtil.parse("<root>你好世界</root>");
        assertEquals("你好世界", doc.getRoot().getText());
    }

    @Test
    /**
     * testParseEntityInAttribute方法。
     */
    public void testParseEntityInAttribute() throws Exception {
        XDocument doc = XmlUtil.parse("<root attr=\"a&lt;b\"/>");
        assertEquals("a<b", doc.getRoot().getAttribute("attr"));
    }

    // ===== 错误处理 =====

    @Test(expected = com.zifang.util.xml.exception.XmlParseException.class)
    /**
     * testParseMismatchedTags方法。
     */
    public void testParseMismatchedTags() throws Exception {
        XmlUtil.parse("<root><child></wrong></root>");
    }

    @Test(expected = com.zifang.util.xml.exception.XmlParseException.class)
    /**
     * testParseUnclosedTag方法。
     */
    public void testParseUnclosedTag() throws Exception {
        XmlUtil.parse("<root><child></root>");
    }

    // ===== 序列化往返 =====

    @Test
    /**
     * testRoundtrip方法。
     */
    public void testRoundtrip() throws Exception {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root id=\"1\"><child>text</child></root>";
        XDocument doc = XmlUtil.parse(input);
        String output = XmlUtil.toXml(doc);
        XDocument reparsed = XmlUtil.parse(output);
        assertEquals("1", reparsed.getRoot().getAttribute("id"));
        assertEquals("text", reparsed.getRoot().getChildElement("child").getText());
    }

    @Test
    /**
     * testRoundtripComplex方法。
     */
    public void testRoundtripComplex() throws Exception {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<catalog>\n" +
                "  <book id=\"b1\">\n" +
                "    <title>Java编程思想</title>\n" +
                "    <author>Bruce Eckel</author>\n" +
                "    <price>108.00</price>\n" +
                "  </book>\n" +
                "</catalog>";
        XDocument doc = XmlUtil.parse(xml);
        String output = XmlUtil.toXml(doc);
        assertTrue(output.contains("<title>Java编程思想</title>"));
        assertTrue(output.contains("Bruce Eckel"));
    }

    // ===== XElement 导航 API =====

    @Test
    /**
     * testGetChildElement方法。
     */
    public void testGetChildElement() throws Exception {
        XDocument doc = XmlUtil.parse("<root><a><b/></a><c/></root>");
        XElement a = doc.getRoot().getChildElement("a");
        assertNotNull(a);
        assertNotNull(a.getChildElement("b"));
        assertNull(doc.getRoot().getChildElement("notexist"));
    }

    @Test
    /**
     * testGetDescendantElement方法。
     */
    public void testGetDescendantElement() throws Exception {
        XDocument doc = XmlUtil.parse("<root><a><b><c/></b></a></root>");
        XElement c = doc.getRoot().getDescendantElement("c");
        assertNotNull(c);
        assertEquals("c", c.getName());
        assertEquals("b", c.getParent().getName());
    }

    @Test
    /**
     * testGetTextTrim方法。
     */
    public void testGetTextTrim() throws Exception {
        XDocument doc = XmlUtil.parse("<root>  空格  </root>");
        assertEquals("空格", doc.getRoot().getTextTrim());
    }

    @Test
    /**
     * testSetText方法。
     */
    public void testSetText() throws Exception {
        XElement e = XmlUtil.element("root");
        e.setText("hello");
        assertEquals("hello", e.getText());
    }

    // ===== valueless attribute =====

    @Test
    /**
     * testValuelessAttribute方法。
     */
    public void testValuelessAttribute() throws Exception {
        XDocument doc = XmlUtil.parse("<root disabled/>");
        assertTrue(doc.getRoot().hasAttribute("disabled"));
        assertEquals("", doc.getRoot().getAttribute("disabled"));
    }

    // ===== Deep Clone =====

    @Test
    /**
     * testDeepClone方法。
     */
    public void testDeepClone() throws Exception {
        XDocument doc = XmlUtil.parse("<root id=\"1\"><child>text</child></root>");
        XElement clone = doc.getRoot().deepClone();
        assertEquals("root", clone.getName());
        assertEquals("1", clone.getAttribute("id"));
        assertEquals("text", clone.getChildElement("child").getText());
    }
}
