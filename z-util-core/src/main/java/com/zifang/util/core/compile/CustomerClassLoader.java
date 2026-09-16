package com.zifang.util.core.compile;

/**
 * 自定义类加载器。
 * <p>
 * 支持从字节数组加载并定义类。
 *
 * @author zifang
 * @see ClassLoader
 */
public class CustomerClassLoader extends ClassLoader {

    /**
     * CustomerClassLoader方法。
     * * @param parent ClassLoader类型参数
     */
    public CustomerClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * defineClass方法。
     * * @param className String类型参数
     *
     * @param bytes byte[]类型参数
     * @return Class<?>类型返回值
     */
    public Class<?> defineClass(String className, byte[] bytes) {

        try {
            return super.defineClass(className, bytes, 0, bytes.length);
        } catch (Exception | LinkageError e) {
            e.printStackTrace();
        }
        return null;
    }
}
