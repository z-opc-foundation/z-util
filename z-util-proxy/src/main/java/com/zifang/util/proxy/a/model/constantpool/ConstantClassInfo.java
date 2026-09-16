package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 字符串常量
 * <p>
 * CONSTANT_String_info用于表示字符串常量的符号引用。
 * string_index指向常量池中CONSTANT_Utf8_info常量的索引。
 */
public class ConstantClassInfo extends AbstractConstantPool {

    private U2 stringIndex;

    /**
     * ConstantClassInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantClassInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.stringIndex = U2.read(inputStream);
    }

    /**
     * getStringIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getStringIndex() {
        return stringIndex;
    }
}
