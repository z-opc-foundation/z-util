package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 字段引用信息
 * <p>
 * CONSTANT_Fieldref_info用于表示类中声明的字段的符号引用。
 * 包含指向类信息的class_index和指向字段描述的name_index。
 */
public class FieldRefInfo extends AbstractConstantPool {

    private U2 classIndex;
    private U2 nameIndex;

    /**
     * FieldRefInfo方法。
     * * @param tag byte类型参数
     */
    public FieldRefInfo(byte tag) {
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
     * setClassIndex方法。
     * * @param classIndex U2类型参数
     */
    public void setClassIndex(U2 classIndex) {
        this.classIndex = classIndex;
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
     * setNameIndex方法。
     * * @param nameIndex U2类型参数
     */
    public void setNameIndex(U2 nameIndex) {
        this.nameIndex = nameIndex;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "FieldRefInfo{" +
                "classIndex=" + classIndex.value +
                ", nameIndex=" + nameIndex.value +
                '}';
    }
}
