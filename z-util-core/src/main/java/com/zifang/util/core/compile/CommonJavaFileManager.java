package com.zifang.util.core.compile;

import javax.tools.JavaFileManager;

/**
 * 通用 Java 文件管理器。
 * <p>
 * 基于 CustomerJavaFileManager 实现，用于管理动态编译过程中的文件对象。
 *
 * @author zifang
 * @see CustomerJavaFileManager
 */
public class CommonJavaFileManager extends CustomerJavaFileManager {
    /**
     * CommonJavaFileManager方法。
     * * @param fileManager JavaFileManager类型参数
     */
    protected CommonJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }
}
