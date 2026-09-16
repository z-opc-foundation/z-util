package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 循环依赖检测测试，对标 Guice 的 {@code ProvisionException}。
 */
class CircularDependencyTest {

    @Test
    void circularFieldInjectionDetected() {
        Injector injector = Injector.createInjector(new CircleModule());
        assertThrows(RuntimeException.class, () -> injector.getInstance(A.class));
    }

    @Test
    void longChainCircularDependencyDetected() {
        Injector injector = Injector.createInjector(new LongChainModule());
        assertThrows(RuntimeException.class, () -> injector.getInstance(C.class));
    }

    @Test
    void selfReferenceDetected() {
        Injector injector = Injector.createInjector(new SelfRefModule());
        assertThrows(RuntimeException.class, () -> injector.getInstance(SelfRef.class));
    }

    @Test
    void nonCircularDoesNotThrow() {
        Injector injector = Injector.createInjector(new OkModule());
        OkA a = injector.getInstance(OkA.class);
        assertNotNull(a);
        assertEquals("ok", a.b.name);
    }

    public static class A {
        @Inject
        public B b;
    }

    public static class B {
        @Inject
        public A a;
    }

    public static class CircleModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(A.class);
            bind(B.class);
        }
    }

    public static class C {
        @Inject
        public D d;
    }

    public static class D {
        @Inject
        public E e;
    }

    public static class E {
        @Inject
        public C c; // C -> D -> E -> C (环)
    }

    public static class LongChainModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(C.class);
            bind(D.class);
            bind(E.class);
        }
    }

    public static class SelfRef {
        @Inject
        public SelfRef self;
    }

    public static class SelfRefModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SelfRef.class);
        }
    }

    public static class OkA {
        @Inject
        public OkB b;
    }

    public static class OkB {
        public String name = "ok";
    }

    public static class OkModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(OkA.class);
            bind(OkB.class);
        }
    }
}
