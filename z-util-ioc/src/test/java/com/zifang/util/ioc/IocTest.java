package com.zifang.util.ioc;

import com.zifang.util.ioc.context.ClassPathApplicationContext;
import com.zifang.util.ioc.exception.NoSuchBeanException;

/**
 * IOC 容器功能测试。
 */
public class IocTest {

    public static void main(String[] args) {
        ClassPathApplicationContext ctx = new ClassPathApplicationContext("com.zifang.util.ioc");

        ctx.registerBeanClass(OrderService.class);
        ctx.registerBeanClass(UserService.class);

        ctx.init();

        OrderService os = ctx.getBean(OrderService.class);
        System.out.println("OrderService: " + os.name);

        UserService us = ctx.getBean(UserService.class);
        System.out.println("UserService: " + us.name);

        System.out.println("containsBean(orderService): " + ctx.containsBean("orderService"));
        System.out.println("containsBean(userService): " + ctx.containsBean("userService"));

        try {
            ctx.getBean("notExist");
        } catch (NoSuchBeanException e) {
            System.out.println("NoSuchBeanException caught OK");
        }

        ctx.close();
        System.out.println("IOC test PASSED");
    }
}