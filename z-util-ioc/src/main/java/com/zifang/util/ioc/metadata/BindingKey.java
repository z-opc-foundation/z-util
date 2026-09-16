package com.zifang.util.ioc.metadata;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 类型 + 限定符 复合键，对标 Guice 的 {@code Key}。
 * <p>
 * 通过该 Key 在注册表中唯一定位一个绑定。
 */
public class BindingKey<T> {

    private final Class<T> type;
    private final String name;          // 用于按名称查找
    private final Class<? extends Annotation> qualifier; // 用于 @Named / 自定义 @Qualifier

    private BindingKey(Class<T> type, String name, Class<? extends Annotation> qualifier) {
        this.type = Objects.requireNonNull(type, "type");
        this.name = name;
        this.qualifier = qualifier;
    }

    public static <T> BindingKey<T> of(Class<T> type) {
        return new BindingKey<>(type, null, null);
    }

    public static <T> BindingKey<T> of(Class<T> type, String name) {
        if (name == null || name.isEmpty()) {
            return of(type);
        }
        return new BindingKey<>(type, name, null);
    }

    public static <T> BindingKey<T> of(Class<T> type, Class<? extends Annotation> qualifier) {
        if (qualifier == null) {
            return of(type);
        }
        return new BindingKey<>(type, null, qualifier);
    }

    public static <T> BindingKey<T> of(Class<T> type, Class<? extends Annotation> qualifier, String qualifierValue) {
        // qualifierValue 暂未单独使用，扩展点保留
        return of(type, qualifier);
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BindingKey)) return false;
        BindingKey<?> that = (BindingKey<?>) o;
        return type.equals(that.type)
                && Objects.equals(name, that.name)
                && Objects.equals(qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, qualifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(type.getName());
        if (qualifier != null) {
            sb.append('@').append(qualifier.getSimpleName());
        }
        if (name != null) {
            sb.append("[\"").append(name).append("\"]");
        }
        return sb.toString();
    }

    /**
     * 提供创建/获取 Bean 实例的统一接口，注入端会调用 {@link #get()}。
     */
    public interface ProviderLookup<T> {
        T get();
    }

    /**
     * 内部持有 javax.inject.Provider，供模块系统使用。
     */
    public interface ProviderHolder<T> {
        Provider<T> provider();
    }
}
