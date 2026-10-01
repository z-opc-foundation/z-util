package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NestMembers 属性（4.7.29）：宿主类列出自己的 nestmate 类。
 */
public class NestMembersAttribute extends AbstractAttribute {

    private final List<U2> classIndices = new ArrayList<>();

    public NestMembersAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        U2 count = U2.read(inputStream);
        for (int i = 0; i < count.value; i++) {
            classIndices.add(U2.read(inputStream));
        }
    }

    public List<U2> getClassIndices() {
        return Collections.unmodifiableList(classIndices);
    }
}