package com.zifang.util.xml.model;

/**
 * XML 文本节点。
 *
 * @author zifang
 */

/**
 * XText类。
 */
public class XText implements XNode {

    private String text;

    /**
     * XText方法。
     * * @param text String类型参数
     */
    public XText(String text) {
        this.text = text;
    }

    /**
     * getText方法。
     *
     * @return String类型返回值
     */
    public String getText() {
        return text;
    }

    /**
     * setText方法。
     * * @param text String类型参数
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return text;
    }
}
