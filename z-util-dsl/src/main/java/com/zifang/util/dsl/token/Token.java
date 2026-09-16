package com.zifang.util.dsl.token;

/**
 * Token接口
 * 表示词法分析产生的词法单元
 */
public interface Token {

    /**
     * 获取Token类型编码
     *
     * @return Token类型对应的整型数值
     */
    int getType();

    /**
     * 获取Token的文本值
     *
     * @return Token匹配的原始文本
     */
    String getText();

    /**
     * 获取Token所在的行号
     *
     * @return 行号（从1开始）
     */
    int getLine();

    /**
     * 获取Token所在的列号
     *
     * @return 列号（从1开始）
     */
    int getColumn();

    /**
     * 获取Token类型名称
     *
     * @return Token类型的字符串名称
     */
    String getTokenName();
}