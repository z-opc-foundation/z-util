package com.zifang.util.bc.bytecode.model.attribute;

import com.zifang.util.bc.bytecode.model.readtype.U2;
import com.zifang.util.bc.bytecode.model.readtype.U4;

import java.io.IOException;
import java.io.InputStream;

/**
 * RuntimeVisibleAnnotations 属性（4.7.16）：当前结构含注解。
 * <p>
 * 仅消费字节，不解析嵌套的 element_value 树（保留原始字节供需要时展开）。
 */
public class RuntimeVisibleAnnotationsAttribute extends AbstractAttribute {

    private byte[] rawBody;

    public RuntimeVisibleAnnotationsAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        U2 numAnnotations = U2.read(inputStream);
        long remaining = (long) getAttributeLength().getValue() - 2;
        byte[] buf = new byte[(int) remaining];
        int r = 0;
        while (r < buf.length) {
            int n;
            try {
                n = inputStream.read(buf, r, buf.length - r);
            } catch (java.io.IOException e) {
                throw new RuntimeException("读取 RuntimeVisibleAnnotations 失败", e);
            }
            if (n < 0) {
                throw new RuntimeException("EOF reading RuntimeVisibleAnnotations body");
            }
            r += n;
        }
        rawBody = buf;
    }

    public int getNumAnnotations() {
        return rawBody == null ? 0 : -1;
    }

    public byte[] getRawBody() {
        return rawBody;
    }
}