package com.zifang.util.aop;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * 自研 JDK Proxy AOP 测试（不依赖 aspectj/cglib/spring）。
 */
public class ProxyFactoryTest {

    @Test
    public void testIntercept_basicCall() throws Throwable {
        LogAdvise.LOG.clear();
        Greeter g = ProxyFactory.wrap(new GreeterImpl());
        String r = g.hello("bob");
        assertEquals("hi bob", r);
        // 链跑了 2 次 log
        assertEquals(2, LogAdvise.LOG.size());
        assertTrue(LogAdvise.LOG.get(0).contains("before"));
        assertTrue(LogAdvise.LOG.get(1).contains("after"));
    }

    @Test
    public void testUnannotatedMethod_passesThrough() {
        Greeter g = ProxyFactory.wrap(new GreeterImpl());
        // defaultMethod 没 @Intercept，应直接透传，不进 advise
        assertEquals("default", g.defaultMethod());
    }

    @Test
    public void testAdviseCanChangeReturn() {
        Greeter g = ProxyFactory.wrap(new UppercaseGreeter());
        assertEquals("HI BOB", g.hello("bob"));
    }

    @Test
    public void testAdviseCanSwallowException() {
        Greeter g = ProxyFactory.wrap(new SwallowGreeter());
        // 异常被 advice 吞掉，返回 fallback
        assertEquals("fallback", g.hello("x"));
    }

    @Test
    public void testExceptionPropagates() {
        Greeter g = ProxyFactory.wrap(new TransparentGreeter());
        try {
            g.hello("x");
            fail("expected exception");
        } catch (RuntimeException e) {
            assertEquals("expected", e.getMessage());
        }
    }

    @Test
    public void testClassWithoutInterface_rejected() {
        try {
            ProxyFactory.wrap(new Object());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* ok */ }
    }

    /**
     * 测试用接口与目标类
     */
    public interface Greeter {
        String hello(String name);

        default String defaultMethod() {
            return "default";
        }
    }

    /**
     * 第一个 advise：日志记录。
     */
    public static class LogAdvise implements Advise<Greeter> {
        static final java.util.List<String> LOG = new java.util.ArrayList<>();

        @Override
        public Object around(Greeter target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            LOG.add("before " + method.getName());
            Object ret = chain.proceed();
            LOG.add("after " + method.getName());
            return ret;
        }
    }

    public static class GreeterImpl implements Greeter {
        @Override
        @Intercept(LogAdvise.class)
        public String hello(String name) {
            return "hi " + name;
        }

        @Override
        public String defaultMethod() {
            return "default";
        }
    }

    public static class UppercaseAdvise implements Advise<Greeter> {
        @Override
        public Object around(Greeter target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            Object r = chain.proceed();
            return r == null ? null : r.toString().toUpperCase();
        }
    }

    public static class UppercaseGreeter extends GreeterImpl {
        @Override
        @Intercept(UppercaseAdvise.class)
        public String hello(String name) {
            return "hi " + name;
        }
    }

    public static class SwallowAdvise implements Advise<Greeter> {
        @Override
        public Object around(Greeter target, Method method, Object[] args, Advise.Chain chain) {
            try {
                return chain.proceed();
            } catch (Throwable t) {
                return "fallback";
            }
        }
    }

    public static class SwallowGreeter extends GreeterImpl {
        @Override
        @Intercept(SwallowAdvise.class)
        public String hello(String name) {
            throw new IllegalStateException("boom");
        }
    }

    public static class TransparentAdvise implements Advise<Greeter> {
        @Override
        public Object around(Greeter target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            return chain.proceed();
        }
    }

    public static class TransparentGreeter extends GreeterImpl {
        @Override
        @Intercept(TransparentAdvise.class)
        public String hello(String name) {
            throw new IllegalArgumentException("expected");
        }
    }
}
