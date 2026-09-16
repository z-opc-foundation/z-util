package com.zifang.util.source.compiler;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户自定义编译文件管理器
 * <p>
 * 继承自ForwardingJavaFileManager，用于在动态编译过程中管理Java文件对象。
 * 支持自定义源代码和字节码的存储、检索，以及覆盖默认的类加载器行为。
 * 主要用于内存中编译场景，无需写入文件系统。
 *
 * @author zifang
 * @version 1.0.0
 */
public class CustomerCompileJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    /**
     * 自定义类加载器
     */
    private final CustomerCompileClassLoader classLoader;

    /**
     * Java文件对象缓存表，键为URI
     */
    private final Map<URI, JavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();

    /**
     * 构造自定义编译文件管理器
     *
     * @param fileManager 标准文件管理器
     * @param classLoader 自定义类加载器
     */
    public CustomerCompileJavaFileManager(JavaFileManager fileManager, CustomerCompileClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
    }

    /**
     * 从位置、包名和相对名称构建URI
     *
     * @param location     位置（如CLASS_PATH、SOURCE_PATH）
     * @param packageName  包名
     * @param relativeName 相对名称
     * @return 构建的URI
     * @throws IllegalArgumentException 如果URI构建失败
     */
    public static URI fromLocation(Location location, String packageName, String relativeName) {
        try {
            return new URI(location.getName() + '/' + packageName + '/' + relativeName);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 获取Java文件输出对象
     * <p>
     * 编译器调用此方法获取用于存储编译结果的JavaFileObject。
     * 这里替换为CharSequenceJavaFileObject实现，以便在内存中存储字节码。
     *
     * @param location  文件位置
     * @param className 类名
     * @param kind      文件类型
     * @param sibling   兄弟文件对象
     * @return Java文件对象
     * @throws IOException 如果发生IO错误
     */
    @Override
    /**
     * getJavaFileForOutput方法。
     *      * @param location Location类型参数
     * @param className String类型参数
     * @param kind JavaFileObject.Kind类型参数
     * @param sibling FileObject类型参数
     * @return JavaFileObject类型返回值
     */
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaFileObject javaFileObject = new CharSequenceJavaFileObject(className, kind);
        classLoader.addJavaFileObject(className, javaFileObject);
        return javaFileObject;
    }

    /**
     * 获取类加载器
     * <p>
     * 返回自定义的类加载器而非标准类加载器
     *
     * @param location 位置
     * @return 类加载器
     */
    @Override
    /**
     * getClassLoader方法。
     *      * @param location Location类型参数
     * @return ClassLoader类型返回值
     */
    public ClassLoader getClassLoader(Location location) {
        return classLoader;
    }

    /**
     * 推断二进制名称
     * <p>
     * 对于自定义的CharSequenceJavaFileObject，直接返回其名称
     *
     * @param location 位置
     * @param file     文件对象
     * @return 二进制名称
     */
    @Override
    /**
     * inferBinaryName方法。
     *      * @param location Location类型参数
     * @param file JavaFileObject类型参数
     * @return String类型返回值
     */
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof CharSequenceJavaFileObject) {
            return file.getName();
        }
        return super.inferBinaryName(location, file);
    }

    /**
     * 列出指定位置和包名下的文件对象
     * <p>
     * 合并了标准文件管理器的结果和内存中缓存的文件对象
     *
     * @param location    位置
     * @param packageName 包名
     * @param kinds       文件类型集合
     * @param recurse     是否递归子包
     * @return 文件对象迭代器
     * @throws IOException 如果发生IO错误
     */
    @Override
    /**
     * list方法。
     *      * @param location Location类型参数
     * @param packageName String类型参数
     * @param kinds SetJavaFileObject.Kind类型参数
     * @param recurse boolean类型参数
     * @return Iterable<JavaFileObject>类型返回值
     */
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> superResult = super.list(location, packageName, kinds, recurse);
        List<JavaFileObject> result = new ArrayList<>();
        // 这里要区分编译的Location以及编译的Kind
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            // .class文件以及classPath下
            for (JavaFileObject file : javaFileObjectMap.values()) {
                if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                    result.add(file);
                }
            }
            // 这里需要额外添加类加载器加载的所有Java文件对象
            result.addAll(classLoader.listJavaFileObject());
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            // .java文件以及编译路径下
            for (JavaFileObject file : javaFileObjectMap.values()) {
                if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                    result.add(file);
                }
            }
        }
        for (JavaFileObject javaFileObject : superResult) {
            result.add(javaFileObject);
        }
        return result;
    }

    /**
     * 添加Java文件对象到缓存
     *
     * @param location       位置
     * @param packageName    包名
     * @param relativeName   相对名称
     * @param javaFileObject Java文件对象
     */
    public void addJavaFileObject(Location location, String packageName, String relativeName, JavaFileObject javaFileObject) {
        javaFileObjectMap.put(fromLocation(location, packageName, relativeName), javaFileObject);
    }

    /**
     * 添加Java文件对象到缓存
     *
     * @param uri            URI
     * @param javaFileObject Java文件对象
     */
    public void addJavaFileObject(URI uri, JavaFileObject javaFileObject) {
        javaFileObjectMap.put(uri, javaFileObject);
    }
}