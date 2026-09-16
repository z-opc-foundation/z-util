package com.zifang.util.xml.binding;

import com.zifang.util.core.parser.xml.ReqHeader;
import com.zifang.util.core.parser.xml.SmsBody;
import com.zifang.util.core.parser.xml.SmsDeliverReq;
import com.zifang.util.xml.binding.annotation.XmlElement;
import com.zifang.util.xml.binding.annotation.XmlElementWrapper;
import com.zifang.util.xml.binding.annotation.XmlRootElement;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * XmlBinding 性能基准测试。
 * <p>
 * 对比自研 XmlBinding 与标准 JAXB 的序列化/反序列化耗时，
 * 验证自研方案在实际业务场景下的性能表现。
 */
public class XmlBindingPerformanceTest {

    // ==================== 测试数据构造 ====================

    /**
     * 构造一个典型的 SmsDeliverReq（2条短信）
     */
    private SmsDeliverReq buildTypicalSms() {
        ReqHeader header = new ReqHeader();
        header.setSysId("SYSTEM001");
        header.setAuthCode("AUTH123");
        header.setReqNo("REQ456");

        SmsBody body1 = new SmsBody();
        body1.setContent("你好，这是一条测试短信内容，包含中文和English混合文本");
        body1.setSourceAddr("13800138000");
        body1.setDestAddr("10086");

        SmsBody body2 = new SmsBody();
        body2.setContent("第二条短信：The quick brown fox jumps over the lazy dog 0123456789");
        body2.setSourceAddr("13900139000");
        body2.setDestAddr("10010");

        SmsDeliverReq req = new SmsDeliverReq();
        req.setReqHeader(header);
        List<SmsBody> bodies = new ArrayList<>();
        bodies.add(body1);
        bodies.add(body2);
        req.setSmsBodys(bodies);
        return req;
    }

