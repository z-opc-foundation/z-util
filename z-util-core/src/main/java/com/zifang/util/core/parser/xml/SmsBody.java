package com.zifang.util.core.parser.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * 请求内容
 *
 * @author zifang
 * Jul 25, 2013 9:34:16 PM
 */
public class SmsBody {

    /**
     * 短信内容
     */
    private String content;

    /**
     * 手机号
     */
    private String sourceAddr;

    /**
     * 服务代码
     */
    private String destAddr;

    /**
     * getContent方法。
     *
     * @return String类型返回值
     */
    public String getContent() {
        return content;
    }

    @XmlElement(name = "CONTENT")
    /**
     * setContent方法。
     *      * @param content String类型参数
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * getSourceAddr方法。
     *
     * @return String类型返回值
     */
    public String getSourceAddr() {
        return sourceAddr;
    }

    @XmlElement(name = "SOURCEADDR")
    /**
     * setSourceAddr方法。
     *      * @param sourceAddr String类型参数
     */
    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    /**
     * getDestAddr方法。
     *
     * @return String类型返回值
     */
    public String getDestAddr() {
        return destAddr;
    }

    @XmlElement(name = "DESTADDR")
    /**
     * setDestAddr方法。
     *      * @param destAddr String类型参数
     */
    public void setDestAddr(String destAddr) {
        this.destAddr = destAddr;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "SmsBody{content=" + content + ", sourceAddr=" + sourceAddr + ", destAddr=" + destAddr + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsBody smsBody = (SmsBody) o;
        return java.util.Objects.equals(content, smsBody.content) &&
                java.util.Objects.equals(sourceAddr, smsBody.sourceAddr) &&
                java.util.Objects.equals(destAddr, smsBody.destAddr);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(content, sourceAddr, destAddr);
    }

}