package com.zifang.util.ioc.inject;

import com.zifang.util.ioc.annotation.Bean;
import com.zifang.util.ioc.annotation.Component;
import com.zifang.util.ioc.annotation.Configuration;
import com.zifang.util.ioc.core.BeanRegistry;
import com.zifang.util.ioc.exception.BeanCreationException;
import com.zifang.util.ioc.exception.ProvisionException;
import com.zifang.util.ioc.metadata.BeanDefinition;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

/**
 * 兼容旧 API：根据 BeanDefinition 实例化对象。
 *
 * <p>支持：
 * <ul>
 *   <li>普通类：{@link Component} 标记类 → 构造函数实例化</li>
 *   <li>配置类：{@link Configuration} 标记类 → 直接返回类本身</li>
 *   <li>{@link Bean} 方法 → 调用工厂方法获取实例</li>
 * </ul>
 *
 * @deprecated 使用 {@link com.zifang.util.ioc.Injector} 替代，新代码不再需要此类的 instantiate。
 */
@Deprecated
public class Instantiator {

    private final BeanRegistry registry;

    public Instantiator(BeanRegistry registry) {
        this.registry = registry;
    }

    public Object instantiate(BeanDefinition bd, InjectorContext context) {
        try {
            if (bd.isConfiguration()) {
                return bd.getBeanClass().getDeclaredConstructor().newInstance();
            }
            if (bd.getFactoryMethod() != null) {
                Class<?> configClass = bd.getBeanClass().getDeclaringClass();
                Object configInstance;
                try {
                    configInstance = configClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new BeanCreationException("Cannot instantiate config class: " + configClass.getName(), e);
                }
                injectFields(configInstance, context);
                Method m = bd.getFactoryMethod();
                m.setAccessible(true);
                return m.invoke(configInstance);
            }
            Object instance = bd.getBeanClass().getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create bean: " + bd.getName(), e);
        }
    }

    /**
     * 注入实例字段（供 resolveInstance 后调用）。
     */
    public void injectFields(Object instance, InjectorContext context) {
        FieldInjector injector = new FieldInjector(context);
        injector.inject(instance);
    }

    /**
     * 处理 @PostConstruct 回调（JSR 250 标准）。
     */
    public void invokePostConstruct(Object instance) {
        invokeLifecycleCallback(instance, PostConstruct.class);
    }

    /**
     * 处理 @PreDestroy 回调（JSR 250 标准）。
     */
    public void invokePreDestroy(Object instance) {
        invokeLifecycleCallback(instance, PreDestroy.class);
    }

    private void invokeLifecycleCallback(Object instance, Class<? extends java.lang.annotation.Annotation> annoClass) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(annoClass) && m.getParameterCount() == 0) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                } catch (Exception e) {
                    throw new ProvisionException("Lifecycle callback failed: " + m.getName(), e);
                }
            }
        }
    }
}
