package io.zifu.z.serialize.core.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * AES-GCM 认证加密器（JDK 内置，零外部依赖）。
 * 密文格式：[IV 12B][ciphertext][tag 16B]
 */
public final class AesGcmEncryptor implements Encryptor {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey key;

    public AesGcmEncryptor(SecretKey key) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        this.key = key;
    }

    public AesGcmEncryptor(byte[] keyBytes) {
        this(new SecretKeySpec(keyBytes, "AES"));
    }

    public static SecretKey generateKey() throws IOException {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256, new SecureRandom());
            return kg.generateKey();
        } catch (Exception e) {
            throw new IOException("Failed to generate AES key", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws IOException {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new IOException("AES-GCM encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws IOException {
        try {
            if (ciphertext.length < GCM_IV_LENGTH + 16) {
                throw new IOException("Ciphertext too short");
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] enc = new byte[ciphertext.length - GCM_IV_LENGTH];
            System.arraycopy(ciphertext, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, GCM_IV_LENGTH, enc, 0, enc.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(enc);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("AES-GCM decryption failed", e);
        }
    }

    @Override
    public String name() { return "aes-gcm"; }
}
