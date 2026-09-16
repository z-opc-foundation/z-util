package com.zifang.util.zex.interview.demo2;

/**
 * 接口A。
 * <p>
 * 定义获取名称、年份以及获取IB接口实例的方法。
 *
 * @author zifang
 * @version 1.0
 */
public interface IA {

    /**
     * 获取名称。
     *
     * @return 名称字符串
     */
    String getName();

    /**
     * 获取年份。
     *
     * @return 年份字符串
     */
    String getYear();

    /**
     * 根据参数获取IB接口实例。
     *
     * @param b 参数
     * @return IB接口实例
     */
    IB getIB(String b);
}
