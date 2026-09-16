package com.zifang.util.proxy.a.model.field;

import com.zifang.util.proxy.a.model.attribute.AbstractAttribute;
import com.zifang.util.proxy.a.model.attribute.AttributeFactory;
import com.zifang.util.proxy.a.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 字段表
 * <p>
 * 存储ClassFile中所有的字段信息。
 */
public class FieldTable {
    private List<FieldInfo> fields = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param stream     输入流
     * @param poolList   常量池列表
     * @param fieldCount 字段数量
     */
    public FieldTable(InputStream stream, List<AbstractConstantPool> poolList, int fieldCount) {
        for (int i = 0; i < fieldCount; i++) {
            fields.add(parseField(stream, poolList));
        }
    }

    /**
     * 解析单个字段
     *
     * @param inputStream 输入流
     * @param poolList    常量池列表
     * @return 解析后的字段信息
     */
    public FieldInfo parseField(InputStream inputStream, List<AbstractConstantPool> poolList) {
        U2 accessFlags = U2.read(inputStream);
        U2 nameIndex = U2.read(inputStream);
        U2 descriptorIndex = U2.read(inputStream);
        U2 attributesCount = U2.read(inputStream);

        List<AbstractAttribute> attributes = new ArrayList<>();
        short count = attributesCount.value;
        for (int i = 0; i < count; i++) {
            AbstractAttribute attribute = AttributeFactory.getAttributeTable(inputStream, poolList);
            if (attribute != null) {
                attributes.add(attribute);
            }
        }

        return new FieldInfo(accessFlags, nameIndex, descriptorIndex, attributesCount, attributes);
    }

    /**
     * 获取所有字段
     *
     * @return 字段列表
     */
    public List<FieldInfo> getFields() {
        return fields;
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * 根据索引获取字段
     *
     * @param index 字段索引
     * @return 字段信息
     */
    public FieldInfo getField(int index) {
        if (index >= 0 && index < fields.size()) {
            return fields.get(index);
        }
        return null;
    }
}