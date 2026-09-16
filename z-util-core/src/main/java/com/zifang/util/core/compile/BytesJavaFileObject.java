package com.zifang.util.core.compile;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * 字节数组形式的 Java 文件对象。
 * <p>
 * 用于动态编译 Java 源码时，以字节数组形式存储编译后的类文件内容。
 *
 * @author zifang
 * @see SimpleJavaFileObject
 */
public class BytesJavaFileObject extends SimpleJavaFileObject {

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * BytesJavaFileObject方法。
     * * @param name String类型参数
     *
     * @param kind Kind类型参数
     */
    public BytesJavaFileObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    /**
     * getBytes方法。
     *
     * @return byte[]类型返回值
     */
    public byte[] getBytes() {
        return bos.toByteArray();
    }

    @Override
    /**
     * openOutputStream方法。
     * @return OutputStream类型返回值
     */
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }

    public void close() throws IOException {
        bos.close();
    }
}
