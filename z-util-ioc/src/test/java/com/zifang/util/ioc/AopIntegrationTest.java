package com.zifang.util.ioc;

import com.zifang.util.aop.Advise;
import com.zifang.util.ioc.aop.AopModule;
import com.zifang.util.ioc.aop.ClassMatcher;
import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * IoC + AOP 集成测试：容器在创建 Bean 后自动应用 advise 链。
 */
class AopIntegrationTest {

    @Test
    void aopProxyIsAppliedAutomatically() {
        RecordingAdvise.LOG.clear();
        Injector injector = Injector.createInjector(
                new ServiceModule(),
                new AopModule()
                        .bindInterceptor(ClassMatcher.subclassOf(ServiceImpl.class), RecordingAdvise.class)
        );
        IService svc = injector.getInstance(IService.class);
        assertEquals("hello bob!", svc.hello("bob"));
        // default + intercept twice (before + after)
        assertEquals(2, RecordingAdvise.LOG.size());
        assertEquals("before hello", RecordingAdvise.LOG.get(0));
        assertEquals("after hello", RecordingAdvise.LOG.get(1));
    }

    @Test
    void methodLevelMatchWorks() {
        OnlyHelloAdvise.LOG.clear();
        Injector injector = Injector.createInjector(
                new ServiceModule(),
                new AopModule()
                        .bindInterceptor(
                                ClassMatcher.subclassOf(ServiceImpl.class),
                                com.zifang.util.ioc.aop.MethodMatcher.named("hello"),
                                OnlyHelloAdvise.class)
        );
        IService svc = injector.getInstance(IService.class);
        assertEquals("hello bob", svc.hello("bob"));
        assertEquals("other", svc.otherMethod());
        assertEquals(1, OnlyHelloAdvise.LOG.size());
    }

    @Test
    void nonInterfaceBeanIsNotProxied() {
        // Bean 没有实现接口时，JDK Proxy 不可用，应跳过代理
        com.zifang.util.ioc.aop.AopModule aop = new com.zifang.util.ioc.aop.AopModule()
                .bindInterceptor(ClassMatcher.any(), RecordingAdvise.class);
        Injector injector = Injector.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PlainBean.class);
                    }
                },
                aop
        );
        PlainBean bean = injector.getInstance(PlainBean.class);
        // 因为 PlainBean 没有接口，AOP 不会代理
        assertEquals("plain", bean.value());
    }

    @Test
    void adviseCanSwallowExceptions() {
        Injector injector = Injector.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IService.class).to(BrokenService.class);
                    }
                },
                new AopModule()
                        .bindInterceptor(
                                ClassMatcher.subclassOf(BrokenService.class),
                                SwallowExceptionAdvise.class)
        );
        IService svc = injector.getInstance(IService.class);
        assertEquals("fallback", svc.hello("x"));
    }

    public interface IService {
        String hello(String name);

        String otherMethod();
    }

    public static class ServiceImpl implements IService {
        @Override
        public String hello(String name) {
            return "hello " + name;
        }

        @Override
        public String otherMethod() {
            return "other";
        }
    }

    /**
     * 测试 advise：记录所有调用，附加 "!"。
     */
    public static class RecordingAdvise implements Advise<IService> {
        public static final List<String> LOG = new ArrayList<>();

        @Override
        public Object around(IService target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            LOG.add("before " + method.getName());
            Object r = chain.proceed();
            LOG.add("after " + method.getName());
            return r + "!";
        }
    }

    public static class ServiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ServiceImpl.class);
        }
    }

    /**
     * 仅对 hello 方法生效的 advise。
     */
    public static class OnlyHelloAdvise implements Advise<IService> {
        public static final List<String> LOG = new ArrayList<>();

        @Override
        public Object around(IService target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            LOG.add("[" + method.getName() + "]");
            return chain.proceed();
        }
    }

    public static class PlainBean {
        public String value() {
            return "plain";
        }
    }

    public static class BrokenService implements IService {
        @Override
        public String hello(String name) {
            throw new IllegalStateException("boom");
        }

        @Override
        public String otherMethod() {
            return "other";
        }
    }

    public static class SwallowExceptionAdvise implements Advise<IService> {
        @Override
        public Object around(IService target, Method method, Object[] args, Advise.Chain chain) {
            try {
                return chain.proceed();
            } catch (Throwable t) {
                return "fallback";
            }
        }
    }
}
