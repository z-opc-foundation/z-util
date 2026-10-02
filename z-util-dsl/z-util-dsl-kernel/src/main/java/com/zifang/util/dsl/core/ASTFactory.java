package com.zifang.util.dsl.core;

/**
 * AST工厂接口
 */
public interface ASTFactory {

    /**
     * 创建AST节点
     */
    ASTNode createNode(String type, String text, int line, int column);

    /**
     * 根据Token创建AST节点
     */
    ASTNode createNode(Object token);
}