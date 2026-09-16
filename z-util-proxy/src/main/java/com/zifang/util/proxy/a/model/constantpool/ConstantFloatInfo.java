package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;

/**
 * 浮点数常量
 * <p>
 * CONSTANT_Float_info用于表示4字节的float类型常量值。
 */
public class ConstantFloatInfo extends AbstractConstantPool {
    private U4 bytes;


    /**
     * ConstantFloatInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantFloatInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.bytes = U4.read(inputStream);
    }

    /**
     * getBytes方法。
     *
     * @return U4类型返回值
     */
    public U4 getBytes() {
        return bytes;
    }

    /**
     * setBytes方法。
     * * @param bytes U4类型参数
     */
    public void setBytes(U4 bytes) {
        this.bytes = bytes;
    }
}
