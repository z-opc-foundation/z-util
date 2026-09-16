package com.zifang.util.proxy.compliler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * A Java class that holds the bytecodes in a byte array.
 *
 * @author zifang Horstmann
 * @version 1.00 2007-11-02
 */

/**
 * ByteArrayJavaClass类。
 */
public class ByteArrayJavaClass extends SimpleJavaFileObject {
    private ByteArrayOutputStream stream;

    /**
     * Constructs a new ByteArrayJavaClass.
     *
     * @param name the name of the class file represented by this file object
     */
    /**
     * ByteArrayJavaClass方法。
     * * @param name String类型参数
     */
    public ByteArrayJavaClass(String name) {
        super(URI.create("bytes:///" + name), Kind.CLASS);
        stream = new ByteArrayOutputStream();
    }

    /**
     * openOutputStream方法。
     *
     * @return OutputStream类型返回值
     */
    public OutputStream openOutputStream() throws IOException {
        return stream;
    }

    /**
     * getBytes方法。
     *
     * @return byte[]类型返回值
     */
    public byte[] getBytes() {
        return stream.toByteArray();
    }
}
