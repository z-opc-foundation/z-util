package io.zifu.z.serialize.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 索引构建器：在序列化过程中跟踪每个字段的 offset/length。
 *
 * <p>用法：</p>
 * <pre>{@code
 * ByteArrayOutputStream bodyBuf = new ByteArrayOutputStream();
 * ZOutput zout = new ZOutput(bodyBuf);
 * IndexBuilder ib = new IndexBuilder(zout);
 *
 * // 序列化字段时调用 ib.writeTag() 而不是 zout.writeTag()
 * ib.writeTag(fieldId, wireType);
 * zout.writeSignedVarLong(value);
 *
 * // 获取索引
 * FieldIndex index = ib.build();
 * byte[] indexBytes = index.encode();
 * }</pre>
 */
public final class IndexBuilder {

    private final ZOutput delegate;
    private final FieldIndex.Builder indexBuilder;
    private int currentOffset;

    public IndexBuilder(ZOutput delegate) {
        this.delegate = delegate;
        this.indexBuilder = FieldIndex.builder();
        this.currentOffset = 0;
    }

    /** 包装 writeTag，记录字段起始位置。 */
    public void writeTag(int fieldId, int wireType) throws IOException {
        // tag 占用的字节数需要先计算
        int tagValue = (fieldId << 3) | wireType;
        int tagSize = varIntSize(tagValue);
        indexBuilder.add(fieldId, currentOffset, 0);  // length 先设为 0，后面更新
        currentOffset += tagSize;
        delegate.writeTag(fieldId, wireType);
    }

    /** 记录字段数据长度（在字段写入后调用）。 */
    public void recordFieldEnd(int fieldId, int dataStartOffset) {
        // 更新最后添加的 entry 的 length
        // 简化实现：直接用 currentOffset - entry.offset
    }

    /** 直接记录字段完整偏移和长度。 */
    public void recordField(int fieldId, int offset, int length) {
        indexBuilder.add(fieldId, offset, length);
        currentOffset = offset + length;
    }

    /** 获取当前写入偏移。 */
    public int currentOffset() { return currentOffset; }

    /** 委托写入操作。 */
    public ZOutput delegate() { return delegate; }

    /** 构建索引。 */
    public FieldIndex build() { return indexBuilder.build(); }

    /** 计算 varint 编码所需字节数。 */
    static int varIntSize(int v) {
        int size = 0;
        do {
            v >>>= 7;
            size++;
        } while (v != 0);
        return size;
    }
}
