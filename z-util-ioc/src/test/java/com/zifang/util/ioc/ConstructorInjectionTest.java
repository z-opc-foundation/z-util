package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 构造器注入测试，对标 Guice 的 {@code @Inject} 构造器策略。
 */
class ConstructorInjectionTest {

    @Test
    void constructorInjection() {
        Injector injector = Injector.createInjector(new CarModule());
        Car car = injector.getInstance(Car.class);
        assertNotNull(car);
        assertEquals("Car with V8", car.describe());
    }

    @Test
    void constructorInjectionWithQualifiedParameter() {
        Injector injector = Injector.createInjector(new MultiArgModule());
        MultiArgService svc = injector.getInstance(MultiArgService.class);
        assertEquals("V8/MyApp", svc.describe());
    }

    @Test
    void noArgClassCanBeInjected() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NoArgService.class);
            }
        });
        NoArgService svc = injector.getInstance(NoArgService.class);
        assertEquals("noarg", svc.value());
    }

    @Test
    void fallbackToPublicNoArgConstructor() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DefaultCtor.class);
            }
        });
        DefaultCtor svc = injector.getInstance(DefaultCtor.class);
        assertEquals("default-ctor", svc.value);
    }

    @Test
    void constructorWithoutUsableCtorFails() {
        Injector injector = Injector.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(RequiresCtorArg.class);
            }
        });
        // java.io.File 没有无参构造器，但被 @Inject 的字段需要一个无法解析的依赖
        assertThrows(RuntimeException.class, () -> injector.getInstance(RequiresCtorArg.class));
    }

    public interface Engine {
        String type();
    }

    public static class V8Engine implements Engine {
        @Override
        public String type() {
            return "V8";
        }
    }

    public static class Car {
        private final Engine engine;

        @Inject
        public Car(Engine engine) {
            this.engine = engine;
        }

        public String describe() {
            return "Car with " + engine.type();
        }
    }

    public static class CarModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Engine.class).to(V8Engine.class);
            bind(Car.class);
        }
    }

    public static class MultiArgService {
        private final Engine engine;
        private final String name;

        @Inject
        public MultiArgService(Engine engine, @Named("appName") String name) {
            this.engine = engine;
            this.name = name;
        }

        public String describe() {
            return engine.type() + "/" + name;
        }
    }

    public static class MultiArgModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Engine.class).to(V8Engine.class);
            bind(MultiArgService.class);
            bindConstant().annotatedWithNamed("appName").to("MyApp");
        }
    }

    @Singleton
    public static class NoArgService {
        public String value() {
            return "noarg";
        }
    }

    public static class DefaultCtor {
        public String value = "default-ctor";
    }

    public static class SingleCtorNoInject {
        private final String value;

        public SingleCtorNoInject(String value) {
            this.value = value;
        }
    }

    public static class SingleCtorOnlyCtor {
        public SingleCtorOnlyCtor() {
        }
    }

    public static class RequiresCtorArg {
        public RequiresCtorArg(java.io.File v) {
        }
    }
}
