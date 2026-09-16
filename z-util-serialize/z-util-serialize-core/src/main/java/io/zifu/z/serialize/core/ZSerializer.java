package io.zifu.z.serialize.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 顶层序列化 API：将 Java 对象编码为 wire format 字节数组。
 *
 * <pre>{@code
 * byte[] bytes = ZSerializer.INSTANCE.toBytes(u);   // 序列化
 * User u2 = ZDeserializer.INSTANCE.fromBytes(bytes, User.class);  // 反序列化
 *
 * // 带压缩/加密
 * CodecConfig config = CodecConfig.builder()
 *     .compressor(GzipCompressor.INSTANCE)
 *     .encryptor(new AesGcmEncryptor(key))
 *     .build();
 * ZSerializer.INSTANCE.useConfig(config);
 * }</pre>
 *
 * <p>默认使用 {@link ReflectCodec}，无状态线程安全。</p>
 */
public final class ZSerializer {

    public static final ZSerializer INSTANCE = new ZSerializer();

    private volatile Codec codec = ReflectCodec.INSTANCE;
    private volatile CodecConfig config = CodecConfig.DEFAULT;

    public ZSerializer() {}

    /** 注入预编译 codec（codegen 生成）。 */
    public ZSerializer useCodec(Codec codec) {
        if (codec == null) throw new IllegalArgumentException("codec");
        this.codec = codec;
        return this;
    }

    /** 注入压缩/加密配置。 */
    public ZSerializer useConfig(CodecConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
        return this;
    }

    /** 获取当前配置。 */
    public CodecConfig getConfig() { return config; }

    /** 序列化为字节数组（包含 header + body）。 */
    public byte[] toBytes(Object message) throws IOException {
        SchemaRegistry.SchemaDescriptor schema = SchemaRegistry.get(message.getClass());

        // 1. 编码 body 到 buffer
        ByteArrayOutputStream bodyOut = new ByteArrayOutputStream(128);
        ZOutput zout = new ZOutput(bodyOut);
        codec.write(zout, message);

        // 2. 计算 flags（schema 注解 + 运行时配置）
        byte flags = 0;
        boolean doCompress = schema.isCompressed() || config.isCompress();
        boolean doEncrypt = schema.isEncrypted() || config.isEncrypt();
        if (doCompress) flags |= Magic.FLAG_COMPRESSED;
        if (doEncrypt) flags |= Magic.FLAG_ENCRYPTED;
        if (!"none".equals(schema.getIndex())) flags |= Magic.FLAG_INDEXED;

        // 3. 处理 body：压缩 -> 加密
        byte[] body = bodyOut.toByteArray();
        if (doCompress && config.getCompressor() != null) {
            body = config.getCompressor().compress(body);
        }
        if (doEncrypt && config.getEncryptor() != null) {
            body = config.getEncryptor().encrypt(body);
        }

        // 4. 写入 header + body
        ByteArrayOutputStream out = new ByteArrayOutputStream(Magic.HEADER_FIXED_LEN + 8 + body.length);
        Header.writeHeader(out, schema.getId(), flags, body);
        return out.toByteArray();
    }

    /** 编码 body（不含 header），用于流式协议嵌入。 */
    public byte[] encodeBody(Object message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        codec.write(new ZOutput(baos), message);
        return baos.toByteArray();
    }
}
