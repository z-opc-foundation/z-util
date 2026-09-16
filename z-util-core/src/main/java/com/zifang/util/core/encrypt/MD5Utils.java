package com.zifang.util.core.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * MD5 消息摘要工具类
 * <p>
 * 提供 MD5 加密功能，支持盐值(Salt)增强安全性，防止彩虹表攻击。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 基本加密
 * String hash = MD5Utils.encrypt("password");
 *
 * // 带盐值加密（推荐）
 * String salt = MD5Utils.generateSalt();
 * String hash = MD5Utils.encryptWithSalt("password", salt);
 *
 * // 验证密码
 * boolean match = MD5Utils.verify("password", hash, salt);
 * </pre>
 *
 * @author zifang
 * @since 1.0
 */
public class MD5Utils {

    /**
     * MD5 算法名称
     */
    private static final String ALGORITHM_MD5 = "MD5";

    /**
     * MD5 摘要的十六进制字符串长度
     */
    private static final int MD5_HEX_LENGTH = 32;

    /**
     * 默认盐值字节长度
     */
    private static final int DEFAULT_SALT_LENGTH = 16;

    private MD5Utils() {
        // 工具类禁止实例化
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 对字符串进行 MD5 加密
     * <p>
     * 注意：此方法适用于非敏感场景。敏感数据（如用户密码）建议使用 {@link #encryptWithSalt(String, String)} 带盐加密。
     * </p>
     *
     * @param value 待加密的字符串
     * @return MD5 十六进制小写字符串，若加密失败返回 null
     */
    public static String encrypt(String value) {
        if (value == null) {
            return null;
        }
        return hash(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对字节数组进行 MD5 加密
     *
     * @param inputByteArray 待加密的字节数组
     * @return MD5 十六进制小写字符串，若加密失败返回 null
     */
    public static String encrypt(byte[] inputByteArray) {
        if (inputByteArray == null) {
            return null;
        }
        return hash(inputByteArray);
    }

    /**
     * 使用指定盐值对字符串进行 MD5 加密
     * <p>
     * 将盐值与原始字符串拼接后再进行 MD5 加密，可有效防止彩虹表攻击。
     * </p>
     *
     * @param value 待加密的字符串
     * @param salt  盐值（建议长度为 16 字节或以上）
     * @return MD5 十六进制小写字符串，若加密失败返回 null
     */
    public static String encryptWithSalt(String value, String salt) {
        if (value == null || salt == null) {
            return null;
        }
        return hash((value + salt).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用指定盐值对字节数组进行 MD5 加密
     *
     * @param inputByteArray 待加密的字节数组
     * @param salt           盐值字节数组
     * @return MD5 十六进制小写字符串，若加密失败返回 null
     */
    public static String encryptWithSalt(byte[] inputByteArray, byte[] salt) {
        if (inputByteArray == null || salt == null) {
            return null;
        }
        byte[] combined = new byte[inputByteArray.length + salt.length];
        System.arraycopy(inputByteArray, 0, combined, 0, inputByteArray.length);
        System.arraycopy(salt, 0, combined, inputByteArray.length, salt.length);
        return hash(combined);
    }

    /**
     * 生成随机盐值
     * <p>
     * 使用 {@link SecureRandom} 生成 cryptographically secure 的随机盐值。
     * </p>
     *
     * @return Base64 编码的盐值字符串
     */
    public static String generateSalt() {
        return generateSalt(DEFAULT_SALT_LENGTH);
    }

    /**
     * 生成指定长度的随机盐值
     *
     * @param length 盐值字节长度
     * @return Base64 编码的盐值字符串
     */
    public static String generateSalt(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Salt length must be positive");
        }
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return Base64Utils.encrypt(salt);
    }

    /**
     * 生成原始字节形式的随机盐值
     *
     * @return 随机盐值字节数组
     */
    public static byte[] generateSaltAsBytes() {
        return generateSaltAsBytes(DEFAULT_SALT_LENGTH);
    }

    /**
     * 生成指定长度的原始字节盐值
     *
     * @param length 盐值字节长度
     * @return 随机盐值字节数组
     */
    public static byte[] generateSaltAsBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Salt length must be positive");
        }
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * 验证字符串是否与已加密的密文匹配
     * <p>
     * 支持两种格式的密文：
     * <ul>
     *   <li>纯 MD5 密文（32位十六进制）</li>
     *   <li>带盐值的密文（格式：{MD5}:{SALT}，其中 MD5 为32位十六进制，SALT 为 Base64 编码）</li>
     * </ul>
     * </p>
     *
     * @param plainText     待验证的原始字符串
     * @param encryptedText 已加密的密文
     * @param salt          盐值（当密文为纯 MD5 时使用）
     * @return 若匹配返回 true，否则返回 false
     */
    public static boolean verify(String plainText, String encryptedText, String salt) {
        if (plainText == null || encryptedText == null) {
            return false;
        }

        // 判断是否为带盐格式的密文
        if (encryptedText.contains(":")) {
            String[] parts = encryptedText.split(":", 2);
            if (parts.length == 2) {
                String storedHash = parts[0];
                String storedSalt = parts[1];
                String computedHash = encryptWithSalt(plainText, storedSalt);
                return constantTimeEquals(storedHash, computedHash);
            }
        }

        // 纯 MD5 密文验证（带指定盐值）
        if (salt != null) {
            String computedHash = encryptWithSalt(plainText, salt);
            return constantTimeEquals(encryptedText, computedHash);
        }

        // 纯 MD5 密文验证（无盐值）
        String computedHash = encrypt(plainText);
        return constantTimeEquals(encryptedText, computedHash);
    }

    /**
     * 验证字符串是否与已加密的密文匹配（盐值内嵌在密文中）
     * <p>
     * 密文格式：{MD5}:{SALT}
     * </p>
     *
     * @param plainText     待验证的原始字符串
     * @param encryptedText 已加密的密文（格式：{MD5}:{SALT}）
     * @return 若匹配返回 true，否则返回 false
     */
    public static boolean verifyWithEmbeddedSalt(String plainText, String encryptedText) {
        return verify(plainText, encryptedText, null);
    }

    /**
     * 将明文密码加密并返回带盐值的密文
     * <p>
     * 返回格式：{MD5}:{SALT}
     * </p>
     *
     * @param plainText 明文密码
     * @return 格式为 {MD5}:{SALT} 的密文
     */
    public static String encryptAndSalt(String plainText) {
        if (plainText == null) {
            return null;
        }
        String salt = generateSalt();
        String hash = encryptWithSalt(plainText, salt);
        return hash + ":" + salt;
    }

    /**
     * 执行 MD5 哈希计算
     *
     * @param data 输入数据
     * @return 十六进制小写字符串
     */
    private static String hash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_MD5);
            byte[] digest = md.digest(data);
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            // MD5 算法在所有 JDK 实现中均可用，此异常不会发生
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为十六进制小写字符串
     *
     * @param bytes 字节数组
     * @return 十六进制小写字符串
     */
    private static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = Character.forDigit(v >>> 4, 16);
            hexChars[i * 2 + 1] = Character.forDigit(v & 0x0F, 16);
        }
        return new String(hexChars);
    }

    /**
     * 常数时间比较两个字符串是否相等
     * <p>
     * 用于防止时序攻击(Timing Attack)。
     * </p>
     *
     * @param a 字符串A
     * @param b 字符串B
     * @return 若相等返回 true
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
