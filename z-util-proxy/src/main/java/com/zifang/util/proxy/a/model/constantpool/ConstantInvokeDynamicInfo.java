package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 动态调用信息
 * <p>
 * CONSTANT_InvokeDynamic_info用于表示invokedynamic指令的动态调用点。
 * 包含引导方法索引和方法名称类型索引。
 */
public class ConstantInvokeDynamicInfo extends AbstractConstantPool {

    private U2 bootstrapMethodAttrIndex;
    private U2 nameIndex;

    /**
     * ConstantInvokeDynamicInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantInvokeDynamicInfo(byte tag) {
        super(tag);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.bootstrapMethodAttrIndex = U2.read(inputStream);
        this.nameIndex = U2.read(inputStream);
    }

    /**
     * getBootstrapMethodAttrIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getBootstrapMethodAttrIndex() {
        return bootstrapMethodAttrIndex;
    }

    /**
     * getNameIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getNameIndex() {
        return nameIndex;
    }
}
