package io.zifu.z.serialize.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 顶层反序列化 API：从 wire format 字节解码 Java 对象。
 *
 * <p>自动处理压缩/解密（根据 header flags）。</p>
 */
public final class ZDeserializer {

    public static final ZDeserializer INSTANCE = new ZDeserializer();

    private volatile Codec codec = ReflectCodec.INSTANCE;
    private volatile CodecConfig config = CodecConfig.DEFAULT;

    public ZDeserializer() {}

    public ZDeserializer useCodec(Codec codec) {
        if (codec == null) throw new IllegalArgumentException("codec");
        this.codec = codec;
        return this;
    }

    /** 注入解压/解密配置（必须与序列化时的配置匹配）。 */
    public ZDeserializer useConfig(CodecConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
        return this;
    }

    /** 从完整字节数组反序列化。 */
    public <T> T fromBytes(byte[] data, Class<T> messageClass) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Header.HeaderInfo info = Header.readHeader(bais);

        // 读 body 字节
        byte[] body = new byte[info.bodyLen];
        int read = 0;
        while (read < info.bodyLen) {
            int n = bais.read(body, read, info.bodyLen - read);
            if (n < 0) throw new IOException("Unexpected EOF in body");
            read += n;
        }

        // 按 flag 解密 → 解压
        if (info.isEncrypted() && config.getEncryptor() != null) {
            body = config.getEncryptor().decrypt(body);
        }
        if (info.isCompressed() && config.getCompressor() != null) {
            body = config.getCompressor().decompress(body);
        }

        return codec.read(new ZInput(new ByteArrayInputStream(body)), messageClass);
    }

    /** 从 body 字节反序列化（不含 header）。 */
    public <T> T fromBody(byte[] body, Class<T> messageClass) throws IOException {
        return codec.read(new ZInput(new ByteArrayInputStream(body)), messageClass);
    }

    /** 从流反序列化（流起始为 header）。 */
    public <T> T fromStream(InputStream in, Class<T> messageClass) throws IOException {
        Header.HeaderInfo info = Header.readHeader(in);
        byte[] body = new byte[info.bodyLen];
        int read = 0;
        while (read < info.bodyLen) {
            int n = in.read(body, read, info.bodyLen - read);
            if (n < 0) throw new IOException("Unexpected EOF in body");
            read += n;
        }

        // 按 flag 解密 → 解压
        if (info.isEncrypted() && config.getEncryptor() != null) {
            body = config.getEncryptor().decrypt(body);
        }
        if (info.isCompressed() && config.getCompressor() != null) {
            body = config.getCompressor().decompress(body);
        }

        return codec.read(new ZInput(new ByteArrayInputStream(body)), messageClass);
    }
}
