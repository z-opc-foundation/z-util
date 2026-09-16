package com.zifang.util.proxy.compliler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class CFJavaFileManager extends ForwardingJavaFileManager {
    private static final Logger log = LoggerFactory.getLogger(CFJavaFileManager.class);
    /**
     * 保存编译后Class文件的对象
     */
    private Map<String, BytesJavaFileObject> fileObjectHashMap = new HashMap<>();

    /**
     * CFJavaFileManager方法。
     * * @param fileManager JavaFileManager类型参数
     */
    protected CFJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */

    /**
     * getFileObjectHashMap方法。
     *
     * @return Map<String, BytesJavaFileObject>类型返回值
     */
    public Map<String, BytesJavaFileObject> getFileObjectHashMap() {
        return fileObjectHashMap;
    }

    /**
     * 将JavaFileObject对象的引用交给JavaCompiler，让它将编译好后的Class文件装载进来
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
        log.info("isSameFile : {}", a);

        return super.isSameFile(a, b);
    }

    /**
     * 处理 编译命令的参数，例如-encoding， -classpath 等
     *
     * @param current   参数名，如 -classpath
     * @param remaining 参数值列表
     * @return
     */
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
        log.info("isSupportedOption : {}", option);

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
        log.info("getJavaFileForInput : {}", location);

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
        log.info("getFileForInput : {}", location);

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
        log.info("getFileForOutput : {}", location);

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
