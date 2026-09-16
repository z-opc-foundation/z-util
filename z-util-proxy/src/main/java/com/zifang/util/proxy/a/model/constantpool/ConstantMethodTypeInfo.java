package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 方法类型信息
 * <p>
 * CONSTANT_MethodType_info用于表示方法类型的符号引用。
 * descriptor_index指向CONSTANT_Utf8_info常量的索引。
 */
public class ConstantMethodTypeInfo extends AbstractConstantPool {

    private U2 descriptorIndex;


    /**
     * ConstantMethodTypeInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantMethodTypeInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.descriptorIndex = U2.read(inputStream);
    }

    /**
     * getDescriptorIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getDescriptorIndex() {
        return descriptorIndex;
    }
}
