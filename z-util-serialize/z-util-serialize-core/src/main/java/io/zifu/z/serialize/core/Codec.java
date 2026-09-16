package io.zifu.z.serialize.core;

import java.io.IOException;

/**
 * 通用编解码器接口。
 *
 * <p>实现包括：</p>
 * <ul>
 *   <li>{@link ReflectCodec} - 基于反射 + SchemaRegistry（动态，无需 APT）</li>
 *   <li>{@code GeneratedCodec} - 由 z-util-serialize-codegen 在编译期生成（零反射，3~10x）</li>
 * </ul>
 *
 * <p>所有实现必须遵守相同的 wire format，详见 {@code docs/wire-format.md}。</p>
 */
public interface Codec {

    /**
     * 将 message 写入到 {@link ZOutput}。
     * <p>调用方负责写入 header（{@link HeaderWriter}）。</p>
     */
    void write(ZOutput out, Object message) throws IOException;

    /**
     * 从 {@link ZInput} 读取 message（调用方已读完 header）。
     */
    <T> T read(ZInput in, Class<T> messageClass) throws IOException;
}
