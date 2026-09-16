package com.zifang.util.proxy.a.model.method;

import com.zifang.util.proxy.a.model.attribute.AbstractAttribute;
import com.zifang.util.proxy.a.model.readtype.U2;

import java.util.List;

/**
 * 方法信息
 * <p>
 * 表示ClassFile中的方法表项，包含方法访问标志、名称索引、描述符索引和属性。
 */
public class MethodInfo {
    private U2 accessFlags;
    private U2 nameIndex;
    private U2 descriptorIndex;
    private U2 attributesCount;
    private List<AbstractAttribute> attributes;

    /**
     * MethodInfo方法。
     * * @param accessFlags U2类型参数
     *
     * @param nameIndex       U2类型参数
     * @param descriptorIndex U2类型参数
     * @param attributesCount U2类型参数
     * @param attributes      ListAbstractAttribute类型参数
     */
    public MethodInfo(U2 accessFlags, U2 nameIndex, U2 descriptorIndex,
                      U2 attributesCount, List<AbstractAttribute> attributes) {
        this.accessFlags = accessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.attributesCount = attributesCount;
        this.attributes = attributes;
    }

    /**
     * getAccessFlags方法。
     *
     * @return U2类型返回值
     */
    public U2 getAccessFlags() {
        return accessFlags;
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
     * getDescriptorIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getDescriptorIndex() {
        return descriptorIndex;
    }

    /**
     * getAttributesCount方法。
     *
     * @return U2类型返回值
     */
    public U2 getAttributesCount() {
        return attributesCount;
    }

    /**
     * getAttributes方法。
     *
     * @return List<AbstractAttribute>类型返回值
     */
    public List<AbstractAttribute> getAttributes() {
        return attributes;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "MethodInfo{" +
                "accessFlags=" + (accessFlags != null ? accessFlags.value : 0) +
                ", nameIndex=" + (nameIndex != null ? nameIndex.value : 0) +
                ", descriptorIndex=" + (descriptorIndex != null ? descriptorIndex.value : 0) +
                ", attributesCount=" + (attributesCount != null ? attributesCount.value : 0) +
                '}';
    }
}