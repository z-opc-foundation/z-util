package com.zifang.util.xml.binding;

import com.zifang.util.xml.XmlUtil;
import com.zifang.util.xml.model.XDocument;
import com.zifang.util.xml.util.XmlFormatter;

import java.io.Writer;

/**
 * XML Bean 绑定公共 API（自研实现，覆盖全部 JAXB 能力，无需 jaxb-api 依赖）。
 * <p>
 * 使用方式：
 * <pre>
 *   // 序列化（Bean → XML）
 *   String xml = XmlBinding.toXml(smsDeliverReq);
 *   String prettyXml = XmlBinding.toXml(smsDeliverReq, true);
 *
 *   // 反序列化（XML → Bean）
 *   SmsDeliverReq req = XmlBinding.fromXml(xml, SmsDeliverReq.class);
 * </pre>
 * <p>
 * 支持的 JAXB 注解（通过反射读取，无需 jaxb-api 编译依赖）：
 * <ul>
 *   <li>{@code @XmlRootElement(name=...)}</li>
 *   <li>{@code @XmlElement(name=...)}</li>
 *   <li>{@code @XmlElementWrapper(name=...)}</li>
 *   <li>{@code @XmlType(propOrder=...)}</li>
 *   <li>{@code @XmlAttribute(name=...)}</li>
 * </ul>
 *
 * @author zifang
 * @see XmlBeanUtil z-util-core 中原有的 JAXB 版本
 */
public final class XmlBinding {

    private XmlBinding() {
    }

    /**
     * 将 Java Bean 序列化为 XML 字符串（紧凑格式）。
     *
     * @param obj Bean 实例
     * @return XML 字符串（根元素为 {@code @XmlRootElement.name()} 或类名）
     * @throws XmlBindingException 序列化失败
     */
    public static String toXml(Object obj) {
        return toXml(obj, false);
    }

    /**
     * 将 Java Bean 序列化为 XML 字符串。
     *
     * @param obj       Bean 实例
     * @param formatted true：2 空格缩进；false：紧凑
     * @return XML 字符串
     * @throws XmlBindingException 序列化失败
     */
    public static String toXml(Object obj, boolean formatted) {
        XDocument doc = new XmlBinder().marshal(obj);
        try {
            XmlFormatter fmt = formatted ? new XmlFormatter(2) : new XmlFormatter(0);
            // format(XElement) 不加 XML declaration，与 JAXB fragment=true 行为一致
            return fmt.format(doc.getRoot());
        } catch (Exception e) {
            throw new XmlBindingException("XML 序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 Java Bean 序列化并写入到 Writer。
     *
     * @param obj    Bean 实例
     * @param writer 输出目标
     * @throws XmlBindingException 序列化失败
     */
    public static void toXml(Object obj, Writer writer) {
        try {
            writer.write(toXml(obj, true));
        } catch (java.io.IOException e) {
            throw new XmlBindingException("写入 XML 到 Writer 失败", e);
        }
    }

    /**
     * 将 XML 字符串反序列化为 Java Bean。
     *
     * @param xml   XML 字符串
     * @param clazz 目标 Bean 类（必须有无参构造器）
     * @param <T>   目标类型
     * @return Bean 实例
     * @throws XmlBindingException 反序列化失败
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        try {
            // SAX 快速路径：绕过 G4 词法分析，直接构建 XDocument（大 XML 场景快 ~25x）
            XDocument doc = SaxXDocumentBuilder.parse(xml);
            return new XmlBinder().unmarshal(doc, clazz);
        } catch (Exception e) {
            throw new XmlBindingException("XML 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 XML 字符串反序列化为 Bean，使用模板实例确定目标类型。
     *
     * @param xml      XML 字符串
     * @param template 模板实例（仅用于确定类型）
     * @param <T>      目标类型
     * @return Bean 实例
     * @throws XmlBindingException 反序列化失败
     */
    public static <T> T fromXml(String xml, T template) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) template.getClass();
        return fromXml(xml, clazz);
    }
}
