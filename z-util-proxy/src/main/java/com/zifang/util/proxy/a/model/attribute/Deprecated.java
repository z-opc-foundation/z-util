package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;

/**
 * 已弃用属性
 * <p>
 * Deprecated_attribute用于标记类、接口、字段或方法已被弃用。
 * 用于编译时警告和运行时警告。
 */
public class Deprecated extends AbstractAttribute {


    /**
     * Deprecated方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public Deprecated(U2 attributeNameIndex, U4 attributeLength) {
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
