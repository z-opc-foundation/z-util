package com.zifang.util.source.compiler;

import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义编译类加载器
 * <p>
 * 继承自ClassLoader，用于加载内存中编译生成的类。
 * 支持从CharSequenceJavaFileObject中获取字节码数据，
 * 配合CustomerCompileJavaFileManager实现纯内存的动态编译和加载。
 *
 * @author zifang
 * @version 1.0.0
 */
public class CustomerCompileClassLoader extends ClassLoader {

    /**
     * 类文件扩展名
     */
    public static final String CLASS_EXTENSION = JavaFileObject.Kind.CLASS.extension;

    /**
     * Java文件对象缓存表，键为全限定类名
     */
    private final Map<String, JavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();

    /**
     * 构造自定义类加载器
     *
     * @param parentClassLoader 父类加载器
     */
    public CustomerCompileClassLoader(ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    /**
     * 查找并加载类
     * <p>
     * 首先从缓存的JavaFileObject中查找已编译的字节码，
     * 如果找到则调用defineClass定义类，否则委托给父类加载器
     *
     * @param name 全限定类名
     * @return Class对象
     * @throws ClassNotFoundException 如果类未找到
     */
    @Override
    /**
     * findClass方法。
     *      * @param name String类型参数
     * @return Class<?>类型返回值
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        JavaFileObject javaFileObject = javaFileObjectMap.get(name);
        if (null != javaFileObject) {
            CharSequenceJavaFileObject charSequenceJavaFileObject = (CharSequenceJavaFileObject) javaFileObject;
            byte[] byteCode = charSequenceJavaFileObject.getByteCode();
            return defineClass(name, byteCode, 0, byteCode.length);
        }
        return super.findClass(name);
    }

    /**
     * 获取资源输入流
     * <p>
     * 如果请求的是类文件资源，优先从缓存的JavaFileObject中获取字节码
     *
     * @param name 资源名称
     * @return 输入流，如果未找到则返回null
     */
    @Override
    /**
     * getResourceAsStream方法。
     *      * @param name String类型参数
     * @return InputStream类型返回值
     */
    public InputStream getResourceAsStream(String name) {
        if (name.endsWith(CLASS_EXTENSION)) {
            String qualifiedClassName = name.substring(0, name.length() - CLASS_EXTENSION.length()).replace('/', '.');
            CharSequenceJavaFileObject javaFileObject = (CharSequenceJavaFileObject) javaFileObjectMap.get(qualifiedClassName);
            if (null != javaFileObject && null != javaFileObject.getByteCode()) {
                return new ByteArrayInputStream(javaFileObject.getByteCode());
            }
        }
        return super.getResourceAsStream(name);
    }

    /**
     * 添加Java文件对象到缓存
     * <p>
     * 暂时存放编译的源文件对象，key为全类名的别名（非URI模式），
     * 如club.throwable.compile.HelloService
     *
     * @param qualifiedClassName 全限定类名
     * @param javaFileObject     Java文件对象
     */
    void addJavaFileObject(String qualifiedClassName, JavaFileObject javaFileObject) {
        javaFileObjectMap.put(qualifiedClassName, javaFileObject);
    }

    /**
     * 获取所有缓存的Java文件对象
     *
     * @return Java文件对象集合（只读）
     */
    Collection<JavaFileObject> listJavaFileObject() {
        return Collections.unmodifiableCollection(javaFileObjectMap.values());
    }
}