package com.zifang.util.expression.define;

/**
 * 表达式引擎接口
 * 提供表达式解析和求值的统一入口
 *
 * @author zifang
 * @version 1.0
 */
public interface ExpressionEngine {

    /**
     * 执行表达式并返回结果
     *
     * @param expression 要执行的表达式字符串
     * @return 表达式的求值结果
     */
    Object evaluate(String expression);

    /**
     * 检查表达式是否有效
     *
     * @param expression 要检查的表达式字符串
     * @return true表示表达式有效，false表示无效
     */
    boolean validate(String expression);

}
