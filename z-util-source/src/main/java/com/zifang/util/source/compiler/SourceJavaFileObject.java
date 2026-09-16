package com.zifang.util.source.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 源代码文件对象
 * <p>
 * 继承自SimpleJavaFileObject，用于存储Java源代码内容。
 * 在动态编译场景中用于包装用户提供的源代码字符串。
 *
 * @author zifang
 * @version 1.0.0
 */
public class SourceJavaFileObject extends SimpleJavaFileObject {


    /**
     * 源代码内容
     */
    private String sourceCode;

    /**
     * 使用指定URI和类型构造文件对象
     *
     * @param uri  文件对象的URI
     * @param kind 文件类型（SOURCE或CLASS）
     */
    protected SourceJavaFileObject(URI uri, Kind kind) {
        super(uri, kind);
    }

    /**
     * 使用类名和源代码构造文件对象
     *
     * @param className  类名
     * @param sourceCode 源代码内容
     */
    public SourceJavaFileObject(String className, String sourceCode) {
        super(fromClassName(className + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    /**
     * 从类名创建URI
     *
     * @param className 类名（包含文件扩展名）
     * @return 对应的URI
     * @throws IllegalArgumentException 如果URI语法错误
     */
    private static URI fromClassName(String className) {
        try {
            return new URI(className);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(className, e);
        }
    }
}
