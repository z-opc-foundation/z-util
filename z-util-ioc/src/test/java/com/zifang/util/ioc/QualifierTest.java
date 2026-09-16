package com.zifang.util.ioc;

import com.zifang.util.ioc.binder.AbstractModule;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Qualifier 多绑定测试，对标 Guice 的 {@code @Named} / 自定义 {@code @Qualifier}。
 */
class QualifierTest {

    @Test
    void qualifierPicksCorrectBinding() {
        Injector injector = Injector.createInjector(new PaymentModule());
        PaymentRouter router = injector.getInstance(PaymentRouter.class);
        assertNotNull(router.defaultService);
        assertEquals("alipay", router.defaultService.name());
        assertEquals("alipay", router.alipay.name());
        assertEquals("wechat", router.primary.name());
    }

    @Test
    void namedAnnotationSelectsCorrectBinding() {
        Injector injector = Injector.createInjector(new NamedClassModule());
        EngineConsumer consumer = injector.getInstance(EngineConsumer.class);
        assertEquals("default-engine", consumer.defaultEngine.type());
        assertEquals("fast-engine", consumer.fastEngine.type());
    }

    @javax.inject.Qualifier
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Primary {
    }

    public interface PaymentService {
        String name();
    }

    public interface Engine {
        String type();
    }

    public static class AlipayService implements PaymentService {
        @Override
        public String name() {
            return "alipay";
        }
    }

    public static class WechatService implements PaymentService {
        @Override
        public String name() {
            return "wechat";
        }
    }

    public static class PaymentRouter {
        @Inject
        public PaymentService defaultService;

        @Inject
        @Named("alipay")
        public PaymentService alipay;

        @Inject
        @Primary
        public PaymentService primary;
    }

    public static class PaymentModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PaymentService.class).to(AlipayService.class);
            bind(PaymentService.class, Named.class).to(AlipayService.class);
            bind(PaymentService.class, Primary.class).to(WechatService.class);
            bind(PaymentRouter.class);
        }
    }

    @Singleton
    public static class EngineImpl implements Engine {
        @Override
        public String type() {
            return "default-engine";
        }
    }

    @Named("fast")
    @Singleton
    public static class FastEngine implements Engine {
        @Override
        public String type() {
            return "fast-engine";
        }
    }

    public static class EngineConsumer {
        @Inject
        public Engine defaultEngine;

        @Inject
        @Named("fast")
        public Engine fastEngine;
    }

    public static class NamedClassModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Engine.class).to(EngineImpl.class);
            bind(Engine.class, Named.class).to(FastEngine.class);
            bind(EngineConsumer.class);
        }
    }
}
