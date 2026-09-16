package com.zifang.util.dsl.ast;

import com.zifang.util.dsl.core.ASTNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 简单AST节点实现
 * ASTNode接口的标准实现类，用于构建抽象语法树
 */
public class SimpleASTNode implements ASTNode {

    private final List<ASTNode> children;
    private String type;
    private String text;
    private int line;
    private int column;
    private ASTNode parent;
    private Object token;

    /**
     * 默认构造函数
     */
    public SimpleASTNode() {
        this.children = new ArrayList<>();
    }

    /**
     * 构造函数
     *
     * @param type   节点类型
     * @param text   节点文本值
     * @param line   行号
     * @param column 列号
     */
    public SimpleASTNode(String type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
        this.children = new ArrayList<>();
    }

    @Override
    /**
     * getType方法。
     * @return String类型返回值
     */
    public String getType() {
        return type;
    }

    /**
     * 设置节点类型
     *
     * @param type 节点类型字符串
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    /**
     * getText方法。
     * @return String类型返回值
     */
    public String getText() {
        return text;
    }

    /**
     * 设置节点文本值
     *
     * @param text 文本值
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    /**
     * getLine方法。
     * @return int类型返回值
     */
    public int getLine() {
        return line;
    }

    /**
     * 设置行号
     *
     * @param line 行号（从1开始）
     */
    public void setLine(int line) {
        this.line = line;
    }

    @Override
    /**
     * getColumn方法。
     * @return int类型返回值
     */
    public int getColumn() {
        return column;
    }

    /**
     * 设置列号
     *
     * @param column 列号（从1开始）
     */
    public void setColumn(int column) {
        this.column = column;
    }

    @Override
    /**
     * getChildren方法。
     * @return List<ASTNode>类型返回值
     */
    public List<ASTNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    /**
     * addChild方法。
     *      * @param child ASTNode类型参数
     */
    public void addChild(ASTNode child) {
        children.add(child);
        if (child != null) {
            child.setParent(this);
        }
    }

    @Override
    /**
     * getParent方法。
     * @return ASTNode类型返回值
     */
    public ASTNode getParent() {
        return parent;
    }

    @Override
    /**
     * setParent方法。
     *      * @param parent ASTNode类型参数
     */
    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    @Override
    /**
     * getToken方法。
     * @return Object类型返回值
     */
    public Object getToken() {
        return token;
    }

    @Override
    /**
     * setToken方法。
     *      * @param token Object类型参数
     */
    public void setToken(Object token) {
        this.token = token;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ASTNode{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }

    /**
     * 打印AST树（用于调试）
     *
     * @param sb     StringBuilder用于接收输出
     * @param indent 缩进层级
     */
    public void dump(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append(type);
        if (text != null && !text.isEmpty()) {
            sb.append(": ").append(text);
        }
        sb.append("\n");
        for (ASTNode child : children) {
            if (child instanceof SimpleASTNode) {
                ((SimpleASTNode) child).dump(sb, indent + 1);
            }
        }
    }
}