package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * UTF-8编码的字符串常量
 * <p>
 * CONSTANT_Utf8_info用于存储字符串、类名、方法名等文本数据。
 * JVM规范定义的UTF-8编码与标准UTF-8略有不同。
 */
public class Utf8Info extends AbstractConstantPool {

    private short length;
    private byte[] bytes;
    private String bytesValue;

    /**
     * Utf8Info方法。
     * * @param tag byte类型参数
     */
    public Utf8Info(byte tag) {
        super(tag);
    }

    @Override
    /**
     * read方法。
     *      * @param in InputStream类型参数
     */
    public void read(InputStream in) {
        U2 u2 = U2.read(in);
        this.length = u2.getValue();
        this.bytes = new byte[this.length];
        try {
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //convertMUTF8(bytes);
        this.bytesValue = getValue();
    }


    /**
     * getValue方法。
     *
     * @return String类型返回值
     */
    public String getValue() {
        String ret = null;
        try {
            ret = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Utf8Info{" +
                "bytesValue='" + bytesValue + '\'' +
                '}';
    }
}
