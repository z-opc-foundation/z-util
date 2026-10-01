package com.zifang.util.bc.bytecode.model.attribute;

import com.zifang.util.bc.bytecode.model.readtype.U2;
import com.zifang.util.bc.bytecode.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LocalVariableTypeTable 属性（4.7.13）：泛型版 LocalVariableTable，
 * signature_index 替代 descriptor_index，用以保留泛型擦除前类型。
 * <p>
 * 结构与 LocalVariableTable 完全一致，本类复用其内嵌 {@code LocalVariableInfo} 模型。
 */
public class LocalVariableTypeTableAttribute extends AbstractAttribute {

    public static final class LocalVariableTypeInfo {
        public U2 startPc;
        public U2 length;
        public U2 nameIndex;
        public U2 signatureIndex;
        public U2 slot;

        public LocalVariableTypeInfo(U2 startPc, U2 length, U2 nameIndex, U2 signatureIndex, U2 slot) {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.slot = slot;
        }
    }

    private final List<LocalVariableTypeInfo> entries = new ArrayList<>();

    public LocalVariableTypeTableAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        U2 count = U2.read(inputStream);
        for (int i = 0; i < count.value; i++) {
            entries.add(new LocalVariableTypeInfo(
                    U2.read(inputStream), U2.read(inputStream),
                    U2.read(inputStream), U2.read(inputStream), U2.read(inputStream)));
        }
    }

    public List<LocalVariableTypeInfo> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}