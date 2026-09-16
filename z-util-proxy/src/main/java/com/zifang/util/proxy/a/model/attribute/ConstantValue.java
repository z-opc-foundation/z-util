package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;

/**
 * 常量值属性类
 * <p>
 * CONSTANT_ConstantValue_attribute用于表示静态字段的常量值。
 */
public class ConstantValue extends AbstractAttribute {

    private U2 constantValueIndex;

    /**
     * ConstantValue方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public ConstantValue(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }


    /**
     * getConstantValueIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getConstantValueIndex() {
        return constantValueIndex;
    }

    /**
     * setConstantValueIndex方法。
     * * @param constantValueIndex U2类型参数
     */
    public void setConstantValueIndex(U2 constantValueIndex) {
        this.constantValueIndex = constantValueIndex;
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        constantValueIndex = U2.read(inputStream);
    }


    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ConstantValue{constantValueIndex=" + constantValueIndex.getValue() + "}";
    }
}
