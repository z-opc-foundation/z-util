package com.zifang.util.expression.source.lexer;

/**
 * Token的一个简单实现。只有类型和文本值两个属性。
 */

final class SimpleToken implements Token {
    //Token类型
    public TokenType type = null;

    //文本值
    public String text = null;


    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String getText() {
        return text;
    }
}
