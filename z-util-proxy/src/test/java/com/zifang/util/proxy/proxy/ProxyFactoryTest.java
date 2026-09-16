package com.zifang.util.proxy.proxy;

import com.zifang.util.proxy.aspects.Aspect;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * ProxyFactoryTest类。
 */
public class ProxyFactoryTest {

    @Test
    /**
     * testCreateReturnsFactory方法。
     */
    public void testCreateReturnsFactory() {
        ProxyFactory factory = ProxyFactory.create();
        assertNotNull(factory);
    }

    @Test
    /**
     * testCreateProxyWithAspectInstance方法。
     */
    public void testCreateProxyWithAspectInstance() {
        TestService target = new TestServiceImpl();
        CountingAspect aspect = new CountingAspect();

        TestService proxy = ProxyFactory.createProxy(target, aspect);
        assertNotNull(proxy);

        String result = proxy.sayHello();
        assertEquals("Hello", result);
        assertTrue(aspect.isBeforeCalled());
        assertTrue(aspect.isAfterCalled());
    }

    @Test
    /**
     * testCreateProxyWithAspectClass方法。
     */
    public void testCreateProxyWithAspectClass() {
        TestService target = new TestServiceImpl();

        TestService proxy = ProxyFactory.createProxy(target, SimpleTestAspect.class);
        assertNotNull(proxy);

        String result = proxy.sayHello();
        assertEquals("Hello", result);
    }

    @Test
    /**
     * testJdkProxyFactoryProxy方法。
     */
    public void testJdkProxyFactoryProxy() {
        TestService target = new TestServiceImpl();
        CountingAspect aspect = new CountingAspect();

        JdkProxyFactory factory = new JdkProxyFactory();
        TestService proxy = factory.proxy(target, aspect);
        assertNotNull(proxy);

        String result = proxy.sayHello();
        assertEquals("Hello", result);
    }

    @Test
    /**
     * testCglibProxyFactoryProxy方法。
     */
    public void testCglibProxyFactoryProxy() {
        TestService target = new TestServiceImpl();
        CountingAspect aspect = new CountingAspect();

        CglibProxyFactory factory = new CglibProxyFactory();
        TestService proxy = factory.proxy(target, aspect);
        assertNotNull(proxy);

        String result = proxy.sayHello();
        assertEquals("Hello", result);
    }

    /**
     * TestService接口。
     */
    public interface TestService {
        String sayHello();
    }

    public static class TestServiceImpl implements TestService {
        @Override
        /**
         * sayHello方法。
         * @return String类型返回值
         */
        public String sayHello() {
            return "Hello";
        }
    }

    public static class CountingAspect implements Aspect {
        private boolean beforeCalled = false;
        private boolean afterCalled = false;

        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            beforeCalled = true;
            return true;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            afterCalled = true;
            return true;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return true;
        }

        /**
         * isBeforeCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isBeforeCalled() {
            return beforeCalled;
        }

        /**
         * isAfterCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isAfterCalled() {
            return afterCalled;
        }
    }

    public static class SimpleTestAspect implements Aspect {
        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            return true;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            return true;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return true;
        }
    }
}
