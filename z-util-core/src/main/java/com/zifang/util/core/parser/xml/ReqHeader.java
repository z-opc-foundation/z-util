package com.zifang.util.core.parser.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * 请求头对象
 */
public class ReqHeader {

    /**
     * 系统编号
     */
    private String sysId;

    /**
     * 鉴权码
     */
    private String authCode;

    /**
     * 流水号
     */
    private String reqNo;

    /**
     * getSysId方法。
     *
     * @return String类型返回值
     */
    public String getSysId() {
        return sysId;
    }

    @XmlElement(name = "SYSID")
    /**
     * setSysId方法。
     *      * @param sysId String类型参数
     */
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    /**
     * getAuthCode方法。
     *
     * @return String类型返回值
     */
    public String getAuthCode() {
        return authCode;
    }

    @XmlElement(name = "AUTHCODE")
    /**
     * setAuthCode方法。
     *      * @param authCode String类型参数
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * getReqNo方法。
     *
     * @return String类型返回值
     */
    public String getReqNo() {
        return reqNo;
    }

    @XmlElement(name = "REQNO")
    /**
     * setReqNo方法。
     *      * @param reqNo String类型参数
     */
    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }


}