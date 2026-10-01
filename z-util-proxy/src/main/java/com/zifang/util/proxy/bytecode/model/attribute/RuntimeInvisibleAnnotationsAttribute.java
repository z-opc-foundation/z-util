package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.io.IOException;
import java.io.InputStream;

/**
 * RuntimeInvisibleAnnotations 属性（4.7.16）：运行时不可见的注解容器，
 * 结构与 {@link RuntimeVisibleAnnotationsAttribute} 一致但语义不同。
 */
public class RuntimeInvisibleAnnotationsAttribute extends AbstractAttribute {

    private byte[] rawBody;

    public RuntimeInvisibleAnnotationsAttribute(U2 attributeNameIndex, U4 attributeLength) {
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
            } catch (IOException e) {
                throw new RuntimeException("读取 RuntimeInvisibleAnnotations 失败", e);
            }
            if (n < 0) {
                throw new RuntimeException("EOF reading RuntimeInvisibleAnnotations body");
            }
            r += n;
        }
        rawBody = buf;
    }

    public byte[] getRawBody() {
        return rawBody;
    }
}