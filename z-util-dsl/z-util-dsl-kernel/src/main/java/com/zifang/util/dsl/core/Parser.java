package com.zifang.util.dsl.core;

/**
 * 语法分析器接口
 */
public interface Parser {

    /**
     * 设置Token源
     */
    void setTokenReader(TokenReader tokenReader);

    /**
     * 执行语法分析，返回AST根节点
     */
    ASTNode parse();

    /**
     * 执行语法分析，返回指定规则的AST
     */
    ASTNode parse(String startRule);
}