package io.zifu.z.serialize.core.crypto;

import java.io.IOException;

/**
 * 加密器接口。
 *
 * <p>实现包括：</p>
 * <ul>
 *   <li>{@link AesGcmEncryptor} - AES-GCM 认证加密（JDK 内置）</li>
 * </ul>
 */
public interface Encryptor {

    /**
     * 加密数据。
     *
     * @param plaintext 明文字节
     * @return 密文字节（包含 IV + 密文 + tag）
     */
    byte[] encrypt(byte[] plaintext) throws IOException;

    /**
     * 解密数据。
     *
     * @param ciphertext 密文字节（包含 IV + 密文 + tag）
     * @return 明文字节
     */
    byte[] decrypt(byte[] ciphertext) throws IOException;

    /** 加密算法名称。 */
    String name();
}
