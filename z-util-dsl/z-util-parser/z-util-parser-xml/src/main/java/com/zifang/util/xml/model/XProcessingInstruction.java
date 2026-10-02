package com.zifang.util.xml.model;

/**
 * XML 处理指令。
 *
 * @author zifang
 */
public class XProcessingInstruction implements XNode {

    private final String target;

    private final String data;

    /**
     * XProcessingInstruction方法。
     * * @param target String类型参数
     *
     * @param data String类型参数
     */
    public XProcessingInstruction(String target, String data) {
        this.target = target;
        this.data = data;
    }

    /**
     * getTarget方法。
     *
     * @return String类型返回值
     */
    public String getTarget() {
        return target;
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
        return "<?" + target + " " + data + "?>";
    }
}
