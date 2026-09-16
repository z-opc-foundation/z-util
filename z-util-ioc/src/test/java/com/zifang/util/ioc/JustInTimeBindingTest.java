package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Just-In-Time Binding 测试。
 * <p>
 * 容器对未显式绑定但有具体实现（且非抽象）的类，自动创建 singleton。
 */
class JustInTimeBindingTest {

    @Test
    void jitBindingResolvesConcreteClass() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UsesJit.class);
                // NoBindNeeded is NOT explicitly bound, JIT should kick in
            }
        });
        UsesJit user = injector.getInstance(UsesJit.class);
        assertNotNull(user.autoInjected);
        assertEquals("jit-created", user.autoInjected.value);
    }

    @Test
    void jitBindingWorksForPrototype() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UsesPrototypeJit.class);
                bind(NotSingleton.class).in(Scopes.PROTOTYPE);
            }
        });
        UsesPrototypeJit user = injector.getInstance(UsesPrototypeJit.class);
        assertEquals("jit-prototype", user.autoInjected.value);
    }

    @Test
    void jitRegisteredBeanIsSingletonByDefault() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        NoBindNeeded a = injector.getInstance(NoBindNeeded.class);
        NoBindNeeded b = injector.getInstance(NoBindNeeded.class);
        assertSame(a, b);
    }

    @Test
    void jitCannotResolveInterface() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        assertThrows(RuntimeException.class, () -> injector.getInstance(OnlyInterface.class));
    }

    @Test
    void jitCannotResolveAbstractClass() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        assertThrows(RuntimeException.class, () -> injector.getInstance(AbstractClass.class));
    }

    public interface OnlyInterface {
    }

    @Singleton
    public static class NoBindNeeded {
        public String value = "jit-created";
    }

    public static class NotSingleton {
        public String value = "jit-prototype";
    }

    public static class UsesJit {
        @Inject
        public NoBindNeeded autoInjected;
    }

    public static class UsesPrototypeJit {
        @Inject
        public NotSingleton autoInjected;
    }

    public abstract static class AbstractClass {
        public abstract String name();
    }
}
