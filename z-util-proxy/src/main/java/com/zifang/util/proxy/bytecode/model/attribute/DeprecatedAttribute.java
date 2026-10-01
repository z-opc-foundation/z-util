package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

/**
 * Deprecated 属性（4.7.15）：仅作标记，attribute_length 通常为 0，没有额外字段要解析。
 */
public class DeprecatedAttribute extends AbstractAttribute {

    public DeprecatedAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(java.io.InputStream inputStream) {
        // 无 body 字节可读
    }
}