package com.zifang.util.proxy.a.model.constantpool;

import java.io.InputStream;

/**
 * 常量池抽象基类
 * <p>
 * JVM常量池中所有常量项的父类，定义了常量池项的基本结构。
 * 每个常量池项都有一个tag字段标识其类型。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4">JVM Specification - Constant Pool</a>
 */
public abstract class AbstractConstantPool {
    public static final byte CONSTANT_UTF8_INFO = 1;                  // CONSTANT_Utf8_info
    public static final byte CONSTANT_INTEGER_INFO = 3;               // CONSTANT_Integer_info
    public static final byte CONSTANT_FLOAT_INFO = 4;                 // CONSTANT_Float_info
    public static final byte CONSTANT_LONG_INFO = 5;                  // CONSTANT_Long_info
    public static final byte CONSTANT_DOUBLE_INFO = 6;                // CONSTANT_Double_info
    public static final byte CONSTANT_CLASS_INFO = 7;                 // CONSTANT_Class_info
    public static final byte CONSTANT_STRING_INFO = 8;                // CONSTANT_String_info
    public static final byte CONSTANT_FIELDREF_INFO = 9;              // CONSTANT_FieldRef_info
    public static final byte CONSTANT_METHODREF_INFO = 10;            // CONSTANT_MethodRef_info
    public static final byte CONSTANT_INTERFACEMETHODREF_INFO = 11;   // CONSTANT_InterfaceMethodRef_info
    public static final byte CONSTANT_NAMEANDTYPE_INFO = 12;                   // CONSTANT_NameAndType_info
    public static final byte CONSTANT_METHODHANDLE_INFO = 15;                  // CONSTANT_MethodHandle_info
    public static final byte CONSTANT_METHODTYPE_INFO = 16;                    // CONSTANT_MethodType_info
    public static final byte CONSTANT_INVOKEDYNAMIC_INFO = 18;                 // CONSTANT_InvokeDynamic_info

    private byte tag;

    /**
     * AbstractConstantPool方法。
     * * @param tag byte类型参数
     */
    public AbstractConstantPool(byte tag) {
        this.tag = tag;
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     *
     * @return abstract void类型返回值
     */
    public abstract void read(InputStream inputStream);

    /**
     * getTag方法。
     *
     * @return byte类型返回值
     */
    public byte getTag() {
        return tag;
    }

    /**
     * setTag方法。
     * * @param tag byte类型参数
     */
    public void setTag(byte tag) {
        this.tag = tag;
    }
}
