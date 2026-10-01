package com.zifang.util.bc.bytecode.model.attribute;

import com.zifang.util.bc.bytecode.model.readtype.U2;
import com.zifang.util.bc.bytecode.model.readtype.U4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * StackMapTable 属性（4.7.4）：类型校验所需的栈帧描述，结构复杂。
 * <p>
 * 当前只保证属性字节被消费并保留原始字节以供后续深度解析，不解释 frame 数组。
 */
public class StackMapTableAttribute extends AbstractAttribute {

    private byte[] rawBody;

    public StackMapTableAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        // JVM 4.7.4：body = number_of_entries u2 + entries（每个 frame 类型码 + 可变 payload）。
        // 当前不解析 entry，按 attribute_length 整段吞为原始字节，避免流错位。
        long remaining = ((long) getAttributeLength().getValue()) & 0xFFFFFFFFL;
        if (remaining > Integer.MAX_VALUE) {
            throw new RuntimeException("StackMapTable 过大无法一次性缓冲: " + remaining);
        }
        byte[] buf = new byte[(int) remaining];
        int r = 0;
        while (r < buf.length) {
            int n;
            try {
                n = inputStream.read(buf, r, buf.length - r);
            } catch (java.io.IOException e) {
                throw new RuntimeException("读取 StackMapTable 失败", e);
            }
            if (n < 0) {
                throw new RuntimeException("EOF reading StackMapTable body");
            }
            r += n;
        }
        rawBody = buf;
    }

    public byte[] getRawBody() {
        return rawBody;
    }
}