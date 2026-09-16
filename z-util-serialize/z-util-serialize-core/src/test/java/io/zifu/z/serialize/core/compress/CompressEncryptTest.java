package io.zifu.z.serialize.core.compress;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import io.zifu.z.serialize.core.CodecConfig;
import io.zifu.z.serialize.core.ZDeserializer;
import io.zifu.z.serialize.core.ZSerializer;
import io.zifu.z.serialize.core.crypto.AesGcmEncryptor;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * 测试压缩 + 加密功能。
 */
public class CompressEncryptTest {

    @ZMessage(id = 800, name = "test.BigMessage")
    public static class BigMessage {
        @ZField(id = 1, type = FieldType.VARINT)
        public long id;

        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
        public String payload;

        @ZField(id = 3, type = FieldType.VARINT)
        public int count;

        public BigMessage() {}
    }

    @Test
    public void testGzipCompressDecompress() throws IOException {
        BigMessage msg = new BigMessage();
        msg.id = 1L;
        msg.payload = "Hello World! ".repeat(100);  // 重复字符串以获得压缩效果
        msg.count = 42;

        CodecConfig config = CodecConfig.builder()
                .compressor(GzipCompressor.INSTANCE)
                .build();

        ZSerializer ser = new ZSerializer();
        ser.useConfig(config);
        byte[] compressed = ser.toBytes(msg);

        ZDeserializer deser = new ZDeserializer();
        deser.useConfig(config);
        BigMessage loaded = deser.fromBytes(compressed, BigMessage.class);

        assertEquals(msg.id, loaded.id);
        assertEquals(msg.payload, loaded.payload);
        assertEquals(msg.count, loaded.count);

        // 验证压缩有效
        ZSerializer noCompress = new ZSerializer();
        byte[] uncompressed = noCompress.toBytes(msg);
        assertTrue("Compressed should be smaller than uncompressed",
                compressed.length < uncompressed.length);
        System.out.printf("Compression: %d -> %d bytes (%.1f%% reduction)%n",
                uncompressed.length, compressed.length,
                (1 - (double) compressed.length / uncompressed.length) * 100);
    }

    @Test
    public void testAesGcmEncryptDecrypt() throws IOException {
        BigMessage msg = new BigMessage();
        msg.id = 2L;
        msg.payload = "Secret data";
        msg.count = 10;

        SecretKey key = AesGcmEncryptor.generateKey();
        CodecConfig config = CodecConfig.builder()
                .encryptor(new AesGcmEncryptor(key))
                .build();

        ZSerializer ser = new ZSerializer();
        ser.useConfig(config);
        byte[] encrypted = ser.toBytes(msg);

        ZDeserializer deser = new ZDeserializer();
        deser.useConfig(config);
        BigMessage loaded = deser.fromBytes(encrypted, BigMessage.class);

        assertEquals(msg.id, loaded.id);
        assertEquals(msg.payload, loaded.payload);
        assertEquals(msg.count, loaded.count);
    }

    @Test
    public void testCompressAndEncrypt() throws IOException {
        BigMessage msg = new BigMessage();
        msg.id = 3L;
        msg.payload = "Compressed + Encrypted data! ".repeat(50);
        msg.count = 99;

        SecretKey key = AesGcmEncryptor.generateKey();
        CodecConfig config = CodecConfig.builder()
                .compressor(GzipCompressor.INSTANCE)
                .encryptor(new AesGcmEncryptor(key))
                .build();

        ZSerializer ser = new ZSerializer();
        ser.useConfig(config);
        byte[] result = ser.toBytes(msg);

        ZDeserializer deser = new ZDeserializer();
        deser.useConfig(config);
        BigMessage loaded = deser.fromBytes(result, BigMessage.class);

        assertEquals(msg.id, loaded.id);
        assertEquals(msg.payload, loaded.payload);
        assertEquals(msg.count, loaded.count);
    }

    @Test(expected = IOException.class)
    public void testWrongKeyFails() throws IOException {
        BigMessage msg = new BigMessage();
        msg.id = 4L;
        msg.payload = "Encrypted";
        msg.count = 1;

        SecretKey key1 = AesGcmEncryptor.generateKey();
        SecretKey key2 = AesGcmEncryptor.generateKey();

        // 用 key1 加密
        CodecConfig encryptConfig = CodecConfig.builder()
                .encryptor(new AesGcmEncryptor(key1))
                .build();
        byte[] encrypted = new ZSerializer().useConfig(encryptConfig).toBytes(msg);

        // 用 key2 解密 → 应该失败
        CodecConfig decryptConfig = CodecConfig.builder()
                .encryptor(new AesGcmEncryptor(key2))
                .build();
        new ZDeserializer().useConfig(decryptConfig).fromBytes(encrypted, BigMessage.class);
    }
}
