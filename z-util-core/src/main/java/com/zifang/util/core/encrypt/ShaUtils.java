package com.zifang.util.core.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA 摘要工具类。
 * <p>
 * 基于 JDK MessageDigest 计算字符串的消息摘要，输出十六进制字符串（默认小写）；
 * 提供 SHA-1 与 SHA-256 便捷方法，也可通过 {@link #digest(String, String, boolean)}
 * 指定任意 JDK 支持的摘要算法（如 SHA-512、MD5）。
 */
public final class ShaUtils {

    /**
     * SHA-1 算法名
     */
    public static final String SHA_1 = "SHA-1";

    /**
     * SHA-256 算法名
     */
    public static final String SHA_256 = "SHA-256";

    /**
     * 十六进制字符表（小写）
     */
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private ShaUtils() {
    }

    /**
     * 计算 SHA-1 摘要，返回小写十六进制字符串。
     *
     * @param text 原文字符串，为 null 时返回 null
     * @return 40 位十六进制摘要
     */
    public static String sha1(String text) {
        return sha1(text, false);
    }

    /**
     * 计算 SHA-1 摘要，返回十六进制字符串。
     *
     * @param text     原文字符串，为 null 时返回 null
     * @param uppercase true 输出大写十六进制，false 输出小写
     * @return 40 位十六进制摘要
     */
    public static String sha1(String text, boolean uppercase) {
        return digest(text, SHA_1, uppercase);
    }

    /**
     * 计算 SHA-256 摘要，返回小写十六进制字符串。
     *
     * @param text 原文字符串，为 null 时返回 null
     * @return 64 位十六进制摘要
     */
    public static String sha256(String text) {
        return sha256(text, false);
    }

    /**
     * 计算 SHA-256 摘要，返回十六进制字符串。
     *
     * @param text     原文字符串，为 null 时返回 null
     * @param uppercase true 输出大写十六进制，false 输出小写
     * @return 64 位十六进制摘要
     */
    public static String sha256(String text, boolean uppercase) {
        return digest(text, SHA_256, uppercase);
    }

    /**
     * 按指定算法计算字符串消息摘要，返回十六进制字符串。
     *
     * @param text      原文字符串，为 null 时返回 null
     * @param algorithm 摘要算法名，如 SHA-1、SHA-256、SHA-512、MD5
     * @param uppercase true 输出大写十六进制，false 输出小写
     * @return 十六进制摘要字符串
     * @throws IllegalArgumentException 算法不受当前 JDK 支持时抛出
     */
    public static String digest(String text, String algorithm, boolean uppercase) {
        if (text == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            String hex = encodeHex(bytes);
            return uppercase ? hex.toUpperCase() : hex;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unsupported digest algorithm: " + algorithm, e);
        }
    }

    /**
     * 字节数组转小写十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串，每个字节对应两个字符
     */
    private static String encodeHex(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            result[index++] = HEX_CHARS[(b >>> 4) & 0x0f];
            result[index++] = HEX_CHARS[b & 0x0f];
        }
        return new String(result);
    }
}
