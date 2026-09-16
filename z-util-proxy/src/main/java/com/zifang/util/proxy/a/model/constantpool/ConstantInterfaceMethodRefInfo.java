package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 接口方法引用信息
 * <p>
 * CONSTANT_InterfaceMethodref_info用于表示接口中声明的方法的符号引用。
 * 结构与CONSTANT_Methodref_info相同。
 */
public class ConstantInterfaceMethodRefInfo extends AbstractConstantPool {
    private U2 classIndex;
    private U2 nameIndex;


    /**
     * ConstantInterfaceMethodRefInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantInterfaceMethodRefInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.classIndex = U2.read(inputStream);
        this.nameIndex = U2.read(inputStream);
    }


    /**
     * getClassIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getClassIndex() {
        return classIndex;
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
