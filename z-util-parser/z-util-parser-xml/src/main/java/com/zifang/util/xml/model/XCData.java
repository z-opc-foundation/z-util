package com.zifang.util.xml.model;

/**
 * XML CDATA 节。
 *
 * @author zifang
 */

/**
 * XCData类。
 */
public class XCData implements XNode {

    private final String data;

    /**
     * XCData方法。
     * * @param data String类型参数
     */
    public XCData(String data) {
        this.data = data;
    }

    /**
     * getData方法。
     *
     * @return String类型返回值
     */
    public String getData() {
        return data;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return data;
    }
}
