package com.zifang.util.expression.lexer;

/**
 * Token定义接口
 * 用于描述词法分析器中Token的元数据信息
 *
 * @author zifang
 * @version 1.0
 */
public interface TokenDefinition {

    /**
     * 获取Token的名称
     *
     * @return Token名称
     */
    String getName();

    /**
     * 获取Token的正则表达式模式
     *
     * @return 正则表达式字符串
     */
    String getPattern();

    /**
     * 获取Token的优先级
     *
     * @return 优先级，数值越大优先级越高
     */
    int getPriority();

    /**
     * 检查是否为隐藏Token（不参与语法分析）
     *
     * @return true表示隐藏，false表示正常Token
     */
    boolean isHidden();

    /**
     * 检查是否为Fragment规则（不直接产生Token）
     *
     * @return true表示为Fragment，false表示正常规则
     */
    boolean isFragment();

}
