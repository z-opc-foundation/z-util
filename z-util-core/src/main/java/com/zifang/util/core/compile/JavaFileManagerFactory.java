package com.zifang.util.core.compile;


import javax.tools.StandardJavaFileManager;
import java.util.logging.Logger;

/**
 * Java 文件管理器工厂。
 * <p>
 * 用于创建 CustomerJavaFileManager 实例。
 *
 * @author zifang
 * @see CustomerJavaFileManager
 */
public class JavaFileManagerFactory {

    private static final Logger log = Logger.getLogger(JavaFileManagerFactory.class.getName());

    /**
     * getJavaFileManager方法。
     * * @param standardManager StandardJavaFileManager类型参数
     *
     * @return static CustomerJavaFileManager类型返回值
     */
    public static CustomerJavaFileManager getJavaFileManager(StandardJavaFileManager standardManager) {
        return new CommonJavaFileManager(standardManager);
    }
}
