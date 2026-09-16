package com.zifang.util.proxy.a.model.method;

import com.zifang.util.proxy.a.model.attribute.AbstractAttribute;
import com.zifang.util.proxy.a.model.attribute.AttributeFactory;
import com.zifang.util.proxy.a.model.constantpool.AbstractConstantPool;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法表
 * <p>
 * 存储ClassFile中所有的方法信息。
 */
public class MethodTable {
    private List<MethodInfo> methods = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param stream      输入流
     * @param poolList    常量池列表
     * @param methodCount 方法数量
     */
    public MethodTable(InputStream stream, List<AbstractConstantPool> poolList, int methodCount) {
        for (int i = 0; i < methodCount; i++) {
            methods.add(parseMethod(stream, poolList));
        }
    }

    /**
     * 解析单个方法
     *
     * @param inputStream 输入流
     * @param poolList    常量池列表
     * @return 解析后的方法信息
     */
    public MethodInfo parseMethod(InputStream inputStream, List<AbstractConstantPool> poolList) {
        com.zifang.util.proxy.a.model.readtype.U2 accessFlags =
                com.zifang.util.proxy.a.model.readtype.U2.read(inputStream);
        com.zifang.util.proxy.a.model.readtype.U2 nameIndex =
                com.zifang.util.proxy.a.model.readtype.U2.read(inputStream);
        com.zifang.util.proxy.a.model.readtype.U2 descriptorIndex =
                com.zifang.util.proxy.a.model.readtype.U2.read(inputStream);
        com.zifang.util.proxy.a.model.readtype.U2 attributesCount =
                com.zifang.util.proxy.a.model.readtype.U2.read(inputStream);

        List<AbstractAttribute> attributes = new ArrayList<>();
        short count = attributesCount.value;
        for (int i = 0; i < count; i++) {
            AbstractAttribute attribute = AttributeFactory.getAttributeTable(inputStream, poolList);
            if (attribute != null) {
                attributes.add(attribute);
            }
        }

        return new MethodInfo(accessFlags, nameIndex, descriptorIndex, attributesCount, attributes);
    }

    /**
     * 获取所有方法
     *
     * @return 方法列表
     */
    public List<MethodInfo> getMethods() {
        return methods;
    }

    /**
     * 获取方法数量
     *
     * @return 方法数量
     */
    public int getMethodCount() {
        return methods.size();
    }

    /**
     * 根据索引获取方法
     *
     * @param index 方法索引
     * @return 方法信息
     */
    public MethodInfo getMethod(int index) {
        if (index >= 0 && index < methods.size()) {
            return methods.get(index);
        }
        return null;
    }
}