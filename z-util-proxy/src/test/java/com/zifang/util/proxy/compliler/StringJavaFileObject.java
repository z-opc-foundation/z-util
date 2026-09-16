package com.zifang.util.proxy.compliler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * 用于将java源码保存在content属性中
 */

/**
 * StringJavaFileObject类。
 */
public class StringJavaFileObject<T> extends SimpleJavaFileObject {

    /**
     * 保存java code
     */
    private T content;


    /**
     * 调用父类构造器，并设置content
     *
     * @param className
     * @param content
     */
    /**
     * StringJavaFileObject方法。
     * * @param className String类型参数
     *
     * @param content T类型参数
     */
    public StringJavaFileObject(String className, T content) {
        super(URI.create("string:///" + className.replace('.', '/')
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    /**
     * 实现getCharContent，使得JavaCompiler可以从content获取java源码
     *
     * @param ignoreEncodingErrors
     * @return
     */
    @Override
    /**
     * getCharContent方法。
     *      * @param ignoreEncodingErrors boolean类型参数
     * @return String类型返回值
     */
    public String getCharContent(boolean ignoreEncodingErrors) {
        return content.toString();
    }
}
