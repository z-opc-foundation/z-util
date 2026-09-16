package com.zifang.util.proxy.a.model.readtype;

import java.io.IOException;
import java.io.InputStream;

/**
 * 无符号2字节数据类型
 * <p>
 * 用于从输入流中读取2字节的无符号整数，范围为0-65535。
 * JVM中多使用此类型表示索引和计数。
 */
public class U2 {
    public short value;

    /**
     * U2方法。
     * * @param value short类型参数
     */
    public U2(short value) {
        this.value = value;
    }

    /**
     * read方法。
     * * @param stream InputStream类型参数
     *
     * @return static U2类型返回值
     */
    public static U2 read(InputStream stream) {
        byte[] bytes = new byte[2];
        try {
            stream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        short value = 0;
        for (int i = 0; i < 2; i++) {
            value <<= 8;
            value |= bytes[i] & 0xff;
        }

        U2 u2 = new U2(value);
        return u2;
    }

    /**
     * getValue方法。
     *
     * @return short类型返回值
     */
    public short getValue() {
        return value;
    }


}
