package com.zifang.util.core;

import com.zifang.util.core.parser.xml.ReqHeader;
import com.zifang.util.core.parser.xml.SmsBody;
import com.zifang.util.core.parser.xml.SmsDeliverReq;
import com.zifang.util.core.parser.xml.XmlTransformer;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Test类。
 */
public class Test {

    /**
     * @param args
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        ReqHeader reqHeader = new ReqHeader();
        reqHeader.setReqNo("111");
        reqHeader.setAuthCode("DDDD");
        reqHeader.setSysId("jjj");

        SmsBody smsBody = new SmsBody();
        smsBody.setContent("类容讷讷");
        smsBody.setDestAddr("1589594");
        smsBody.setSourceAddr("9999");

        SmsBody smsBody1 = new SmsBody();
        smsBody1.setContent("asdf");
        smsBody1.setDestAddr("asdf");
        smsBody1.setSourceAddr("4fdf");


        SmsDeliverReq smsDeliverReq = new SmsDeliverReq();
        smsDeliverReq.setReqHeader(reqHeader);
        List<SmsBody> smsBodys = new ArrayList<SmsBody>();
        smsBodys.add(smsBody);
        smsBodys.add(smsBody1);
        smsDeliverReq.setSmsBodys(smsBodys);

        StringWriter sw = XmlTransformer.beanToXml(smsDeliverReq);
        System.out.println(sw.toString());

        SmsDeliverReq xmlToBean = XmlTransformer.xmlToBean(sw.toString(), smsDeliverReq);
        System.out.println(xmlToBean.toString());
    }
}
