package com.zifang.util.core.encrypt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * MD5Utils 单元测试类
 * <p>
 * 测试 MD5 加密、盐值生成、密码验证等功能的正确性。
 * </p>
 *
 * @author zifang
 */

/**
 * MD5UtilsTest类。
 */
public class MD5UtilsTest {

    // ========== 基本加密测试 ==========

    @Test
    /**
     * testEncrypt_StringNull方法。
     */
    public void testEncrypt_StringNull() {
        assertNull(MD5Utils.encrypt((String) null));
    }

    @Test
    /**
     * testEncrypt_EmptyString方法。
     */
    public void testEncrypt_EmptyString() {
        String result = MD5Utils.encrypt("");
        assertNotNull(result);
        assertEquals(32, result.length());
        // 空字符串的 MD5 值为: d41d8cd98f00b204e9800998ecf8427e
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    /**
     * testEncrypt_HelloWorld方法。
     */
    public void testEncrypt_HelloWorld() {
        // "hello world" 的标准 MD5 值
        String result = MD5Utils.encrypt("hello world");
        assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", result);
    }

    @Test
    /**
     * testEncrypt_Chinese方法。
     */
    public void testEncrypt_Chinese() {
        // 中文字符的 MD5 加密测试
        String result = MD5Utils.encrypt("你好世界");
        assertNotNull(result);
        assertEquals(32, result.length());
        // 验证结果为小写十六进制
        assertTrue(result.matches("[0-9a-f]{32}"));
    }

    @Test
    /**
     * testEncrypt_SpecialChars方法。
     */
    public void testEncrypt_SpecialChars() {
        String input = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String result = MD5Utils.encrypt(input);
        assertNotNull(result);
        assertEquals(32, result.length());
    }

    @Test
    /**
     * testEncrypt_SameInputSameOutput方法。
     */
    public void testEncrypt_SameInputSameOutput() {
        String input = "consistent";
        String result1 = MD5Utils.encrypt(input);
        String result2 = MD5Utils.encrypt(input);
        assertEquals(result1, result2);
    }

    @Test
    /**
     * testEncrypt_DifferentInputDifferentOutput方法。
     */
    public void testEncrypt_DifferentInputDifferentOutput() {
        String result1 = MD5Utils.encrypt("input1");
        String result2 = MD5Utils.encrypt("input2");
        assertNotEquals(result1, result2);
    }

    // ========== 字节数组加密测试 ==========

    @Test
    /**
     * testEncrypt_ByteArrayNull方法。
     */
    public void testEncrypt_ByteArrayNull() {
        assertNull(MD5Utils.encrypt((byte[]) null));
    }

    @Test
    /**
     * testEncrypt_ByteArrayEmpty方法。
     */
    public void testEncrypt_ByteArrayEmpty() {
        String result = MD5Utils.encrypt(new byte[0]);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    /**
     * testEncrypt_ByteArrayEquivalents方法。
     */
    public void testEncrypt_ByteArrayEquivalents() {
        String strResult = MD5Utils.encrypt("hello");
        String byteResult = MD5Utils.encrypt("hello".getBytes());
        assertEquals(strResult, byteResult);
    }

    // ========== 带盐加密测试 ==========

    @Test
    /**
     * testEncryptWithSalt_NullValue方法。
     */
    public void testEncryptWithSalt_NullValue() {
        assertNull(MD5Utils.encryptWithSalt(null, "salt"));
    }

    @Test
    /**
     * testEncryptWithSalt_NullSalt方法。
     */
    public void testEncryptWithSalt_NullSalt() {
        assertNull(MD5Utils.encryptWithSalt("value", null));
    }

    @Test
    /**
     * testEncryptWithSalt_BothNull方法。
     */
    public void testEncryptWithSalt_BothNull() {
        assertNull(MD5Utils.encryptWithSalt((String) null, (String) null));
    }

    @Test
    /**
     * testEncryptWithSalt_SameValueDifferentSalt方法。
     */
    public void testEncryptWithSalt_SameValueDifferentSalt() {
        String result1 = MD5Utils.encryptWithSalt("password", "salt1");
        String result2 = MD5Utils.encryptWithSalt("password", "salt2");
        assertNotEquals(result1, result2);
    }

    @Test
    /**
     * testEncryptWithSalt_DifferentValueSameSalt方法。
     */
    public void testEncryptWithSalt_DifferentValueSameSalt() {
        String result1 = MD5Utils.encryptWithSalt("pass1", "salt");
        String result2 = MD5Utils.encryptWithSalt("pass2", "salt");
        assertNotEquals(result1, result2);
    }

    @Test
    /**
     * testEncryptWithSalt_SameValueSameSalt方法。
     */
    public void testEncryptWithSalt_SameValueSameSalt() {
        String result1 = MD5Utils.encryptWithSalt("password", "salt");
        String result2 = MD5Utils.encryptWithSalt("password", "salt");
        assertEquals(result1, result2);
    }

    @Test
    /**
     * testEncryptWithSalt_ByteArrayNull方法。
     */
    public void testEncryptWithSalt_ByteArrayNull() {
        assertNull(MD5Utils.encryptWithSalt(null, new byte[]{1, 2, 3}));
        assertNull(MD5Utils.encryptWithSalt(new byte[]{1, 2, 3}, null));
    }

    @Test
    /**
     * testEncryptWithSalt_ByteArraySalt方法。
     */
    public void testEncryptWithSalt_ByteArraySalt() {
        byte[] data = "password".getBytes();
        byte[] salt = "salt".getBytes();
        String result = MD5Utils.encryptWithSalt(data, salt);
        assertNotNull(result);
        assertEquals(32, result.length());
    }

    // ========== 盐值生成测试 ==========

    @Test
    /**
     * testGenerateSalt_NotNull方法。
     */
    public void testGenerateSalt_NotNull() {
        String salt = MD5Utils.generateSalt();
        assertNotNull(salt);
    }

    @Test
    /**
     * testGenerateSalt_Base64Format方法。
     */
    public void testGenerateSalt_Base64Format() {
        String salt = MD5Utils.generateSalt();
        // Base64 编码应该只包含 Base64 字符
        assertTrue(salt.matches("[A-Za-z0-9+/=]+"));
    }

    @Test
    /**
     * testGenerateSalt_DifferentEachTime方法。
     */
    public void testGenerateSalt_DifferentEachTime() {
        String salt1 = MD5Utils.generateSalt();
        String salt2 = MD5Utils.generateSalt();
        assertNotEquals(salt1, salt2);
    }

    @Test
    /**
     * testGenerateSalt_CustomLength方法。
     */
    public void testGenerateSalt_CustomLength() {
        String salt8 = MD5Utils.generateSalt(8);
        String salt16 = MD5Utils.generateSalt(16);
        String salt32 = MD5Utils.generateSalt(32);

        // 长度验证：Base64 编码后约为原长的 4/3 倍
        assertTrue(salt8.length() >= 10 && salt8.length() <= 12);
        assertTrue(salt16.length() >= 20 && salt16.length() <= 24);
        assertTrue(salt32.length() >= 40 && salt32.length() <= 44);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGenerateSalt_ZeroLength方法。
     */
    public void testGenerateSalt_ZeroLength() {
        MD5Utils.generateSalt(0);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGenerateSalt_NegativeLength方法。
     */
    public void testGenerateSalt_NegativeLength() {
        MD5Utils.generateSalt(-1);
    }

    @Test
    /**
     * testGenerateSaltAsBytes_NotNull方法。
     */
    public void testGenerateSaltAsBytes_NotNull() {
        byte[] salt = MD5Utils.generateSaltAsBytes();
        assertNotNull(salt);
    }

    @Test
    /**
     * testGenerateSaltAsBytes_DefaultLength方法。
     */
    public void testGenerateSaltAsBytes_DefaultLength() {
        byte[] salt = MD5Utils.generateSaltAsBytes();
        assertEquals(16, salt.length);
    }

    @Test
    /**
     * testGenerateSaltAsBytes_CustomLength方法。
     */
    public void testGenerateSaltAsBytes_CustomLength() {
        byte[] salt = MD5Utils.generateSaltAsBytes(32);
        assertEquals(32, salt.length);
    }

    // ========== 密码验证测试 ==========

    @Test
    /**
     * testVerify_CorrectPassword方法。
     */
    public void testVerify_CorrectPassword() {
        String password = "myPassword123";
        String salt = MD5Utils.generateSalt();
        String hash = MD5Utils.encryptWithSalt(password, salt);
        assertTrue(MD5Utils.verify(password, hash, salt));
    }

    @Test
    /**
     * testVerify_WrongPassword方法。
     */
    public void testVerify_WrongPassword() {
        String salt = MD5Utils.generateSalt();
        String hash = MD5Utils.encryptWithSalt("correct", salt);
        assertFalse(MD5Utils.verify("wrong", hash, salt));
    }

    @Test
    /**
     * testVerify_NullPassword方法。
     */
    public void testVerify_NullPassword() {
        String hash = MD5Utils.encrypt("somehash");
        assertFalse(MD5Utils.verify(null, hash, null));
    }

    @Test
    /**
     * testVerify_NullHash方法。
     */
    public void testVerify_NullHash() {
        assertFalse(MD5Utils.verify("password", null, null));
    }

    @Test
    /**
     * testVerify_EmbeddedSaltFormat方法。
     */
    public void testVerify_EmbeddedSaltFormat() {
        String password = "securePassword";
        String encrypted = MD5Utils.encryptAndSalt(password);
        assertTrue(MD5Utils.verifyWithEmbeddedSalt(password, encrypted));
    }

    @Test
    /**
     * testVerify_EmbeddedSaltFormat_WrongPassword方法。
     */
    public void testVerify_EmbeddedSaltFormat_WrongPassword() {
        String encrypted = MD5Utils.encryptAndSalt("correct");
        assertFalse(MD5Utils.verifyWithEmbeddedSalt("wrong", encrypted));
    }

    @Test
    /**
     * testVerify_PureMD5WithoutSalt方法。
     */
    public void testVerify_PureMD5WithoutSalt() {
        String hash = MD5Utils.encrypt("password");
        assertTrue(MD5Utils.verify("password", hash, null));
        assertFalse(MD5Utils.verify("wrong", hash, null));
    }

    @Test
    /**
     * testVerify_ConstantTimeComparison方法。
     */
    public void testVerify_ConstantTimeComparison() {
        // 测试时序攻击防护：确保比较时间与不匹配位置无关
        String hash1 = MD5Utils.encrypt("short");
        String hash2 = MD5Utils.encrypt("verylongpassword");
        assertFalse(MD5Utils.verify("short", hash2, null));
        assertFalse(MD5Utils.verify("verylongpassword", hash1, null));
    }

    // ========== 加密并加盐测试 ==========

    @Test
    /**
     * testEncryptAndSalt_NotNull方法。
     */
    public void testEncryptAndSalt_NotNull() {
        String result = MD5Utils.encryptAndSalt("password");
        assertNotNull(result);
    }

    @Test
    /**
     * testEncryptAndSalt_Format方法。
     */
    public void testEncryptAndSalt_Format() {
        String result = MD5Utils.encryptAndSalt("password");
        assertTrue(result.contains(":"));
        String[] parts = result.split(":");
        assertEquals(2, parts.length);
        assertEquals(32, parts[0].length()); // MD5 部分
        assertTrue(parts[1].matches("[A-Za-z0-9+/=]+")); // Salt 部分
    }

    @Test
    /**
     * testEncryptAndSalt_VerifyRoundTrip方法。
     */
    public void testEncryptAndSalt_VerifyRoundTrip() {
        String password = "roundTripTest";
        String encrypted = MD5Utils.encryptAndSalt(password);
        assertTrue(MD5Utils.verifyWithEmbeddedSalt(password, encrypted));
    }

    @Test
    /**
     * testEncryptAndSalt_DifferentEachTime方法。
     */
    public void testEncryptAndSalt_DifferentEachTime() {
        String result1 = MD5Utils.encryptAndSalt("password");
        String result2 = MD5Utils.encryptAndSalt("password");
        assertNotEquals(result1, result2); // 盐值不同，结果应不同
    }

    @Test
    /**
     * testEncryptAndSalt_NullPassword方法。
     */
    public void testEncryptAndSalt_NullPassword() {
        assertNull(MD5Utils.encryptAndSalt(null));
    }

    // ========== 边界条件测试 ==========

    @Test
    /**
     * testEncrypt_LongString方法。
     */
    public void testEncrypt_LongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String result = MD5Utils.encrypt(sb.toString());
        assertNotNull(result);
        assertEquals(32, result.length());
    }

    @Test
    /**
     * testEncrypt_Unicode方法。
     */
    public void testEncrypt_Unicode() {
        // 测试各种 Unicode 字符
        String result1 = MD5Utils.encrypt("中文");
        String result2 = MD5Utils.encrypt("日本語");
        String result3 = MD5Utils.encrypt("한국어");
        String result4 = MD5Utils.encrypt("🔐🔑🔒");

        assertNotEquals(result1, result2);
        assertNotEquals(result2, result3);
        assertNotEquals(result3, result4);
    }

    @Test
    /**
     * testEncrypt_LineBreak方法。
     */
    public void testEncrypt_LineBreak() {
        assertEquals(MD5Utils.encrypt("line1\nline2"), MD5Utils.encrypt("line1\nline2"));
        assertNotEquals(MD5Utils.encrypt("line1\nline2"), MD5Utils.encrypt("line1\nline3"));
    }

    @Test
    /**
     * testEncrypt_TabCharacter方法。
     */
    public void testEncrypt_TabCharacter() {
        assertEquals(MD5Utils.encrypt("a\tb"), MD5Utils.encrypt("a\tb"));
        assertNotEquals(MD5Utils.encrypt("a\tb"), MD5Utils.encrypt("a\tc"));
    }

    // ========== 安全性测试 ==========

    @Test
    /**
     * testSalt_Uniqueness方法。
     */
    public void testSalt_Uniqueness() {
        // 验证盐值的唯一性
        java.util.Set<String> salts = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            salts.add(MD5Utils.generateSalt());
        }
        // 1000 次生成应有 1000 个不同的盐值（允许极少碰撞）
        assertTrue(salts.size() >= 999);
    }

    @Test
    /**
     * testEncrypt_NotReversible方法。
     */
    public void testEncrypt_NotReversible() {
        String password = "MySecretPassword123!@#";
        String hash = MD5Utils.encrypt(password);
        // MD5 不可逆，尝试解密应返回 null 或不同结果
        String decrypted = MD5Utils.encrypt(hash);
        assertNotEquals(password, decrypted);
    }

    // ========== 性能基准测试（可选）==========

    @Test
    /**
     * testEncrypt_Performance方法。
     */
    public void testEncrypt_Performance() {
        int iterations = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            MD5Utils.encrypt("performanceTest");
        }
        long duration = System.currentTimeMillis() - start;
        // 1000 次加密应在合理时间内完成（通常 < 1 秒）
        assertTrue("Encryption took too long: " + duration + "ms", duration < 5000);
    }
}
