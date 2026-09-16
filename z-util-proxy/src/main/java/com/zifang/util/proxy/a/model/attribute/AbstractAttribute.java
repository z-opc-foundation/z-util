package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;

/**
 * 属性抽象基类
 * <p>
 * JVM中各种属性的父类，定义了属性的基本结构。
 * 包含属性名索引和属性长度。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7">JVM Specification - Attributes</a>
 */
public abstract class AbstractAttribute {

    private U2 attributeNameIndex;
    private U4 attributeLength;//明属性值所占用的字节数

    /**
     * AbstractAttribute方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public AbstractAttribute(U2 attributeNameIndex, U4 attributeLength) {
        this.attributeNameIndex = attributeNameIndex;
        this.attributeLength = attributeLength;
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     *
     * @return abstract void类型返回值
     */
    public abstract void read(InputStream inputStream);

    /**
     * getAttributeNameIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getAttributeNameIndex() {
        return attributeNameIndex;
    }

    /**
     * setAttributeNameIndex方法。
     * * @param attributeNameIndex U2类型参数
     */
    public void setAttributeNameIndex(U2 attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    /**
     * getAttributeLength方法。
     *
     * @return U4类型返回值
     */
    public U4 getAttributeLength() {
        return attributeLength;
    }

    /**
     * setAttributeLength方法。
     * * @param attributeLength U4类型参数
     */
    public void setAttributeLength(U4 attributeLength) {
        this.attributeLength = attributeLength;
    }
}
