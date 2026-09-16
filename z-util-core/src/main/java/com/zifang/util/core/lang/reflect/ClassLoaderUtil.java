package com.zifang.util.core.lang.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * ClassLoader工具类 提供ClassLoader相关的操作方法
 */
public class ClassLoaderUtil {

    private static final Logger log = LoggerFactory.getLogger(ClassLoaderUtil.class);

    /**
     * 可覆盖的ClassLoader，用于优先使用指定的ClassLoader加载类
     */
    public static ClassLoader overrideClassLoader;

    /**
     * 获取上下文ClassLoader，如果存在覆盖的ClassLoader则使用覆盖的，否则使用线程上下文ClassLoader
     *
     * @return ClassLoader实例
     */
    public static ClassLoader getContextClassLoader() {
        return overrideClassLoader != null ? overrideClassLoader : Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载指定的类
     */
    public static Class<?> loadClass(String className) {
        Class<?> theClass = null;
        try {
            theClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error:" + e.getMessage());
            e.printStackTrace();
        }
        return theClass;
    }

    /**
     * 获取指定ClassLoader已加载的所有类列表
     *
     * @param classLoader ClassLoader实例
     * @return 已加载的类列表
     * @throws NoSuchFieldException   如果找不到classes字段
     * @throws IllegalAccessException 如果无法访问classes字段
     */
    public static List<Class> getLoaderClass(ClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        Class cla = classLoader.getClass();
        while (cla != ClassLoader.class) {
            cla = cla.getSuperclass();
        }
        Field field = cla.getDeclaredField("classes");
        field.setAccessible(true);
        Vector v = (Vector) field.get(classLoader);
        List<Class> result = new ArrayList<>();
        for (Object o : v) {
            result.add((Class) o);
        }
        return result;
    }
}