package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.bytecode.model.constantpool.Utf8Info;
import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.util.List;

/**
 * SourceFile 属性（4.7.7）：记录 .class 对应的源文件名（javac 默认会加）。
 */
public class SourceFileAttribute extends AbstractAttribute {

    private U2 sourceFileIndex;
    private String sourceFileName;

    public SourceFileAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(java.io.InputStream inputStream) {
        sourceFileIndex = U2.read(inputStream);
    }

    public void resolve(List<AbstractConstantPool> poolList) {
        if (sourceFileIndex == null) {
            return;
        }
        int i = sourceFileIndex.value - 1;
        if (i < 0 || i >= poolList.size()) {
            return;
        }
        AbstractConstantPool p = poolList.get(i);
        if (p instanceof Utf8Info) {
            sourceFileName = ((Utf8Info) p).getValue();
        }
    }

    public U2 getSourceFileIndex() {
        return sourceFileIndex;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }
}