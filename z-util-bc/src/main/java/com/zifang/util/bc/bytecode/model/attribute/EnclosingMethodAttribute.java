package com.zifang.util.bc.bytecode.model.attribute;

import com.zifang.util.bc.bytecode.model.constantpool.AbstractConstantPool;
import com.zifang.util.bc.bytecode.model.constantpool.ClassInfo;
import com.zifang.util.bc.bytecode.model.constantpool.ConstantClassInfo;
import com.zifang.util.bc.bytecode.model.constantpool.ConstantNameAndTypeInfo;
import com.zifang.util.bc.bytecode.model.constantpool.Utf8Info;
import com.zifang.util.bc.bytecode.model.readtype.U2;
import com.zifang.util.bc.bytecode.model.readtype.U4;

import java.util.List;

/**
 * EnclosingMethod 属性（4.7.7）：局部类/匿名类的宿主方法定位（用于反射恢复外部实例）。
 */
public class EnclosingMethodAttribute extends AbstractAttribute {

    private U2 classIndex;
    private U2 methodIndex;
    private String enclosingClassInternal;
    private String enclosingMethodName;
    private String enclosingMethodDescriptor;

    public EnclosingMethodAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(java.io.InputStream inputStream) {
        classIndex = U2.read(inputStream);
        methodIndex = U2.read(inputStream);
    }

    public void resolve(List<AbstractConstantPool> poolList) {
        if (classIndex == null) {
            return;
        }
        int ci = classIndex.value - 1;
        if (ci < 0 || ci >= poolList.size()) {
            return;
        }
        AbstractConstantPool p = poolList.get(ci);
        U2 nameIdx;
        if (p instanceof ClassInfo) {
            nameIdx = ((ClassInfo) p).getNameIndex();
        } else if (p instanceof ConstantClassInfo) {
            nameIdx = ((ConstantClassInfo) p).getStringIndex();
        } else {
            return;
        }
        int ni = nameIdx.value - 1;
        if (ni < 0 || ni >= poolList.size()) {
            return;
        }
        AbstractConstantPool utf = poolList.get(ni);
        if (utf instanceof Utf8Info) {
            enclosingClassInternal = ((Utf8Info) utf).getValue();
        }
        int mi = methodIndex.value;
        if (mi == 0 || mi >= poolList.size()) {
            return;
        }
        AbstractConstantPool nt = poolList.get(mi - 1);
        if (nt instanceof ConstantNameAndTypeInfo) {
            int nameIdx2 = ((ConstantNameAndTypeInfo) nt).getNameIndex().value - 1;
            int descIdx2 = ((ConstantNameAndTypeInfo) nt).getDescriptorIndex().value - 1;
            if (nameIdx2 >= 0 && nameIdx2 < poolList.size()) {
                AbstractConstantPool utf2 = poolList.get(nameIdx2);
                if (utf2 instanceof Utf8Info) {
                    enclosingMethodName = ((Utf8Info) utf2).getValue();
                }
            }
            if (descIdx2 >= 0 && descIdx2 < poolList.size()) {
                AbstractConstantPool utf3 = poolList.get(descIdx2);
                if (utf3 instanceof Utf8Info) {
                    enclosingMethodDescriptor = ((Utf8Info) utf3).getValue();
                }
            }
        }
    }

    public U2 getClassIndex() {
        return classIndex;
    }

    public U2 getMethodIndex() {
        return methodIndex;
    }

    public String getEnclosingClassInternal() {
        return enclosingClassInternal;
    }

    public String getEnclosingMethodName() {
        return enclosingMethodName;
    }

    public String getEnclosingMethodDescriptor() {
        return enclosingMethodDescriptor;
    }
}