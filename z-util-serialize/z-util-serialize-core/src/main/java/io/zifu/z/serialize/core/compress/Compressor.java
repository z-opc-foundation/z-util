package io.zifu.z.serialize.core.compress;

import java.io.IOException;

/**
 * 压缩器接口。
 *
 * <p>实现包括：</p>
 * <ul>
 *   <li>{@link GzipCompressor} - JDK 内置 gzip（无外部依赖）</li>
 * </ul>
 *
 * <p>可通过 SPI 或手动注册添加 Zstd/LZ4 实现。</p>
 */
public interface Compressor {

    /** 压缩字节数组。 */
    byte[] compress(byte[] data) throws IOException;

    /** 解压字节数组。 */
    byte[] decompress(byte[] data) throws IOException;

    /** 压缩算法名称（用于日志和调试）。 */
    String name();
}
