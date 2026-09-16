package com.zifang.util.core.lang;

import org.junit.Test;

import javax.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * XmlUtil工具类中 JAXB 序列化相关方法的单元测试
 */
public class XmlUtilTest {

    /**
     * JAXB 序列化测试用 Bean
     */
    @XmlRootElement(name = "person")
    public static class Person {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    /**
     * testBeanToXml方法：JavaBean 序列化为格式化 XML 字符串。
     */
    @Test
    public void testBeanToXml() {
        Person person = new Person();
        person.setName("alice");
        person.setAge(30);
        String xml = XmlUtil.beanToXml(person);

        assertTrue(xml.contains("<person"));
        assertTrue(xml.contains("<name>alice</name>"));
        assertTrue(xml.contains("<age>30</age>"));
    }

    /**
     * testBeanToXmlWithNull方法：null 对象返回 null。
     */
    @Test
    public void testBeanToXmlWithNull() {
        assertNull(XmlUtil.beanToXml(null));
    }

    /**
     * testXmlToBean方法：XML 字符串反序列化为对象。
     */
    @Test
    public void testXmlToBean() {
        Person person = XmlUtil.xmlToBean("<person><name>bob</name><age>18</age></person>", Person.class);

        assertEquals("bob", person.getName());
        assertEquals(Integer.valueOf(18), person.getAge());
    }

    /**
     * testXmlToBeanRoundTrip方法：序列化与反序列化往返一致（含中文）。
     */
    @Test
    public void testXmlToBeanRoundTrip() {
        Person person = new Person();
        person.setName("中文");
        person.setAge(1);
        String xml = XmlUtil.beanToXml(person);
        Person parsed = XmlUtil.xmlToBean(xml, Person.class);

        assertEquals("中文", parsed.getName());
        assertEquals(Integer.valueOf(1), parsed.getAge());
    }

    /**
     * testXmlToBeanWithNull方法：xmlStr 或 clazz 为 null 时返回 null。
     */
    @Test
    public void testXmlToBeanWithNull() {
        assertNull(XmlUtil.xmlToBean(null, Person.class));
        assertNull(XmlUtil.xmlToBean("<person/>", null));
    }

    /**
     * testXmlToBeanWithBadXml方法：非法 XML 抛出 RuntimeException。
     */
    @Test(expected = RuntimeException.class)
    public void testXmlToBeanWithBadXml() {
        XmlUtil.xmlToBean("<person><name>bob</age></person>", Person.class);
    }
}
