package com.zifang.util.core.parser.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * SMS 短信下发请求XML结构。
 * <p>
 * 对应 SMSDELIVERREQ 根节点，包含请求头和短信内容列表。
 *
 * @author zifang
 * @see ReqHeader
 * @see SmsBody
 */
@XmlRootElement(name = "SMSDELIVERREQ")
/**
 * SmsDeliverReq类。
 */
public class SmsDeliverReq {

    private ReqHeader reqHeader;

    private List<SmsBody> smsBodys;

    @XmlElement(name = "REQHEADER")
    /**
     * getReqHeader方法。
     * @return ReqHeader类型返回值
     */
    public ReqHeader getReqHeader() {
        return reqHeader;
    }

    /**
     * setReqHeader方法。
     * * @param reqHeader ReqHeader类型参数
     */
    public void setReqHeader(ReqHeader reqHeader) {
        this.reqHeader = reqHeader;
    }

    @XmlElementWrapper(name = "SMSBODYS")
    @XmlElement(name = "SMSBODY")
    /**
     * getSmsBodys方法。
     * @return List<SmsBody>类型返回值
     */
    public List<SmsBody> getSmsBodys() {
        return smsBodys;
    }

    /**
     * setSmsBodys方法。
     * * @param smsBodys ListSmsBody类型参数
     */
    public void setSmsBodys(List<SmsBody> smsBodys) {
        this.smsBodys = smsBodys;
    }
}