    /**
     * 构造大批量数据（100条短信）
     */
    private SmsDeliverReq buildLargeSms() {
        ReqHeader header = new ReqHeader();
        header.setSysId("BATCH_SYSTEM");
        header.setAuthCode("BATCH_AUTH");
        header.setReqNo("BATCH_001");

        SmsDeliverReq req = new SmsDeliverReq();
        req.setReqHeader(header);
        List<SmsBody> bodies = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            SmsBody body = new SmsBody();
            body.setContent("短信内容 #" + i + "：这是一段较长的测试文本，用于验证大数据量下的序列化性能表现。"
                    + "The quick brown fox jumps over the lazy dog. 0123456789!@#$%^&*()");
            body.setSourceAddr("138" + String.format("%08d", i));
            body.setDestAddr("100" + String.format("%06d", i));
            bodies.add(body);
        }
        req.setSmsBodys(bodies);
        return req;
    }

    /**
     * 构造简单 bean（仅用于自研方案测试，JAXB 对比使用 SmsDeliverReq）
     */
    @XmlRootElement(name = "SimpleItem")
    public static class SimpleItem {
        private String name;
        private int value;
        private boolean active;

        public SimpleItem() {}
        public SimpleItem(String name, int value, boolean active) {
            this.name = name;
            this.value = value;
            this.active = active;
        }

        @XmlElement(name = "NAME")
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @XmlElement(name = "VALUE")
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }

        @XmlElement(name = "ACTIVE")
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    @XmlRootElement(name = "ItemList")
    public static class ItemList {
        private List<SimpleItem> items;

        public ItemList() {}
        public ItemList(List<SimpleItem> items) { this.items = items; }

        @XmlElementWrapper(name = "ITEMS")
        @XmlElement(name = "ITEM")
        public List<SimpleItem> getItems() { return items; }
        public void setItems(List<SimpleItem> items) { this.items = items; }
    }

    // ==================== 性能测试 ====================

    /**
     * 性能基准：序列化性能对比
     */
    @Test
    public void testSerializationPerformance() throws Exception {
        SmsDeliverReq typical = buildTypicalSms();
        SmsDeliverReq large = buildLargeSms();

        // 预热
        for (int i = 0; i < 100; i++) {
            XmlBinding.toXml(typical);
            marshalWithJaxb(typical);
        }

        int warmup = 100;
        int iterations = 1000;

        // === 小对象序列化 ===
        // 自研方案
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.toXml(typical);
        }
        long selfSmall = System.nanoTime() - start;
        String selfSmallXml = XmlBinding.toXml(typical);

        // JAXB
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            marshalWithJaxb(typical);
        }
        long jaxbSmall = System.nanoTime() - start;
        String jaxbSmallXml = marshalWithJaxb(typical);

        // === 大对象序列化 ===
        // 自研方案
        start = System.nanoTime();
        for (int i = 0; i < warmup; i++) {
            XmlBinding.toXml(large);
        }
        long selfLargeStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.toXml(large);
        }
        long selfLarge = System.nanoTime() - selfLargeStart;

        // JAXB
        start = System.nanoTime();
        for (int i = 0; i < warmup; i++) {
            marshalWithJaxb(large);
        }
        long jaxbLargeStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            marshalWithJaxb(large);
        }
        long jaxbLarge = System.nanoTime() - jaxbLargeStart;

        // === 简单 bean 序列化（仅自研方案，因为 ItemList 使用自研注解，JAXB 不支持）===
        ItemList list = new ItemList();
        List<SimpleItem> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add(new SimpleItem("item_" + i, i, i % 2 == 0));
        }
        list.setItems(items);

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.toXml(list);
        }
        long selfSimple = System.nanoTime() - start;

        // 输出结果
        System.out.println("=== 序列化性能对比 (" + iterations + " 次) ===");
        printResult("小对象 SmsDeliverReq (2条)", selfSmall, jaxbSmall);
        printResult("大对象 SmsDeliverReq (100条)", selfLarge, jaxbLarge);
        System.out.printf("  %-30s 自研: %8.2fms (无JAXB对照，自研注解) %n", "嵌套列表 ItemList (50项)", selfSimple / 1_000_000.0);

        // 功能性验证
        assertTrue("输出应包含根元素", selfSmallXml.contains("<SMSDELIVERREQ>"));
        assertTrue("输出应包含头部", selfSmallXml.contains("<REQHEADER>"));
        assertTrue("输出应包含短信列表", selfSmallXml.contains("<SMSBODYS>"));
    }

    /**
     * 性能基准：反序列化性能对比
     */
    @Test
    public void testDeserializationPerformance() throws Exception {
        SmsDeliverReq typical = buildTypicalSms();
        SmsDeliverReq large = buildLargeSms();

        String typicalXml = marshalWithJaxb(typical);
        String largeXml = marshalWithJaxb(large);

        ItemList list = new ItemList();
        List<SimpleItem> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add(new SimpleItem("item_" + i, i, i % 2 == 0));
        }
        list.setItems(items);
        String listXml = XmlBinding.toXml(list);

        // 预热
        for (int i = 0; i < 100; i++) {
            XmlBinding.fromXml(typicalXml, SmsDeliverReq.class);
            unmarshalWithJaxb(typicalXml, SmsDeliverReq.class);
        }

        int iterations = 1000;
        int warmup = 100;

        // === 小对象反序列化 ===
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.fromXml(typicalXml, SmsDeliverReq.class);
        }
        long selfSmall = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            unmarshalWithJaxb(typicalXml, SmsDeliverReq.class);
        }
        long jaxbSmall = System.nanoTime() - start;

        // === 大对象反序列化 ===
        start = System.nanoTime();
        for (int i = 0; i < warmup; i++) {
            XmlBinding.fromXml(largeXml, SmsDeliverReq.class);
        }
        long selfLargeStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.fromXml(largeXml, SmsDeliverReq.class);
        }
        long selfLarge = System.nanoTime() - selfLargeStart;

        start = System.nanoTime();
        for (int i = 0; i < warmup; i++) {
            unmarshalWithJaxb(largeXml, SmsDeliverReq.class);
        }
        long jaxbLargeStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            unmarshalWithJaxb(largeXml, SmsDeliverReq.class);
        }
        long jaxbLarge = System.nanoTime() - jaxbLargeStart;

        // === 嵌套列表反序列化（仅自研方案）===
        start = System.nanoTime();
        for (int i = 0; i < warmup; i++) {
            XmlBinding.fromXml(listXml, ItemList.class);
        }
        long selfListStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XmlBinding.fromXml(listXml, ItemList.class);
        }
        long selfList = System.nanoTime() - selfListStart;

        System.out.println("\n=== 反序列化性能对比 (" + iterations + " 次) ===");
        printResult("小对象 SmsDeliverReq (2条)", selfSmall, jaxbSmall);
        printResult("大对象 SmsDeliverReq (100条)", selfLarge, jaxbLarge);
        System.out.printf("  %-30s 自研: %8.2fms%n", "嵌套列表 ItemList (50项, 仅自研)", selfList / 1_000_000.0);

        // 功能验证
        SmsDeliverReq parsed = XmlBinding.fromXml(typicalXml, SmsDeliverReq.class);
        assertNotNull(parsed);
        assertEquals("SYSTEM001", parsed.getReqHeader().getSysId());
        assertEquals(2, parsed.getSmsBodys().size());
    }

    /**
     * Round-trip 一致性验证
     */
    @Test
    public void testRoundTripConsistency() throws Exception {
        SmsDeliverReq req = buildTypicalSms();

        // 自研方案 round-trip
        String selfXml = XmlBinding.toXml(req);
        SmsDeliverReq selfParsed = XmlBinding.fromXml(selfXml, SmsDeliverReq.class);

        // 验证字段一致
        assertNotNull(selfParsed);
        assertNotNull(selfParsed.getReqHeader());
        assertEquals(req.getReqHeader().getSysId(), selfParsed.getReqHeader().getSysId());
        assertEquals(req.getReqHeader().getAuthCode(), selfParsed.getReqHeader().getAuthCode());
        assertEquals(req.getReqHeader().getReqNo(), selfParsed.getReqHeader().getReqNo());
        assertNotNull(selfParsed.getSmsBodys());
        assertEquals(req.getSmsBodys().size(), selfParsed.getSmsBodys().size());
        for (int i = 0; i < req.getSmsBodys().size(); i++) {
            assertEquals(req.getSmsBodys().get(i).getContent(),
                    selfParsed.getSmsBodys().get(i).getContent());
            assertEquals(req.getSmsBodys().get(i).getSourceAddr(),
                    selfParsed.getSmsBodys().get(i).getSourceAddr());
            assertEquals(req.getSmsBodys().get(i).getDestAddr(),
                    selfParsed.getSmsBodys().get(i).getDestAddr());
        }

        System.out.println("\n=== Round-trip 一致性 ===");
        System.out.println("自研方案 XML 长度: " + selfXml.length() + " bytes");
        System.out.println("所有字段完全一致: PASS");
    }

    // ==================== JAXB 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private <T> String marshalWithJaxb(T obj) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            marshaller.marshal(obj, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("JAXB marshal failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T unmarshalWithJaxb(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new java.io.StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException("JAXB unmarshal failed", e);
        }
    }

    private void printResult(String label, long selfNanos, long jaxbNanos) {
        double selfMs = selfNanos / 1_000_000.0;
        double jaxbMs = jaxbNanos / 1_000_000.0;
        double ratio = (double) selfNanos / jaxbNanos;
        String faster = selfNanos <= jaxbNanos ? "自研更快" : "JAXB更快";
        double speedup = selfNanos <= jaxbNanos
                ? (1.0 / ratio - 1.0) * 100
                : (ratio - 1.0) * 100;

        System.out.printf("  %-30s 自研: %8.2fms | JAXB: %8.2fms | 比值: %.2fx | %s %.1f%%%n",
                label, selfMs, jaxbMs, ratio, faster, speedup);
    }
}
