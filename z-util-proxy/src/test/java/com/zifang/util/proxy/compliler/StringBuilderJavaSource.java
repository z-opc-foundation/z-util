package com.zifang.util.proxy.compliler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * A Java source that holds the code in a string builder.
 *
 * @author zifang Horstmann
 * @version 1.00 2007-11-02
 */

/**
 * StringBuilderJavaSource类。
 */
public class StringBuilderJavaSource extends SimpleJavaFileObject {
    private StringBuilder code;

    /**
     * Constructs a new StringBuilderJavaSource.
     *
     * @param name the name of the source file represented by this file object
     */
    /**
     * StringBuilderJavaSource方法。
     * * @param name String类型参数
     */
    public StringBuilderJavaSource(String name) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                Kind.SOURCE);
        code = new StringBuilder();
    }

    /**
     * getCharContent方法。
     * * @param ignoreEncodingErrors boolean类型参数
     *
     * @return CharSequence类型返回值
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }

    /**
     * append方法。
     * * @param str String类型参数
     */
    public void append(String str) {
        code.append(str);
        code.append('\n');
    }
}
