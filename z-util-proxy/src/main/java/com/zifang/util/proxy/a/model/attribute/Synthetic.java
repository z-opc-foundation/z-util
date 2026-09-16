package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;

/**
 * 合成属性
 * <p>
 * Synthetic_attribute表示类、字段或方法是由编译器生成的，
 * 不是源码中直接存在的。
 */
public class Synthetic extends AbstractAttribute {

    /**
     * Synthetic方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public Synthetic(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {

    }
}
