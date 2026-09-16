package com.zifang.util.proxy.a.model.readtype;

import java.io.IOException;
import java.io.InputStream;

/**
 * 无符号8字节数据类型
 * <p>
 * 用于从输入流中读取8字节的长整数数据。
 * JVM中用于表示long和double类型的位模式存储。
 */
public class U8 {

    private long value;

    /**
     * U8方法。
     * * @param value long类型参数
     */
    public U8(long value) {
        this.value = value;
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     *
     * @return static U8类型返回值
     */
    public static U8 read(InputStream inputStream) {
        byte[] bytes = new byte[8];
        try {
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value <<= 8;
            value |= bytes[i] & 0xFF;
        }
        U8 u8 = new U8(value);
        return u8;
    }

    /**
     * getValue方法。
     *
     * @return long类型返回值
     */
    public long getValue() {
        return value;
    }

}
