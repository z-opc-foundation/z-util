package com.zifang.util.expression.define;


/**
 * 语法分析器接口
 * 负责将Token序列解析为抽象语法树（AST）
 *
 * @author zifang
 * @version 1.0
 */
public interface SyntacticParser {

    /**
     * 执行语法分析
     *
     * @param tokens Token序列
     * @return 语法分析结果，通常返回AST根节点
     */
    Object parse(Object tokens);

}
