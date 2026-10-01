package com.zifang.util.core.pattern.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置应用上下文。
 * <p>
 * 提供基于注解配置的 IOC 容器实现。
 *
 * @author zifang
 */
public class AnnotationConfigApplicationContext {

    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    private final Map<Class<?>, Class<?>> beanTypes = new ConcurrentHashMap<>();

    /**
     * 扫描指定包下的类并注册到容器中。
     *
     * @param basePackage 基础包名
     */
    public void scan(String basePackage) {
        // Stub implementation - in real use would scan classpath
        // For now, this is a placeholder
    }

    /**
     * 注册类到容器中。
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 实例
     */
    @SuppressWarnings("unchecked")
    /**
     * register方法。
     *      * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    public <T> T register(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            beans.put(clazz, instance);
            beanTypes.put(clazz, clazz);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register bean: " + clazz.getName(), e);
        }
    }

    /**
     * 注册实例到容器中。
     *
     * @param instance 实例
     * @param <T>      类型
     */
    public <T> void register(T instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) instance.getClass();
        beans.put(clazz, instance);
        beanTypes.put(clazz, clazz);
    }

    /**
     * 获取指定类型的bean实例。
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 实例
     */
    @SuppressWarnings("unchecked")
    /**
     * getBean方法。
     *      * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    public <T> T getBean(Class<T> clazz) {
        T bean = (T) beans.get(clazz);
        if (bean == null) {
            Class<?> actualType = beanTypes.get(clazz);
            if (actualType != null) {
                bean = (T) beans.get(actualType);
            }
        }
        return bean;
    }

    /**
     * 获取指定类型的所有bean实例。
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 实例集合
     */
    public <T> List<T> getBeansOfType(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                result.add(clazz.cast(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * 清除所有注册的bean。
     */
    public void clear() {
        beans.clear();
        beanTypes.clear();
    }
}