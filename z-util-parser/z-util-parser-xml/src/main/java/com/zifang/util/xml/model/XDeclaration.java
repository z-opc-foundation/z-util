package com.zifang.util.xml.model;

/**
 * XML 文档声明。
 *
 * @author zifang
 */

/**
 * XDeclaration类。
 */
public class XDeclaration {

    private String version = "1.0";

    private String encoding;

    private String standalone;

    /**
     * XDeclaration方法。
     */
    public XDeclaration() {
    }

    /**
     * XDeclaration方法。
     * * @param version String类型参数
     *
     * @param encoding   String类型参数
     * @param standalone String类型参数
     */
    public XDeclaration(String version, String encoding, String standalone) {
        this.version = version;
        this.encoding = encoding;
        this.standalone = standalone;
    }

    /**
     * getVersion方法。
     *
     * @return String类型返回值
     */
    public String getVersion() {
        return version;
    }

    /**
     * setVersion方法。
     * * @param version String类型参数
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * getEncoding方法。
     *
     * @return String类型返回值
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * setEncoding方法。
     * * @param encoding String类型参数
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * getStandalone方法。
     *
     * @return String类型返回值
     */
    public String getStandalone() {
        return standalone;
    }

    /**
     * setStandalone方法。
     * * @param standalone String类型参数
     */
    public void setStandalone(String standalone) {
        this.standalone = standalone;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("<?xml version=\"" + version + "\"");
        if (encoding != null) {
            sb.append(" encoding=\"").append(encoding).append("\"");
        }
        if (standalone != null) {
            sb.append(" standalone=\"").append(standalone).append("\"");
        }
        sb.append("?>");
        return sb.toString();
    }
}
