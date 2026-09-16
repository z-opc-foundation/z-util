package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Provider 注入测试，对标 Guice 的 {@code Provider<T>}。
 */
class ProviderTest {

    @Test
    void providerIsLazy() {
        AtomicInteger instances = new AtomicInteger();
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IService.class).to(ServiceImpl.class);
                bind(LazyService.class);
            }
        });
        LazyService lazy = injector.getInstance(LazyService.class);
        // Provider not yet called
        assertNotNull(lazy.provider);
        IService svc = lazy.getOrCreate();
        assertEquals("service", svc.name());
    }

    @Test
    void providerWithQualifier() {
        Injector injector = Injector.createInjector(new QualifiedModule());
        QualifiedProviderHolder holder = injector.getInstance(QualifiedProviderHolder.class);
        assertEquals("service", holder.defaultProvider.get().name());
        assertEquals("backup", holder.backupProvider.get().name());
    }

    @Test
    void injectorGetProviderReturnsProvider() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IService.class).to(ServiceImpl.class);
            }
        });
        Provider<IService> p = injector.getProvider(IService.class);
        assertNotNull(p);
        assertEquals("service", p.get().name());
    }

    @Test
    void providerCanBeCalledMultipleTimes() {
        StatefulProvider.CALLS.set(0);
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toProvider(StatefulProvider.class);
                bind(UsesStatefulProvider.class);
            }
        });
        UsesStatefulProvider holder = injector.getInstance(UsesStatefulProvider.class);
        assertEquals("value-1", holder.provider.get());
        assertEquals("value-2", holder.provider.get());
        assertEquals("value-3", holder.provider.get());
    }

    @Qualifier
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Primary {
    }

    public interface IService {
        String name();
    }

    public static class ServiceImpl implements IService {
        @Override
        public String name() {
            return "service";
        }
    }

    public static class LazyService {
        private final Provider<IService> provider;
        public volatile IService resolved;

        @Inject
        public LazyService(Provider<IService> provider) {
            this.provider = provider;
        }

        public IService getOrCreate() {
            if (resolved == null) {
                resolved = provider.get();
            }
            return resolved;
        }
    }

    public static class LazyModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ServiceImpl.class);
            bind(LazyService.class);
        }
    }

    public static class QualifiedProviderHolder {
        public final Provider<IService> defaultProvider;
        public final Provider<IService> backupProvider;

        @Inject
        public QualifiedProviderHolder(
                Provider<IService> defaultProvider,
                @Primary Provider<IService> backupProvider) {
            this.defaultProvider = defaultProvider;
            this.backupProvider = backupProvider;
        }
    }

    public static class BackupImpl implements IService {
        @Override
        public String name() {
            return "backup";
        }
    }

    public static class QualifiedModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IService.class).to(ServiceImpl.class);
            bind(IService.class, Primary.class).to(BackupImpl.class);
            bind(QualifiedProviderHolder.class);
        }
    }

    public static class StatefulProvider implements Provider<String> {
        public static final AtomicInteger CALLS = new AtomicInteger();

        @Override
        public String get() {
            return "value-" + CALLS.incrementAndGet();
        }
    }

    public static class UsesStatefulProvider {
        @Inject
        public Provider<String> provider;
    }
}
