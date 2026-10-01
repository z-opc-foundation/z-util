package com.zifang.util.bc.compile;

import javax.tools.StandardJavaFileManager;
import java.util.logging.Logger;

/**
 * JavaFileManagerFactory类。
 */
public class JavaFileManagerFactory {

    private static final Logger log = Logger.getLogger(JavaFileManagerFactory.class.getName());

    /**
     * getJavaFileManager方法。
     * * @param standardManager StandardJavaFileManager类型参数
     *
     * @return static CFJavaFileManager类型返回值
     */
    public static CFJavaFileManager getJavaFileManager(StandardJavaFileManager standardManager) {
        // 2026-10-01 恢复：曾整体 return null（实现全被注释），管线一调即 NPE。
        // CFJavaFileManager 把编译输出重定向进内存 BytesJavaFileObject，是制造字节码的容器。
        return new CFJavaFileManager(standardManager);
    }
}
