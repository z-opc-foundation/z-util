package com.zifang.util.ioc;

import com.zifang.util.ioc.context.ClassPathApplicationContext;
import com.zifang.util.ioc.exception.NoSuchBeanException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IOC 容器 JUnit 5 测试。
 */
public class IocContainerTest {

    static ClassPathApplicationContext ctx;

    @BeforeAll
    static void init() {
        ctx = new ClassPathApplicationContext("com.zifang.util.ioc");
        ctx.registerBeanClass(OrderService.class);
        ctx.registerBeanClass(UserService.class);
        ctx.init();
    }

    @AfterAll
    static void close() {
        ctx.close();
    }

    @Test
    void getBean_byType() {
        OrderService os = ctx.getBean(OrderService.class);
        assertNotNull(os);
        assertEquals("OrderService", os.name);
    }

    @Test
    void getBean_byName() {
        UserService us = (UserService) ctx.getBean("userService");
        assertNotNull(us);
        assertEquals("UserService", us.name);
    }

    @Test
    void containsBean() {
        assertTrue(ctx.containsBean("orderService"));
        assertTrue(ctx.containsBean("userService"));
        assertFalse(ctx.containsBean("notExist"));
    }

    @Test
    void noSuchBeanException() {
        assertThrows(NoSuchBeanException.class, () -> ctx.getBean("notExist"));
    }

    @Test
    void singletonReturnsSameInstance() {
        OrderService os1 = ctx.getBean(OrderService.class);
        OrderService os2 = ctx.getBean(OrderService.class);
        assertSame(os1, os2);
    }
}