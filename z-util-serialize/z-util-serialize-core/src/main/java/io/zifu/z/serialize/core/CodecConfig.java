package io.zifu.z.serialize.core;

import io.zifu.z.serialize.core.compress.Compressor;
import io.zifu.z.serialize.core.crypto.Encryptor;

/**
 * 序列化配置：压缩、加密选项。
 *
 * <p>通过 {@link ZSerializer#useConfig(CodecConfig)} 注入。</p>
 *
 * <pre>{@code
 * CodecConfig config = CodecConfig.builder()
 *     .compressor(GzipCompressor.INSTANCE)
 *     .encryptor(new AesGcmEncryptor(key))
 *     .build();
 *
 * ZSerializer.INSTANCE.useConfig(config);
 * }</pre>
 */
public final class CodecConfig {

    private final Compressor compressor;
    private final Encryptor encryptor;

    private CodecConfig(Compressor compressor, Encryptor encryptor) {
        this.compressor = compressor;
        this.encryptor = encryptor;
    }

    public Compressor getCompressor() { return compressor; }
    public Encryptor getEncryptor() { return encryptor; }

    public boolean isCompress() { return compressor != null; }
    public boolean isEncrypt() { return encryptor != null; }

    /** 默认配置（无压缩、无加密）。 */
    public static final CodecConfig DEFAULT = new CodecConfig(null, null);

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Compressor compressor;
        private Encryptor encryptor;

        public Builder compressor(Compressor compressor) {
            this.compressor = compressor;
            return this;
        }

        public Builder encryptor(Encryptor encryptor) {
            this.encryptor = encryptor;
            return this;
        }

        public CodecConfig build() {
            return new CodecConfig(compressor, encryptor);
        }
    }
}
