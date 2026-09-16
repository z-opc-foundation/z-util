package com.zifang.util.expression.source.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一个简单的AST节点的实现。
 * 属性包括：类型、文本值、父节点、子节点。
 */
public class SimpleASTNode implements ASTNode {
    SimpleASTNode parent = null;
    List<ASTNode> children = new ArrayList<ASTNode>();
    List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
    ASTNodeType nodeType = null;
    String text = null;


    public SimpleASTNode(ASTNodeType nodeType, String text) {
        this.nodeType = nodeType;
        this.text = text;
    }

    @Override
    public ASTNode getParent() {
        return parent;
    }

    @Override
    public List<ASTNode> getChildren() {
        return readonlyChildren;
    }

    @Override
    public ASTNodeType getType() {
        return nodeType;
    }

    @Override
    public String getText() {
        return text;
    }

    public void addChild(SimpleASTNode child) {
        children.add(child);
        child.parent = this;
    }

}
