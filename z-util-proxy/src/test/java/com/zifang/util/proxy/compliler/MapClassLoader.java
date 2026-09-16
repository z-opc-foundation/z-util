package com.zifang.util.proxy.compliler;

import java.util.Map;

/**
 * A class loader that loads classes from a map whose keys are class names and whose values are byte
 * code arrays.
 *
 * @author zifang Horstmann
 * @version 1.00 2007-11-02
 */

/**
 * MapClassLoader类。
 */
public class MapClassLoader extends ClassLoader {
    private Map<String, byte[]> classes;

    /**
     * MapClassLoader方法。
     * * @param classes MapString,类型参数
     */
    public MapClassLoader(Map<String, byte[]> classes) {
        this.classes = classes;
    }

    /**
     * findClass方法。
     * * @param name String类型参数
     *
     * @return Class<?>类型返回值
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classes.get(name);
        if (classBytes == null) throw new ClassNotFoundException(name);
        Class<?> cl = defineClass(name, classBytes, 0, classBytes.length);
        if (cl == null) throw new ClassNotFoundException(name);
        return cl;
    }
}
