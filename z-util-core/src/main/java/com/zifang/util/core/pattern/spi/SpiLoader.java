package com.zifang.util.core.pattern.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI (Service Provider Interface) loader for dynamically loading implementations.
 * <p>
 * Uses Java's ServiceLoader mechanism to load implementations from META-INF/services/.
 * Supports plugin-style extensibility with interface-implementation decoupling.
 *
 * @param <T> the SPI interface type to load
 * @author zifang
 */
public class SpiLoader<T> {

    private static final String META_INF_SERVICES = "META-INF/services/";
    private final Class<T> clazz;

    private SpiLoader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get an SPI loader instance for the specified interface.
     *
     * @param clazz SPI interface class, must not be null
     * @param <T>   SPI interface type
     * @return SPI loader instance
     * @throws IllegalArgumentException if clazz is null
     */
    public static <T> SpiLoader<T> getSpiLoader(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("SPI interface type cannot be null");
        }
        return new SpiLoader<>(clazz);
    }

    /**
     * Load the first available implementation of the SPI interface.
     *
     * @return the first implementation instance, or null if none found
     */
    public T get() {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        for (T instance : loader) {
            return instance;
        }
        return null;
    }

    /**
     * Load all available implementations of the SPI interface.
     *
     * @return list of all implementation instances, never null
     */
    public List<T> getAll() {
        List<T> result = new ArrayList<>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        for (T instance : loader) {
            result.add(instance);
        }
        return result;
    }

    /**
     * Load implementation by class name (uses SPI config file directly).
     *
     * @param className fully qualified implementation class name
     * @return implementation instance, or null if loading fails
     */
    @SuppressWarnings("unchecked")
    /**
     * get方法。
     *      * @param className String类型参数
     * @return T类型返回值
     */
    public T get(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        try {
            Class<?> implClass = Class.forName(className);
            if (!clazz.isAssignableFrom(implClass)) {
                throw new IllegalArgumentException(
                        "Class " + className + " does not implement " + clazz.getName());
            }
            return (T) implClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get class loader for SPI loading.
     */
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    /**
     * Get the SPI interface class.
     */
    public Class<T> getClazz() {
        return clazz;
    }
}