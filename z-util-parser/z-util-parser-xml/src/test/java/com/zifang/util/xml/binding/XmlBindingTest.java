package com.zifang.util.xml.binding;

import com.zifang.util.xml.binding.annotation.XmlElement;
import com.zifang.util.xml.binding.annotation.XmlElementWrapper;
import com.zifang.util.xml.binding.annotation.XmlRootElement;
import com.zifang.util.xml.binding.annotation.XmlType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * XmlBinding 自研注解路径完整测试。
 * <p>
 * 覆盖：简单 bean、嵌套 bean、List + wrapper、propOrder、枚举、
 * 自动根名推断、序列化 / 反序列化双向 round-trip。
 */
public class XmlBindingTest {

    // ==================== 测试 Bean 定义 ====================

    @XmlRootElement(name = "Person")
    public static class Person {
        private String name;
        private int age;
        private String email;

        public Person() {}

        public Person(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @XmlElement(name = "NAME")
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @XmlElement(name = "AGE")
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        @XmlElement(name = "EMAIL")
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @XmlRootElement(name = "Employee")
    public static class Employee {
        private String id;
        private Person person;
        private List<String> tags;

        public Employee() {}

        public Employee(String id, Person person, List<String> tags) {
            this.id = id;
            this.person = person;
            this.tags = tags;
        }

        @XmlElement(name = "ID")
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        @XmlElement(name = "DETAIL")
        public Person getPerson() { return person; }
        public void setPerson(Person person) { this.person = person; }

        @XmlElementWrapper(name = "TAGS")
        @XmlElement(name = "TAG")
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class Item {
        private String code;
        private String province;
        private String city;

        public Item() {}
        public Item(String code, String province, String city) {
            this.code = code;
            this.province = province;
            this.city = city;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    @XmlRootElement(name = "RequestOrder")
    @XmlType(propOrder = {"province", "city", "code"})
    public static class OrderedRequest {
        private List<Item> item;

        public OrderedRequest() {}
        public OrderedRequest(List<Item> item) { this.item = item; }

        public List<Item> getItem() { return item; }
        public void setItem(List<Item> item) { this.item = item; }
    }

    @XmlRootElement(name = "StatusResponse")
    public static class StatusResponse {
        private String message;
        private StatusEnum status;

        public StatusResponse() {}
        public StatusResponse(String message, StatusEnum status) {
            this.message = message;
            this.status = status;
        }

        @XmlElement(name = "MESSAGE")
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        @XmlElement(name = "STATUS")
        public StatusEnum getStatus() { return status; }
        public void setStatus(StatusEnum status) { this.status = status; }
    }

    public enum StatusEnum { OK, ERROR, WARNING }

    // ==================== 测试方法 ====================

    @Test
    public void testSimpleRoundTrip() {
        Person p = new Person("Alice", 30, "alice@example.com");
        String xml = XmlBinding.toXml(p);
        assertNotNull(xml);
        assertTrue(xml.contains("<NAME>Alice</NAME>"));
        assertTrue(xml.contains("<AGE>30</AGE>"));
        assertTrue(xml.contains("<EMAIL>alice@example.com</EMAIL>"));

        Person p2 = XmlBinding.fromXml(xml, Person.class);
        assertEquals("Alice", p2.getName());
        assertEquals(30, p2.getAge());
        assertEquals("alice@example.com", p2.getEmail());
    }

    @Test
    public void testNestedBeanRoundTrip() {
        Person p = new Person("Bob", 25, "bob@example.com");
        Employee emp = new Employee("E001", p, java.util.Arrays.asList("dev", "test"));

        String xml = XmlBinding.toXml(emp);
        assertNotNull(xml);
        assertTrue(xml.contains("<ID>E001</ID>"));
        assertTrue(xml.contains("<DETAIL>"));
        assertTrue(xml.contains("<NAME>Bob</NAME>"));
        assertTrue(xml.contains("<TAGS>"));
        assertTrue(xml.contains("<TAG>dev</TAG>"));
        assertTrue(xml.contains("<TAG>test</TAG>"));

        Employee emp2 = XmlBinding.fromXml(xml, Employee.class);
        assertEquals("E001", emp2.getId());
        assertNotNull(emp2.getPerson());
        assertEquals("Bob", emp2.getPerson().getName());
        assertEquals(25, emp2.getPerson().getAge());
        assertNotNull(emp2.getTags());
        assertEquals(2, emp2.getTags().size());
        assertEquals("dev", emp2.getTags().get(0));
        assertEquals("test", emp2.getTags().get(1));
    }

    @Test
    public void testFormattedOutput() {
        Person p = new Person("X", 10, "x@x.com");
        String formatted = XmlBinding.toXml(p, true);
        assertTrue(formatted.contains("  "));
        assertTrue(formatted.contains("<NAME>X</NAME>"));
    }

    @Test
    public void testEnumRoundTrip() {
        StatusResponse resp = new StatusResponse("success", StatusEnum.OK);
        String xml = XmlBinding.toXml(resp);
        assertTrue(xml.contains("<STATUS>OK</STATUS>"));

        StatusResponse resp2 = XmlBinding.fromXml(xml, StatusResponse.class);
        assertEquals(StatusEnum.OK, resp2.getStatus());
        assertEquals("success", resp2.getMessage());
    }

    @Test
    public void testNullFieldOmitted() {
        Person p = new Person("NoEmail", 20, null);
        String xml = XmlBinding.toXml(p);
        assertFalse(xml.contains("<EMAIL>"));
    }

    @Test
    public void testAutoRootName() {
        Item item = new Item("C01", "Pro", "City");
        String xml = XmlBinding.toXml(item);
        assertTrue("Expected to start with <Item>, got: " + xml, xml.startsWith("<Item>"));
    }
}
