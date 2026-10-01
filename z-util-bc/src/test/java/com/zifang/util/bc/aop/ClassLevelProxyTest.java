package com.zifang.util.bc.aop;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * z-util-aop 类级别字节码代理端到端集成：不需要目标实现任何接口，
 * 通过 {@link ClassLevelProxy} 走 z-util-bridge 的 SourceProxyFactory
 * 把 Advise 链织进子类字节码。
 */
public class ClassLevelProxyTest {

    public static class ConcreteGreeter {
        public String hello(String name) {
            return "hi " + name;
        }

        public int add(int a, int b) {
            return a + b;
        }
    }

    public static class LoggingAdvise implements Advise<ConcreteGreeter> {
        public static final List<String> LOG = new ArrayList<>();

        @Override
        public Object around(ConcreteGreeter target, Method method, Object[] args, Advise.Chain chain) {
            LOG.add("before " + method.getName());
            Object ret;
            try {
                ret = chain.proceed();
            } catch (Throwable t) {
                LOG.add("threw " + t.getClass().getSimpleName());
                throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
            }
            LOG.add("after " + method.getName() + "=" + ret);
            return ret;
        }
    }

    @Test
    public void classLevelProxy_invokesAdviseAndBusiness() {
        LoggingAdvise.LOG.clear();
        ConcreteGreeter target = new ConcreteGreeter();
        ConcreteGreeter proxy = ClassLevelProxy.wrap(target, LoggingAdvise.class);

        assertEquals("hi zifang", proxy.hello("zifang"));
        assertEquals(5, proxy.add(2, 3));

        assertEquals(4, LoggingAdvise.LOG.size());
        assertTrue(LoggingAdvise.LOG.get(0).startsWith("before hello"));
        assertTrue(LoggingAdvise.LOG.get(1).startsWith("after hello"));
        assertTrue(LoggingAdvise.LOG.get(2).startsWith("before add"));
        assertTrue(LoggingAdvise.LOG.get(3).startsWith("after add"));
    }
}