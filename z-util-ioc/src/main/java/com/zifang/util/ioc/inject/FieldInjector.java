package com.zifang.util.ioc.inject;

import com.zifang.util.ioc.Injector;
import com.zifang.util.ioc.exception.ProvisionException;
import com.zifang.util.ioc.provider.InternalProvider;

import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 * 字段级别的依赖注入器。
 * <p>
 * 遍历所有被 {@link Inject} 标记的字段（JSR 330 标准），从容器中查找依赖并写入。
 * <p>
 * 支持：
 * <ul>
 *   <li>基本类型注入</li>
 *   <li>{@code Provider<T>} 字段（延迟解析）</li>
 *   <li>{@code @Named} / 自定义 {@code @Qualifier} 多绑定</li>
 * </ul>
 */
public class FieldInjector {

    private final Injector injector;
    private final InjectorContext legacyContext;

    public FieldInjector(Injector injector) {
        this.injector = injector;
        this.legacyContext = null;
    }

    /**
     * 兼容旧 API：从 {@link InjectorContext} 构造。
     * <p>
     * 警告：仅支持按类型解析，不支持限定符；建议使用 {@link #FieldInjector(Injector)}。
     */
    public FieldInjector(InjectorContext context) {
        this.legacyContext = context;
        this.injector = null;
    }

    /**
     * 向 target 实例注入所有被 {@link Inject} 标记的字段。
     */
    public void inject(Object target) {
        Class<?> clazz = target.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                injectField(target, field);
            }
        }
    }

    private void injectField(Object target, Field field) {
        if (legacyContext != null) {
            // 旧 API 路径：按类型解析
            @SuppressWarnings("unchecked")
            Class<Object> type = (Class<Object>) field.getType();
            Object value = legacyContext.resolve(type);
            if (value == null) {
                throw new ProvisionException("Cannot resolve dependency for field '" + field.getName()
                        + "' of type " + type.getName()
                        + " in " + target.getClass().getName());
            }
            setField(target, field, value);
            return;
        }
        // 新 API 路径
        InjectionPoint ip = InjectionPoint.forField(field);
        Object value;
        if (ip.isProvider()) {
            value = new InternalProvider<>(injector, ip.getRequiredType(), ip.getQualifier());
        } else {
            value = injector.getInstance(ip.getRequiredType(), ip.getQualifier());
            if (value == null) {
                throw new ProvisionException("Cannot resolve dependency for field '" + field.getName()
                        + "' of type " + ip.getRequiredType().getName()
                        + " in " + target.getClass().getName());
            }
        }
        setField(target, field, value);
    }

    private void setField(Object target, Field field, Object value) {
        boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new ProvisionException("Failed to inject field: " + field, e);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }
}
