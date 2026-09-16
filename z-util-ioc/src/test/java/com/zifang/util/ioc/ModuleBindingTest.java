package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Module + Binding DSL 测试，对标 Guice 的 {@code AbstractModule} 用法。
 */
class ModuleBindingTest {

    @Test
    void bindToAndResolve() {
        Injector injector = Injector.createInjector(new CountingModule());
        IService svc = injector.getInstance(IService.class);
        assertNotNull(svc);
        assertEquals("default", svc.name());
    }

    @Test
    void singletonScopeReturnsSameInstance() {
        Injector injector = Injector.createInjector(new CountingModule());
        IService a = injector.getInstance(IService.class);
        IService b = injector.getInstance(IService.class);
        assertSame(a, b);
    }

    @Test
    void bindToInstance() {
        CustomServiceImpl custom = new CustomServiceImpl();
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IService.class).toInstance(custom);
            }
        });
        IService svc = injector.getInstance(IService.class);
        assertSame(custom, svc);
    }

    @Test
    void prototypeScopeReturnsDifferentInstances() {
        Injector injector = Injector.createInjector(new PrototypeModule());
        IService a = injector.getInstance(IService.class);
        IService b = injector.getInstance(IService.class);
        assertNotSame(a, b);
    }

    @Test
    void annotatedSingletonResolves() {
        Injector injector = Injector.createInjector(new SingletonAnnotationModule());
        AnnotatedSingleton a = injector.getInstance(AnnotatedSingleton.class);
        AnnotatedSingleton b = injector.getInstance(AnnotatedSingleton.class);
        assertNotNull(a);
        assertSame(a, b);
    }

    @Test
    void eagerSingletonIsCreatedAtInjectorInit() {
        CountingService.CTOR_COUNT.set(0);
        Injector injector = Injector.createInjector(new EagerModule());
        assertEquals(1, CountingService.CTOR_COUNT.get(),
                "Eager singleton should have been created during Injector init");
        injector.getInstance(CountingService.class);
        assertEquals(1, CountingService.CTOR_COUNT.get(),
                "Should not re-create the eager singleton");
    }

    @Test
    void bindWithoutImpl_throws() {
        com.zifang.util.ioc.exception.BindingException ex = assertThrows(
                com.zifang.util.ioc.exception.BindingException.class,
                () -> Injector.createInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                        // 用 toInstance(null) 触发 BindingException
                        bind(IOptional.class).toInstance(null);
                    }
                }));
        // Module installation wraps the original; check both top-level and cause messages
        String msg = ex.getMessage();
        if (ex.getCause() != null) {
            msg += " | " + ex.getCause().getMessage();
        }
        assertTrue(msg.contains("null instance") || msg.contains("Module installation failed"));
    }

    public interface IService {
        String name();
    }

    public interface IOptional {
    }

    public static class ServiceImpl implements IService {
        @Override
        public String name() {
            return "default";
        }
    }

    public static class CustomServiceImpl implements IService {
        @Override
        public String name() {
            return "custom";
        }
    }

    public static class CountingModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ServiceImpl.class).in(Scopes.SINGLETON);
        }
    }

    public static class ToInstanceModule extends AbstractModule {
        @Override
        protected void configure() {
            IService svc = new CustomServiceImpl();
            bind(IService.class).toInstance(svc);
        }
    }

    public static class PrototypeModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ServiceImpl.class).in(Scopes.PROTOTYPE);
        }
    }

    @Singleton
    public static class AnnotatedSingleton {
    }

    public static class SingletonAnnotationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AnnotatedSingleton.class);
        }
    }

    public static class CountingService {
        static final AtomicInteger CTOR_COUNT = new AtomicInteger();

        public CountingService() {
            CTOR_COUNT.incrementAndGet();
        }
    }

    public static class EagerModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(CountingService.class).asEagerSingleton();
        }
    }
}
