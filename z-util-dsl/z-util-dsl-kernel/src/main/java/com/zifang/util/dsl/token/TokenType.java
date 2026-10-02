package com.zifang.util.dsl.token;

/**
 * Token类型接口
 * 定义Token类型的基本信息
 */
public interface TokenType {

    /**
     * 获取Token类型编码
     *
     * @return 类型对应的整型数值
     */
    int getType();

    /**
     * 获取Token类型名称
     *
     * @return 类型名称字符串
     */
    String getName();
}