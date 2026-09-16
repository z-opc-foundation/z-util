package com.zifang.util.expression.source.lexer;

/**
 * 一个简单的Token。
 * 只有类型和文本值两个属性。
 */
public interface Token {

    /**
     * 获取Token的类型。
     *
     * @return Token类型枚举值
     */
    public TokenType getType();

    /**
     * 获取Token的文本值。
     *
     * @return Token的原始文本内容
     */
    public String getText();

}