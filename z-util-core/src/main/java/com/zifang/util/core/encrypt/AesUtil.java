package com.zifang.util.core.encrypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密工具类。
 * <p>
 * 以口令作为 SHA1PRNG 随机源种子派生密钥，同一口令派生出确定的密钥，
 * 密文以十六进制字符串（大写）编码，适合口令对称加解密场景。
 */
public final class AesUtil {

    /**
     * 密钥位数
     */
    private static final int KEY_SIZE = 256;

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * CBC 模式变换（AES/CBC/PKCS5Padding）
     */
    private static final String CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * 密钥派生随机源算法
     */
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * 十六进制字符表（大写）
     */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private AesUtil() {
    }

    /**
     * encrypt方法。
     * AES 加密字节数组。
     *
     * @param data     byte[]类型参数，待加密数据
     * @param password String类型参数，派生密钥的口令
     * @return static byte[]类型返回值，密文字节数组
     * @throws RuntimeException 加密失败或口令为 null 时抛出
     */
    public static byte[] encrypt(byte[] data, String password) {
        if (data == null || password == null) {
            throw new IllegalArgumentException("data and password must not be null");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt error", e);
        }
    }

    /**
     * decrypt方法。
     * AES 解密字节数组。
     *
     * @param data     byte[]类型参数，待解密密文
     * @param password String类型参数，派生密钥的口令
     * @return static byte[]类型返回值，明文字节数组
     * @throws RuntimeException 解密失败（口令不一致、密文损坏等）或口令为 null 时抛出
     */
    public static byte[] decrypt(byte[] data, String password) {
        if (data == null || password == null) {
            throw new IllegalArgumentException("data and password must not be null");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(password));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt error", e);
        }
    }

    /**
     * encryptToString方法。
     * AES 加密字符串，密文以十六进制（大写）编码。
     *
     * @param content  String类型参数，明文
     * @param password String类型参数，派生密钥的口令
     * @return static String类型返回值，十六进制密文字符串
     */
    public static String encryptToString(String content, String password) {
        return encodeHex(encrypt(content.getBytes(StandardCharsets.UTF_8), password));
    }

    /**
     * decryptToString方法。
     * AES 解密十六进制编码的密文字符串。
     *
     * @param content  String类型参数，十六进制密文字符串（大小写均可）
     * @param password String类型参数，派生密钥的口令
     * @return static String类型返回值，明文（UTF-8）
     */
    public static String decryptToString(String content, String password) {
        return new String(decrypt(decodeHex(content), password), StandardCharsets.UTF_8);
    }

    /**
     * generateKey方法。
     * 生成随机 AES 密钥的字节形式。
     *
     * @param keySize int类型参数，密钥位数，仅允许 128/192/256
     * @return static byte[]类型返回值，密钥字节（长度为 16/24/32）
     * @throws RuntimeException 密钥生成失败时抛出
     */
    public static byte[] generateKey(int keySize) {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new IllegalArgumentException("keySize must be 128, 192 or 256");
        }
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keySize);
            return keyGenerator.generateKey().getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("AES generate key error", e);
        }
    }

    /**
     * encryptCbc方法。
     * AES/CBC/PKCS5Padding 加密，使用显式密钥字节与初始向量。
     *
     * @param data byte[]类型参数，待加密数据
     * @param key  byte[]类型参数，密钥字节（长度 16/24/32）
     * @param iv   byte[]类型参数，初始向量（长度 16）
     * @return static byte[]类型返回值，密文字节数组
     * @throws RuntimeException 加密失败时抛出
     */
    public static byte[] encryptCbc(byte[] data, byte[] key, byte[] iv) {
        return doCbc(Cipher.ENCRYPT_MODE, data, key, iv);
    }

    /**
     * decryptCbc方法。
     * AES/CBC/PKCS5Padding 解密，使用显式密钥字节与初始向量。
     *
     * @param data byte[]类型参数，待解密密文
     * @param key  byte[]类型参数，密钥字节（长度 16/24/32）
     * @param iv   byte[]类型参数，初始向量（长度 16）
     * @return static byte[]类型返回值，明文字节数组
     * @throws RuntimeException 解密失败（密钥/初始向量不一致、密文损坏等）时抛出
     */
    public static byte[] decryptCbc(byte[] data, byte[] key, byte[] iv) {
        return doCbc(Cipher.DECRYPT_MODE, data, key, iv);
    }

    /**
     * encryptCbcToBase64方法。
     * AES/CBC/PKCS5Padding 加密字符串，密文以 Base64 编码。
     *
     * @param plainText String类型参数，明文
     * @param key       byte[]类型参数，密钥字节（长度 16/24/32）
     * @param iv        byte[]类型参数，初始向量（长度 16）
     * @return static String类型返回值，Base64 密文字符串
     */
    public static String encryptCbcToBase64(String plainText, byte[] key, byte[] iv) {
        return Base64.getEncoder().encodeToString(encryptCbc(plainText.getBytes(StandardCharsets.UTF_8), key, iv));
    }

    /**
     * decryptCbcFromBase64方法。
     * AES/CBC/PKCS5Padding 解密 Base64 编码的密文字符串。
     *
     * @param cipherText String类型参数，Base64 密文字符串
     * @param key        byte[]类型参数，密钥字节（长度 16/24/32）
     * @param iv         byte[]类型参数，初始向量（长度 16）
     * @return static String类型返回值，明文（UTF-8）
     */
    public static String decryptCbcFromBase64(String cipherText, byte[] key, byte[] iv) {
        return new String(decryptCbc(Base64.getDecoder().decode(cipherText), key, iv), StandardCharsets.UTF_8);
    }

    /**
     * doCbc方法。
     * CBC 模式加解密共用实现。
     */
    private static byte[] doCbc(int cipherMode, byte[] data, byte[] key, byte[] iv) {
        if (data == null || key == null || iv == null) {
            throw new IllegalArgumentException("data, key and iv must not be null");
        }
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("key length must be 16, 24 or 32 bytes");
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("iv length must be 16 bytes");
        }
        try {
            Cipher cipher = Cipher.getInstance(CBC_TRANSFORMATION);
            cipher.init(cipherMode, new SecretKeySpec(key, ALGORITHM), new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES cbc cipher error", e);
        }
    }

    /**
     * deriveKey方法。
     * 以口令作为 SHA1PRNG 种子派生 AES 密钥。
     */
    private static SecretKeySpec deriveKey(String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance(RANDOM_ALGORITHM);
        secureRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
    }

    /**
     * encodeHex方法。
     * 字节数组编码为十六进制字符串（大写）。
     */
    private static String encodeHex(byte[] data) {
        char[] out = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xFF;
            out[i * 2] = HEX_CHARS[value >>> 4];
            out[i * 2 + 1] = HEX_CHARS[value & 0x0F];
        }
        return new String(out);
    }

    /**
     * decodeHex方法。
     * 十六进制字符串解码为字节数组（兼容大小写）。
     */
    private static byte[] decodeHex(String hex) {
        if (hex == null || hex.length() == 0) {
            throw new IllegalArgumentException("hex string must not be empty");
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("hex string length must be even: " + hex);
        }
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int high = Character.digit(hex.charAt(i * 2), 16);
            int low = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("illegal hex string: " + hex);
            }
            out[i] = (byte) ((high << 4) | low);
        }
        return out;
    }
}
