package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.bytecode.model.constantpool.ClassInfo;
import com.zifang.util.proxy.bytecode.model.constantpool.ConstantClassInfo;
import com.zifang.util.proxy.bytecode.model.constantpool.Utf8Info;
import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exceptions 属性（4.7.5）：方法显式声明的检查型异常类型列表。
 */
public class ExceptionsAttribute extends AbstractAttribute {

    private final List<U2> exceptionIndexTable = new ArrayList<>();
    private final List<String> exceptionClassNames = new ArrayList<>();

    public ExceptionsAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        U2 count = U2.read(inputStream);
        for (int i = 0; i < count.value; i++) {
            U2 idx = U2.read(inputStream);
            exceptionIndexTable.add(idx);
        }
    }

    /**
     * 把异常类的常量池索引解析为外部形式的全限定名（点分）。
     */
    public void resolve(List<AbstractConstantPool> poolList) {
        exceptionClassNames.clear();
        for (U2 idx : exceptionIndexTable) {
            int i = idx.value - 1;
            if (i < 0 || i >= poolList.size()) {
                continue;
            }
            AbstractConstantPool p = poolList.get(i);
            if (!(p instanceof ConstantClassInfo) && !(p instanceof ClassInfo)) {
                continue;
            }
            int nameIdx;
            if (p instanceof ClassInfo) {
                nameIdx = ((ClassInfo) p).getNameIndex().value - 1;
            } else if (p instanceof ConstantClassInfo) {
                nameIdx = ((ConstantClassInfo) p).getStringIndex().value - 1;
            } else {
                continue;
            }
            if (nameIdx < 0 || nameIdx >= poolList.size()) {
                continue;
            }
            AbstractConstantPool utf = poolList.get(nameIdx);
            if (utf instanceof Utf8Info) {
                String internal = ((Utf8Info) utf).getValue();
                exceptionClassNames.add(internal.replace('/', '.'));
            }
        }
    }

    public List<U2> getExceptionIndexTable() {
        return Collections.unmodifiableList(exceptionIndexTable);
    }

    public List<String> getExceptionClassNames() {
        return Collections.unmodifiableList(exceptionClassNames);
    }
}