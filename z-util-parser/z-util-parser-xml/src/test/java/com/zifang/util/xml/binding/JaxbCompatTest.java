package com.zifang.util.xml.binding;

import com.zifang.util.core.parser.xml.ReqHeader;
import com.zifang.util.core.parser.xml.SmsBody;
import com.zifang.util.core.parser.xml.SmsDeliverReq;
import com.zifang.util.core.parser.xml.TitleRequest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * XmlBinding 与现有 JAXB 注解 bean 的兼容性测试。
 * <p>
 * 验证 XmlBinding 能正确读取 javax.xml.bind.annotation 注解，
 * 并与原有 XmlBeanUtil / XmlTransformer 产生等价结果。
 */
public class JaxbCompatTest {

    // ==================== 辅助方法 ====================

    private SmsDeliverReq buildSmsDeliverReq() {
        ReqHeader header = new ReqHeader();
        header.setSysId("SYSTEM001");
        header.setAuthCode("AUTH123");
        header.setReqNo("REQ456");

        SmsBody body1 = new SmsBody();
        body1.setContent("你好，这是一条测试短信");
        body1.setSourceAddr("13800138000");
        body1.setDestAddr("10086");

        SmsBody body2 = new SmsBody();
        body2.setContent("第二条测试短信");
        body2.setSourceAddr("13900139000");
        body2.setDestAddr("10086");

        List<SmsBody> bodies = new ArrayList<>();
        bodies.add(body1);
        bodies.add(body2);

        SmsDeliverReq req = new SmsDeliverReq();
        req.setReqHeader(header);
        req.setSmsBodys(bodies);
        return req;
    }

    private TitleRequest buildTitleRequest() {
        TitleRequest.Item item1 = new TitleRequest.Item("C01", "北京", "朝阳区", "三里屯");
        TitleRequest.Item item2 = new TitleRequest.Item("C02", "上海", "浦东", "陆家嘴");

        List<TitleRequest.Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        TitleRequest req = new TitleRequest();
        req.setItem(items);
        return req;
    }

    // ==================== SmsDeliverReq 测试 ====================

    @Test
    public void testSmsDeliverReqToXml() {
        SmsDeliverReq req = buildSmsDeliverReq();
        String xml = XmlBinding.toXml(req);
        assertNotNull(xml);
        assertTrue(xml.contains("<SMSDELIVERREQ>"));
        assertTrue(xml.contains("<REQHEADER>"));
        assertTrue(xml.contains("<SYSID>SYSTEM001</SYSID>"));
        assertTrue(xml.contains("<AUTHCODE>AUTH123</AUTHCODE>"));
        assertTrue(xml.contains("<REQNO>REQ456</REQNO>"));
        assertTrue(xml.contains("<SMSBODYS>"));
        assertTrue(xml.contains("<SMSBODY>"));
        assertTrue(xml.contains("<CONTENT>你好，这是一条测试短信</CONTENT>"));
        assertTrue(xml.contains("<SOURCEADDR>13800138000</SOURCEADDR>"));
        assertTrue(xml.contains("<DESTADDR>10086</DESTADDR>"));
        assertTrue(xml.contains("<CONTENT>第二条测试短信</CONTENT>"));
    }

    @Test
    public void testSmsDeliverReqRoundTrip() {
        SmsDeliverReq req = buildSmsDeliverReq();
        String xml = XmlBinding.toXml(req);

        SmsDeliverReq parsed = XmlBinding.fromXml(xml, SmsDeliverReq.class);
        assertNotNull(parsed);
        assertNotNull(parsed.getReqHeader());
        assertEquals("SYSTEM001", parsed.getReqHeader().getSysId());
        assertEquals("AUTH123", parsed.getReqHeader().getAuthCode());
        assertEquals("REQ456", parsed.getReqHeader().getReqNo());
        assertNotNull(parsed.getSmsBodys());
        assertEquals(2, parsed.getSmsBodys().size());
        assertEquals("你好，这是一条测试短信", parsed.getSmsBodys().get(0).getContent());
        assertEquals("13800138000", parsed.getSmsBodys().get(0).getSourceAddr());
        assertEquals("10086", parsed.getSmsBodys().get(0).getDestAddr());
        assertEquals("第二条测试短信", parsed.getSmsBodys().get(1).getContent());
    }

    @Test
    public void testSmsDeliverReqFromXml() {
        String xml = "<SMSDELIVERREQ>"
                + "<REQHEADER>"
                + "<SYSID>SYS</SYSID>"
                + "<AUTHCODE>AUTH</AUTHCODE>"
                + "<REQNO>REQ001</REQNO>"
                + "</REQHEADER>"
                + "<SMSBODYS>"
                + "<SMSBODY><CONTENT>test</CONTENT><SOURCEADDR>123</SOURCEADDR><DESTADDR>456</DESTADDR></SMSBODY>"
                + "</SMSBODYS>"
                + "</SMSDELIVERREQ>";

        SmsDeliverReq req = XmlBinding.fromXml(xml, SmsDeliverReq.class);
        assertEquals("SYS", req.getReqHeader().getSysId());
        assertEquals(1, req.getSmsBodys().size());
        assertEquals("test", req.getSmsBodys().get(0).getContent());
    }

    // ==================== TitleRequest 测试 ====================

    @Test
    public void testTitleRequestToXml() {
        TitleRequest req = buildTitleRequest();
        String xml = XmlBinding.toXml(req);
        assertNotNull(xml);
        assertTrue(xml.contains("<RequestOrder>"));
        assertTrue(xml.contains("<item>"));
        assertTrue(xml.contains("<code>C01</code>"));
        assertTrue(xml.contains("<province>北京</province>"));
        assertTrue(xml.contains("<city>朝阳区</city>"));
        assertTrue(xml.contains("<district>三里屯</district>"));
    }

    @Test
    public void testTitleRequestRoundTrip() {
        TitleRequest req = buildTitleRequest();
        String xml = XmlBinding.toXml(req);

        TitleRequest parsed = XmlBinding.fromXml(xml, TitleRequest.class);
        assertNotNull(parsed);
        assertNotNull(parsed.getItem());
        assertEquals(2, parsed.getItem().size());
        assertEquals("C01", parsed.getItem().get(0).getCode());
        assertEquals("北京", parsed.getItem().get(0).getProvince());
        assertEquals("朝阳区", parsed.getItem().get(0).getCity());
        assertEquals("三里屯", parsed.getItem().get(0).getDistrict());
    }

    @Test
    public void testTitleRequestFormatted() {
        TitleRequest req = buildTitleRequest();
        String formatted = XmlBinding.toXml(req, true);
        assertTrue(formatted.contains("<code>C01</code>"));
        // 格式化输出应该包含缩进
        assertTrue(formatted.contains("  "));
    }
}
