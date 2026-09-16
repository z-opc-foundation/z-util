package com.zifang.util.core.compile;


import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 自定义 Java 文件管理器抽象基类。
 * <p>
 * 继承自 ForwardingJavaFileManager，用于管理动态编译过程中的文件对象。
 *
 * @author zifang
 * @see ForwardingJavaFileManager
 * @see BytesJavaFileObject
 */
public abstract class CustomerJavaFileManager extends ForwardingJavaFileManager {

    private Map<String, BytesJavaFileObject> fileObjectHashMap = new HashMap<>();

    /**
     * CustomerJavaFileManager方法。
     * * @param fileManager JavaFileManager类型参数
     */
    protected CustomerJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    /**
     * getFileObjectHashMap方法。
     *
     * @return Map<String, BytesJavaFileObject>类型返回值
     */
    public Map<String, BytesJavaFileObject> getFileObjectHashMap() {
        return fileObjectHashMap;
    }

    @Override
    /**
     * getJavaFileForOutput方法。
     *      * @param location Location类型参数
     * @param className String类型参数
     * @param kind JavaFileObject.Kind类型参数
     * @param sibling FileObject类型参数
     * @return JavaFileObject类型返回值
     */
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

        BytesJavaFileObject fileObject = fileObjectHashMap.get(className);
        if (fileObject == null) {
            fileObject = new BytesJavaFileObject(className, kind);
            fileObjectHashMap.put(className, fileObject);
            return fileObject;
        } else {
            return fileObject;
        }
    }

    @Override
    /**
     * hasLocation方法。
     *      * @param location Location类型参数
     * @return boolean类型返回值
     */
    public boolean hasLocation(Location location) {
        return super.hasLocation(location);
    }

    @Override
    /**
     * isSameFile方法。
     *      * @param a FileObject类型参数
     * @param b FileObject类型参数
     * @return boolean类型返回值
     */
    public boolean isSameFile(FileObject a, FileObject b) {
        return super.isSameFile(a, b);
    }

    @Override
    /**
     * handleOption方法。
     *      * @param current String类型参数
     * @param remaining Iterator类型参数
     * @return boolean类型返回值
     */
    public boolean handleOption(String current, Iterator remaining) {
        return super.handleOption(current, remaining);
    }

    @Override
    /**
     * isSupportedOption方法。
     *      * @param option String类型参数
     * @return int类型返回值
     */
    public int isSupportedOption(String option) {
        return super.isSupportedOption(option);
    }

    @Override
    /**
     * getJavaFileForInput方法。
     *      * @param location Location类型参数
     * @param className String类型参数
     * @param kind JavaFileObject.Kind类型参数
     * @return JavaFileObject类型返回值
     */
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        return super.getJavaFileForInput(location, className, kind);
    }

    @Override
    /**
     * getFileForInput方法。
     *      * @param location Location类型参数
     * @param packageName String类型参数
     * @param relativeName String类型参数
     * @return FileObject类型返回值
     */
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    /**
     * getFileForOutput方法。
     *      * @param location Location类型参数
     * @param packageName String类型参数
     * @param relativeName String类型参数
     * @param sibling FileObject类型参数
     * @return FileObject类型返回值
     */
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return super.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    /**
     * flush方法。
     */
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    /**
     * close方法。
     */
    public void close() throws IOException {
        super.close();
    }
}
