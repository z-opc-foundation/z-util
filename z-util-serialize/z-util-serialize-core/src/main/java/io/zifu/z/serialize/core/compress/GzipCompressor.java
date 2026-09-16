package io.zifu.z.serialize.core.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip 压缩器（JDK 内置，零外部依赖）。
 *
 * <p>压缩率中等，速度中等。适合通用场景。
 * 生产环境建议替换为 Zstd（更高压缩率 + 更快速度）。</p>
 */
public final class GzipCompressor implements Compressor {

    public static final GzipCompressor INSTANCE = new GzipCompressor();

    private GzipCompressor() {}

    @Override
    public byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length / 2);
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length * 2);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = gzip.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
        }
        return baos.toByteArray();
    }

    @Override
    public String name() {
        return "gzip";
    }
}
