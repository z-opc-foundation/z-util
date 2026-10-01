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
 * NestHost 属性（4.7.28）：嵌套类声明宿主类（Java 11+ 取代了 InnerClasses 用于 nestmate 反射）。
 */
public class NestHostAttribute extends AbstractAttribute {

    private U2 hostClassIndex;
    private String hostClassInternalName;

    public NestHostAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        hostClassIndex = U2.read(inputStream);
    }

    public void resolve(List<AbstractConstantPool> poolList) {
        int idx = hostClassIndex.value - 1;
        if (idx < 0 || idx >= poolList.size()) {
            return;
        }
        AbstractConstantPool p = poolList.get(idx);
        U2 nameIdx;
        if (p instanceof ClassInfo) {
            nameIdx = ((ClassInfo) p).getNameIndex();
        } else if (p instanceof ConstantClassInfo) {
            nameIdx = ((ConstantClassInfo) p).getStringIndex();
        } else {
            return;
        }
        int i = nameIdx.value - 1;
        if (i < 0 || i >= poolList.size()) {
            return;
        }
        AbstractConstantPool utf = poolList.get(i);
        if (utf instanceof Utf8Info) {
            hostClassInternalName = ((Utf8Info) utf).getValue();
        }
    }

    public U2 getHostClassIndex() {
        return hostClassIndex;
    }

    public String getHostClassInternalName() {
        return hostClassInternalName;
    }
}