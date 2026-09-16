package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Child Injector 测试，对标 Guice 的 {@code injector.createChildInjector()}。
 */
class ChildInjectorTest {

    @Test
    void childInheritsParentBindings() {
        Injector parent = Injector.createInjector(new ParentModule());
        Injector child = parent.createChildInjector(new ChildOverrideModule());

        // parent still uses default
        assertEquals("default", parent.getInstance(Holder.class).svc.name());
        // child overrides
        assertEquals("override", child.getInstance(Holder.class).svc.name());
    }

    @Test
    void childCanAddNewBindings() {
        Injector parent = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        });
        Injector child = parent.createChildInjector(new ChildNewModule());
        // child uses child-only
        assertEquals("child-only", child.getInstance(Holder.class).svc.name());
        // parent does not have IService binding
        assertThrows(RuntimeException.class, () -> parent.getInstance(Holder.class));
    }

    @Test
    void parentAndChildHaveSeparateSingletons() {
        Counter.CTORS = 0;
        Injector parent = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Counter.class);
            }
        });
        Injector child = parent.createChildInjector();
        Counter p = parent.getInstance(Counter.class);
        Counter c = child.getInstance(Counter.class);
        assertNotNull(p);
        assertNotNull(c);
        assertNotSame(p, c);
        assertEquals(2, Counter.CTORS);
    }

    public interface IService {
        String name();
    }

    public static class DefaultImpl implements IService {
        @Override
        public String name() {
            return "default";
        }
    }

    public static class OverrideImpl implements IService {
        @Override
        public String name() {
            return "override";
        }
    }

    public static class ChildOnlyImpl implements IService {
        @Override
        public String name() {
            return "child-only";
        }
    }

    public static class Holder {
        @Inject
        public IService svc;
    }

    public static class ParentModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(DefaultImpl.class);
            bind(Holder.class);
        }
    }

    public static class ChildOverrideModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(OverrideImpl.class);
        }
    }

    public static class ChildNewModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ChildOnlyImpl.class);
        }
    }

    @Singleton
    public static class Counter {
        public static int CTORS = 0;

        public Counter() {
            CTORS++;
        }
    }
}
