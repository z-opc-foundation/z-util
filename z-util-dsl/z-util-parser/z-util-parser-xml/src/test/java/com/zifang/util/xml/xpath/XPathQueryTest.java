package com.zifang.util.xml.xpath;

import com.zifang.util.xml.XmlUtil;
import com.zifang.util.xml.model.XDocument;
import com.zifang.util.xml.model.XElement;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * XPath 查询功能测试。
 */

/**
 * XPathQueryTest类。
 */
public class XPathQueryTest {

    private XDocument buildTestDoc() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<library>" +
                "  <book id=\"b1\" lang=\"zh\">" +
                "    <title>Java编程思想</title>" +
                "    <author>Bruce Eckel</author>" +
                "    <price>108.00</price>" +
                "  </book>" +
                "  <book id=\"b2\" lang=\"en\">" +
                "    <title>Effective Java</title>" +
                "    <author>Joshua Bloch</author>" +
                "    <price>89.00</price>" +
                "  </book>" +
                "  <magazine id=\"m1\">" +
                "    <title>Monthly</title>" +
                "  </magazine>" +
                "</library>";
        return XmlUtil.parse(xml);
    }

    // ===== 路径查询 =====

    @Test
    /**
     * testAbsolutePath方法。
     */
    public void testAbsolutePath() throws Exception {
        XDocument doc = buildTestDoc();
        XElement library = (XElement) XmlUtil.xpathOne(doc, "/library");
        assertNotNull(library);
        assertEquals("library", library.getName());
    }

    @Test
    /**
     * testChildPath方法。
     */
    public void testChildPath() throws Exception {
        XDocument doc = buildTestDoc();
        XElement book = (XElement) XmlUtil.xpathOne(doc, "/library/book");
        assertNotNull(book);
        assertEquals("b1", book.getAttribute("id"));
    }

    @Test
    /**
     * testDescendantPath方法。
     */
    public void testDescendantPath() throws Exception {
        XDocument doc = buildTestDoc();
        Object result = XmlUtil.xpathOne(doc, "//title");
        assertNotNull(result);
        assertTrue(result instanceof XElement);
        assertEquals("Java编程思想", ((XElement) result).getText());
    }

    @Test
    /**
     * testDescendantAll方法。
     */
    public void testDescendantAll() throws Exception {
        XDocument doc = buildTestDoc();
        List<Object> titles = XmlUtil.xpath(doc, "//title");
        assertEquals(3, titles.size());
    }

    // ===== 通配符 =====

    @Test
    /**
     * testWildcardChild方法。
     */
    public void testWildcardChild() throws Exception {
        XDocument doc = buildTestDoc();
        List<Object> children = XmlUtil.xpath(doc, "/library/*");
        assertEquals(3, children.size());
    }

    @Test
    /**
     * testWildcardDescendant方法。
     */
    public void testWildcardDescendant() throws Exception {
        XDocument doc = buildTestDoc();
        List<Object> all = XmlUtil.xpath(doc, "//*");
        assertTrue(all.size() > 3);
    }

    // ===== 属性过滤 =====

    @Test
    /**
     * testAttributeFilter方法。
     */
    public void testAttributeFilter() throws Exception {
        XDocument doc = buildTestDoc();
        XElement zhBook = (XElement) XmlUtil.xpathOne(doc, "//book[@lang='zh']");
        assertNotNull(zhBook);
        assertEquals("b1", zhBook.getAttribute("id"));
    }

    @Test
    /**
     * testAttributeFilterNoMatch方法。
     */
    public void testAttributeFilterNoMatch() throws Exception {
        XDocument doc = buildTestDoc();
        Object result = XmlUtil.xpathOne(doc, "//book[@lang='fr']");
        assertNull(result);
    }

    @Test
    /**
     * testMultipleAttributeFilter方法。
     */
    public void testMultipleAttributeFilter() throws Exception {
        XDocument doc = buildTestDoc();
        List<Object> books = XmlUtil.xpath(doc, "//book[@id]");
        assertEquals(2, books.size());
    }

    // ===== 位置索引 =====

    @Test
    /**
     * testPositionalIndex方法。
     */
    public void testPositionalIndex() throws Exception {
        XDocument doc = buildTestDoc();
        XElement firstBook = (XElement) XmlUtil.xpathOne(doc, "/library/book[1]");
        assertEquals("b1", firstBook.getAttribute("id"));

        XElement secondBook = (XElement) XmlUtil.xpathOne(doc, "/library/book[2]");
        assertEquals("b2", secondBook.getAttribute("id"));
    }

    @Test
    /**
     * testPositionalIndexOutOfRange方法。
     */
    public void testPositionalIndexOutOfRange() throws Exception {
        XDocument doc = buildTestDoc();
        Object result = XmlUtil.xpathOne(doc, "/library/book[99]");
        assertNull(result);
    }

    // ===== 属性选择 =====

    @Test
    /**
     * testAttributeSelection方法。
     */
    public void testAttributeSelection() throws Exception {
        XDocument doc = buildTestDoc();
        Object id = XmlUtil.xpathOne(doc, "//book[1]/@id");
        assertEquals("b1", id);
    }

    @Test
    /**
     * testAllAttributes方法。
     */
    public void testAllAttributes() throws Exception {
        XDocument doc = buildTestDoc();
        List<Object> attrs = XmlUtil.xpath(doc, "//book[1]/@*");
        assertEquals(2, attrs.size()); // id and lang
    }

    // ===== 深度嵌套 =====

    @Test
    /**
     * testDeepNested方法。
     */
    public void testDeepNested() throws Exception {
        XDocument doc = buildTestDoc();
        Object author = XmlUtil.xpathOne(doc, "/library/book[2]/author");
        assertNotNull(author);
        assertEquals("Joshua Bloch", ((XElement) author).getText());
    }

    // ===== getElement / getAttr 便捷方法 =====

    @Test
    /**
     * testGetElement方法。
     */
    public void testGetElement() throws Exception {
        XDocument doc = buildTestDoc();
        XElement price = XmlUtil.getElement(doc, "//book[1]/price");
        assertEquals("108.00", price.getText());
    }

    @Test
    /**
     * testGetAttr方法。
     */
    public void testGetAttr() throws Exception {
        XDocument doc = buildTestDoc();
        String id = XmlUtil.getAttr(doc, "//magazine[@id='m1']/@id");
        assertEquals("m1", id);
    }
}
