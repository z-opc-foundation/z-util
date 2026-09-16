package com.zifang.util.proxy.a.model.readtype;

import java.io.IOException;
import java.io.InputStream;

/**
 * 无符号4字节数据类型
 * <p>
 * 用于从输入流中读取4字节的无符号整数，范围为0-4294967295。
 * JVM中用于表示float类型的位模式存储。
 */
public class U4 {

    public int value;
    public byte[] bytes;

    /**
     * U4方法。
     * * @param value int类型参数
     *
     * @param bytes byte[]类型参数
     */
    public U4(int value, byte[] bytes) {
        this.value = value;
        this.bytes = bytes;
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     *
     * @return static U4类型返回值
     */
    public static U4 read(InputStream inputStream) {
        byte[] bytes = new byte[4];
        try {
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int value = 0;
        //4字节数据转成int类型数据需要做的位运算转换.
        for (int i = 0; i < 4; i++) {
            value <<= 8;
            int temp = bytes[i] & 0xFF;
            value = value | temp;        //想保持二进制补码的一致性。进行 & 运算； & 运算是: 两个为1则为1；
            // | 运算：有一个为1 则为1；
        }
        U4 u4 = new U4(value, bytes);
        return u4;
    }

    /**
     * getValue方法。
     *
     * @return int类型返回值
     */
    public int getValue() {
        return value;
    }

}
