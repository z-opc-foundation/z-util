package com.zifang.util.expression.lexer;

import java.util.List;

/**
 * 词法分析器接口
 * 负责将源代码字符串转换为Token序列
 *
 * @author zifang
 * @version 1.0
 */
public interface Lexer {

    /**
     * 设置要分析的源代码输入
     *
     * @param input 源代码字符串
     */
    void setInput(String input);

    /**
     * 执行词法分析，将源代码转换为Token列表
     *
     * @return Token列表，包含所有识别出的Token
     */
    List<TokenDefinition> tokenize();

}
