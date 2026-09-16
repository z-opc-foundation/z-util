package com.zifang.util.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * XPathDemo类。
 */
public class XPathDemo {

    private static Document doc;
    private static XPath xpath;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        init();
        getRootEle();
        getChildEles();
        getPartEles();
        haveChildsEles();
        getLevelEles();
        getAttrEles();

        //打印根节点下的所有元素节点
        System.out.println("----------------打印根节点下的所有元素节点----------");
        System.out.println(doc.getDocumentElement().getChildNodes().getLength());
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                System.out.print(nodeList.item(i).getNodeName() + " ");
            }
        }
    }

    // 初始化Document、XPath对象

    /**
     * init方法。
     *
     * @return static void类型返回值
     */
    public static void init() throws Exception {
        // 创建Document对象
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(XPathDemo.class.getResourceAsStream("demo.xml"));

        // 创建XPath对象
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
    }

    // 获取根元素
    // 表达式可以更换为/*,/rss

    /**
     * getRootEle方法。
     *
     * @return static void类型返回值
     */
    public static void getRootEle() throws XPathExpressionException {
        Node node = (Node) xpath.evaluate("/rss", doc, XPathConstants.NODE);
        System.out.println(node.getNodeName() + "--------"
                + node.getNodeValue());
    }

    // 获取子元素并打印

    /**
     * getChildEles方法。
     *
     * @return static void类型返回值
     */
    public static void getChildEles() throws XPathExpressionException {
        NodeList nodeList = (NodeList) xpath.evaluate("/rss/channel/*", doc,
                XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.print(nodeList.item(i).getNodeName() + " ");
        }
        System.out.println();
    }

    // 获取部分元素
    // 只获取元素名称为title的元素

    /**
     * getPartEles方法。
     *
     * @return static void类型返回值
     */
    public static void getPartEles() throws XPathExpressionException {
        NodeList nodeList = (NodeList) xpath.evaluate("//*[name() = 'title']",
                doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.print(nodeList.item(i).getNodeName() + "-->"
                    + nodeList.item(i).getTextContent());
        }
        System.out.println();
    }

    // 获取包含子节点的元素

    /**
     * haveChildsEles方法。
     *
     * @return static void类型返回值
     */
    public static void haveChildsEles() throws XPathExpressionException {
        NodeList nodeList = (NodeList) xpath.evaluate("//*[*]", doc,
                XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.print(nodeList.item(i).getNodeName() + " ");
        }
        System.out.println();
    }

    // 获取指定层级的元素

    /**
     * getLevelEles方法。
     *
     * @return static void类型返回值
     */
    public static void getLevelEles() throws XPathExpressionException {
        NodeList nodeList = (NodeList) xpath.evaluate("/*/*/*/*", doc,
                XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.print(nodeList.item(i).getNodeName() + "-->"
                    + nodeList.item(i).getTextContent() + " ");
        }
        System.out.println("-----------------------------");
    }

    // 获取指定属性的元素
    // 获取所有大于指定价格的书箱

    /**
     * getAttrEles方法。
     *
     * @return static void类型返回值
     */
    public static void getAttrEles() throws XPathExpressionException {
        NodeList nodeList = (NodeList) xpath.evaluate("//bookstore/book[price>35.00]/title", doc,
                XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.print(nodeList.item(i).getNodeName() + "-->"
                    + nodeList.item(i).getTextContent() + " ");
        }
        System.out.println();
    }
}