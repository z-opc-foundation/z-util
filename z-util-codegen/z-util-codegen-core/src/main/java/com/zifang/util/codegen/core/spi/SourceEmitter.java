package com.zifang.util.codegen.core.spi;

import com.zifang.util.codegen.core.emit.JavaSource;

/**
 * 源码生成出口：把新写出来的顶层类型落成 {@code .java} 文件。
 *
 * @author zifang
 */
public interface SourceEmitter {

    /**
     * 生成一个新的顶层类型文件。
     *
     * @param qualifiedName 新类型的全限定名
     * @param source        源码内容
     */
    void generate(String qualifiedName, JavaSource source);
}
