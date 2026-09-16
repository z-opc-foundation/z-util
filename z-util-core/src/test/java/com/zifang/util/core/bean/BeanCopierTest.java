package com.zifang.util.core.bean;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 自研属性拷贝测试。
 */
public class BeanCopierTest {

    @Test
    public void testCopy_sameFields() {
        User u = new User();
        u.setName("alice");
        u.setAge(30);
        u.setEmail("a@x.com");
        UserDto dto = BeanCopier.copy(u, UserDto.class);
        assertEquals("alice", dto.name);
        assertEquals(30, dto.age);
        assertNull("city 不在 src 中", dto.city);
    }

    @Test
    public void testCopyInto_existing() {
        User u = new User();
        u.setName("bob");
        u.setAge(25);
        UserDto dto = new UserDto();
        dto.city = "BJ";
        BeanCopier.copyInto(u, dto);
        assertEquals("bob", dto.name);
        assertEquals(25, dto.age);
        assertEquals("BJ", dto.city);   // 不被覆盖
    }

    @Test
    public void testFromMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "carol");
        m.put("age", 40);
        UserDto dto = BeanCopier.fromMap(m, UserDto.class);
        assertEquals("carol", dto.name);
        assertEquals(40, dto.age);
    }

    @Test
    public void testToMap() {
        User u = new User();
        u.setName("dan");
        u.setAge(50);
        u.setEmail("d@x.com");
        Map<String, Object> m = BeanCopier.toMap(u);
        assertEquals("dan", m.get("name"));
        assertEquals(50, m.get("age"));
        assertEquals("d@x.com", m.get("email"));
    }

    @Test
    public void testNullSource() {
        assertNull(BeanCopier.copy(null, UserDto.class));
        assertNotNull(BeanCopier.toMap(null));
    }

    public static class User {
        private String name;
        private int age;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int a) {
            this.age = a;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String e) {
            this.email = e;
        }
    }

    public static class UserDto {
        private String name;
        private int age;
        private String city;
    }
}
