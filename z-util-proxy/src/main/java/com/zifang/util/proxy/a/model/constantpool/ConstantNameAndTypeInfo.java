package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 名称和类型描述符
 * <p>
 * CONSTANT_NameAndType_info用于表示字段或方法的名称和类型的符号引用。
 * 包含name_index和descriptor_index两个指向UTF-8常量的索引。
 */
public class ConstantNameAndTypeInfo extends AbstractConstantPool {
    private U2 nameIndex;
    private U2 descriptorIndex;


    /**
     * ConstantNameAndTypeInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantNameAndTypeInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.nameIndex = U2.read(inputStream);
        this.descriptorIndex = U2.read(inputStream);
    }

    /**
     * getNameIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getNameIndex() {
        return nameIndex;
    }

    /**
     * getDescriptorIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getDescriptorIndex() {
        return descriptorIndex;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ConstantNameAndTypeInfo{" +
                "nameIndex=" + nameIndex.value +
                ", descriptorIndex=" + descriptorIndex.value +
                '}';
    }
}
