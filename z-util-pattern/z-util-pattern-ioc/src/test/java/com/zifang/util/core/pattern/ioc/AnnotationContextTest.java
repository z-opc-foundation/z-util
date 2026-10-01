package com.zifang.util.core.pattern.ioc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 烟雾: AnnotationContext.register + getBean 必须能透传引用.
 * scan() 是 stub (设计如此), 故只测 register/getBean 这一对.
 */
class AnnotationContextTest {

    @Test
    @DisplayName("register + getBean 必须返同一实例")
    void registerThenGetReturnsSameInstance() {
        AnnotationContext ctx = new AnnotationContext();
        Object bean = new Object();
        ctx.register(Object.class, bean);
        assertSame(bean, ctx.getBean(Object.class),
                "register 进去的对象必须原样取出, 不允许拷贝");
    }

    @Test
    @DisplayName("未注册的类 getBean 必须返回 null (而不是抛异常)")
    void getBeanMissingReturnsNull() {
        AnnotationContext ctx = new AnnotationContext();
        assertSame(null, ctx.getBean(String.class),
                "未注册类型应返 null —— 调用方大多用 if (bean != null) 链, 抛异常会断链");
    }
}