package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 内部类属性
 * <p>
 * InnerClasses_attribute用于记录内部类和其外围类的关联信息。
 */
public class InnerClasses extends AbstractAttribute {

    private U2 numberOfClasses;
    private List<InnerClassInfo> innerClasses = new ArrayList<>(numberOfClasses.value);


    /**
     * InnerClasses方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public InnerClasses(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {

    }

    /**
     * InnerClassInfo类。
     */
    public class InnerClassInfo {
        private U2 innerClassInfoIndex;
        private U2 outerClassInfoIndex;
        private U2 innerNameIndex;
        private U2 InnerClassAccessFlags;


    }
}
