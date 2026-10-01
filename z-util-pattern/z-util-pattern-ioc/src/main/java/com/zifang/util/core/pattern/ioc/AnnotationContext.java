package com.zifang.util.core.pattern.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置上下文。
 * <p>
 * 用于基于注解的 IOC 容器上下文管理。
 *
 * @author zifang
 */
public class AnnotationContext {

    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    /**
     * 扫描指定包下的类并注册到容器中。
     *
     * @param packageName 包名
     */
    public void scan(String packageName) {
        // Stub implementation - in real use would scan classpath
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
        return (T) beans.get(clazz);
    }

    /**
     * 获取指定类型的所有bean实例。
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 实例列表
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
    }
}