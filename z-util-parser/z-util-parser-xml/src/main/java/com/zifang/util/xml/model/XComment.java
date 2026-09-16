package com.zifang.util.xml.model;

/**
 * XML 注释节点。
 *
 * @author zifang
 */

/**
 * XComment类。
 */
public class XComment implements XNode {

    private final String content;

    /**
     * XComment方法。
     * * @param content String类型参数
     */
    public XComment(String content) {
        this.content = content;
    }

    /**
     * getContent方法。
     *
     * @return String类型返回值
     */
    public String getContent() {
        return content;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "<!--" + content + "-->";
    }
}
