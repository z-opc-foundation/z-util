package com.zifang.util.core.parser.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML 与 Java Bean 转换工具类。
 * <p>
 * 基于 JAXB 实现 Bean 到 XML 字符串的 marshal，以及 XML 字符串到 Bean 的 unmarshal。
 *
 * @author zifang
 * @see JAXBContext
 */
public class XmlBeanUtil {

    private static final Logger log = LoggerFactory.getLogger(XmlBeanUtil.class);

    /**
     * beanToXml方法。
     * * @param obj Object类型参数
     *
     * @param load Class?类型参数
     * @return static String类型返回值
     */
    public static String beanToXml(Object obj, Class<?> load) {
        String xmlStr = null;
        try {
            JAXBContext context = JAXBContext.newInstance(load);
            Marshaller marshaller = context.createMarshaller();
            // 去掉生成xml的默认报文头
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            xmlStr = writer.toString();
        } catch (JAXBException e) {
            log.error("实体转为xml时出错!", e);
        }
        return xmlStr;
    }

    /**
     * xmlToBean方法。
     * * @param str String类型参数
     *
     * @param load ClassT类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T xmlToBean(String str, Class<T> load) {
        Object object = null;
        try {
            JAXBContext context = JAXBContext.newInstance(load);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            object = unmarshaller.unmarshal(new StringReader(str));
        } catch (JAXBException e) {
            log.error("xml转换到实体时出错!", e);
        }
        return (T) object;

    }
}