package com.zifang.util.xml.model;

/**
 * XML 属性节点。
 *
 * @author zifang
 */

/**
 * XAttribute类。
 */
public class XAttribute {

    private final String name;

    private String value;

    /**
     * XAttribute方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     */
    public XAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getValue方法。
     *
     * @return String类型返回值
     */
    public String getValue() {
        return value;
    }

    /**
     * setValue方法。
     * * @param value String类型参数
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return name + "=\"" + value + "\"";
    }
}
