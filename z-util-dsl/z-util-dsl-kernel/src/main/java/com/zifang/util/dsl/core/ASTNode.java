package com.zifang.util.dsl.core;

import java.util.List;

/**
 * AST节点接口
 */
public interface ASTNode {

    /**
     * 获取节点类型
     */
    String getType();

    /**
     * 获取文本值
     */
    String getText();

    /**
     * 获取行号
     */
    int getLine();

    /**
     * 获取列号
     */
    int getColumn();

    /**
     * 获取子节点
     */
    List<ASTNode> getChildren();

    /**
     * 添加子节点
     */
    void addChild(ASTNode child);

    /**
     * 获取父节点
     */
    ASTNode getParent();

    /**
     * 设置父节点
     */
    void setParent(ASTNode parent);

    /**
     * 获取Token
     */
    Object getToken();

    /**
     * 设置Token
     */
    void setToken(Object token);
}