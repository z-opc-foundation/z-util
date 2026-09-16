package com.zifang.util.xml.model;

import java.util.ArrayList;
import java.util.List;

/**
 * XML 文档根节点。
 * <p>
 * 包含 XML 声明、根元素以及顶层注释/处理指令。
 *
 * @author zifang
 */

/**
 * XDocument类。
 */
public class XDocument implements XNode {

    private XDeclaration declaration;

    private XElement root;

    private List<XNode> prependNodes = new ArrayList<>();

    /**
     * XDocument方法。
     */
    public XDocument() {
    }

    /**
     * XDocument方法。
     * * @param root XElement类型参数
     */
    public XDocument(XElement root) {
        this.root = root;
    }

    /**
     * getDeclaration方法。
     *
     * @return XDeclaration类型返回值
     */
    public XDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * setDeclaration方法。
     * * @param declaration XDeclaration类型参数
     */
    public void setDeclaration(XDeclaration declaration) {
        this.declaration = declaration;
    }

    /**
     * getRoot方法。
     *
     * @return XElement类型返回值
     */
    public XElement getRoot() {
        return root;
    }

    /**
     * setRoot方法。
     * * @param root XElement类型参数
     */
    public void setRoot(XElement root) {
        this.root = root;
    }

    /**
     * 根元素之前的顶层节点（注释、处理指令）。
     */
    /**
     * getPrependNodes方法。
     *
     * @return List<XNode>类型返回值
     */
    public List<XNode> getPrependNodes() {
        return prependNodes;
    }

    /**
     * addPrependNode方法。
     * * @param node XNode类型参数
     */
    public void addPrependNode(XNode node) {
        prependNodes.add(node);
    }

    /**
     * getRootElementName方法。
     *
     * @return String类型返回值
     */
    public String getRootElementName() {
        return root == null ? null : root.getName();
    }
}
