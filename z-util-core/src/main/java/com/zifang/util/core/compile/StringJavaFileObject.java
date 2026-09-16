package com.zifang.util.core.compile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * 字符串形式的 Java 源码文件对象。
 * <p>
 * 用于将 Java 源码字符串封装为 JavaFileObject，供动态编译使用。
 *
 * @param <T> 内容类型
 * @author zifang
 * @see SimpleJavaFileObject
 */
public class StringJavaFileObject<T> extends SimpleJavaFileObject {

    private T content;

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
