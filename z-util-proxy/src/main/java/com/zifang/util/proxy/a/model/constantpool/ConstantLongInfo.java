package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U8;

import java.io.InputStream;

/**
 * 长整数常量
 * <p>
 * CONSTANT_Long_info用于表示8字节的long类型常量值。
 * 注意：Long和Double类型在常量池中占用两个索引位置。
 */
public class ConstantLongInfo extends AbstractConstantPool {
    private U8 bytes;


    /**
     * ConstantLongInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantLongInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.bytes = U8.read(inputStream);
    }

    /**
     * getBytes方法。
     *
     * @return U8类型返回值
     */
    public U8 getBytes() {
        return bytes;
    }
}
