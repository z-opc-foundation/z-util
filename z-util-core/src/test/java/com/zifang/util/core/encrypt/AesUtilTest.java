package com.zifang.util.core.encrypt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * AesUtil工具类的单元测试
 */
public class AesUtilTest {

    private static final String PASSWORD = "z-util-test-password";

    /**
     * testEncryptDecryptToString方法：字符串加解密往返一致（含中文与特殊字符）。
     */
    @Test
    public void testEncryptDecryptToString() {
        String[] samples = {"hello world", "中文内容测试", "a=b&c=d%20e", "line1\nline2\t"};
        for (String sample : samples) {
            String encrypted = AesUtil.encryptToString(sample, PASSWORD);
            assertEquals(sample, AesUtil.decryptToString(encrypted, PASSWORD));
        }
    }

    /**
     * testEncryptDecryptEmptyString方法：空字符串可加解密往返。
     */
    @Test
    public void testEncryptDecryptEmptyString() {
        String encrypted = AesUtil.encryptToString("", PASSWORD);
        assertEquals("", AesUtil.decryptToString(encrypted, PASSWORD));
    }

    /**
     * testEncryptDecryptBytes方法：字节数组加解密往返一致。
     */
    @Test
    public void testEncryptDecryptBytes() {
        byte[] data = new byte[]{0x00, 0x01, 0x7F, (byte) 0x80, (byte) 0xFF};
        byte[] encrypted = AesUtil.encrypt(data, PASSWORD);
        byte[] decrypted = AesUtil.decrypt(encrypted, PASSWORD);
        assertTrue(java.util.Arrays.equals(data, decrypted));
    }

    /**
     * testEncryptDeterministic方法：同一口令对同一明文的加密结果确定。
     */
    @Test
    public void testEncryptDeterministic() {
        String first = AesUtil.encryptToString("deterministic", PASSWORD);
        String second = AesUtil.encryptToString("deterministic", PASSWORD);
        assertEquals(first, second);
    }

    /**
     * testDifferentPassword方法：不同口令派生不同密钥，密文不同。
     */
    @Test
    public void testDifferentPassword() {
        String first = AesUtil.encryptToString("content", PASSWORD);
        String second = AesUtil.encryptToString("content", "another-password");
        assertNotEquals(first, second);
    }

    /**
     * testDecryptWithWrongPassword方法：口令不一致时解密失败。
     */
    @Test(expected = RuntimeException.class)
    public void testDecryptWithWrongPassword() {
        String encrypted = AesUtil.encryptToString("secret", PASSWORD);
        AesUtil.decryptToString(encrypted, "wrong-password");
    }

    /**
     * testDecryptWithInvalidHex方法：非法十六进制密文抛出 IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecryptWithInvalidHex() {
        AesUtil.decryptToString("not-hex", PASSWORD);
    }

    /**
     * testDecryptWithOddLengthHex方法：奇数长度十六进制串抛出 IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecryptWithOddLengthHex() {
        AesUtil.decryptToString("ABC", PASSWORD);
    }

    /**
     * testEncryptWithNullPassword方法：口令为 null 抛出 IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEncryptWithNullPassword() {
        AesUtil.encryptToString("content", null);
    }

    /**
     * testGenerateKey方法：生成指定长度的随机密钥，非法位数抛出 IllegalArgumentException。
     */
    @Test
    public void testGenerateKey() {
        assertEquals(16, AesUtil.generateKey(128).length);
        assertEquals(24, AesUtil.generateKey(192).length);
        assertEquals(32, AesUtil.generateKey(256).length);
        // 两次生成的随机密钥内容不同
        assertFalse(java.util.Arrays.equals(AesUtil.generateKey(128), AesUtil.generateKey(128)));
    }

    /**
     * testGenerateKeyWithInvalidSize方法：非法位数抛出 IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateKeyWithInvalidSize() {
        AesUtil.generateKey(100);
    }

    /**
     * testEncryptDecryptCbc方法：CBC 模式往返加解密，同密钥同初始向量结果确定。
     */
    @Test
    public void testEncryptDecryptCbc() {
        byte[] key = AesUtil.generateKey(128);
        byte[] iv = new byte[16];
        byte[] data = "cbc-round-trip-测试".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encrypted = AesUtil.encryptCbc(data, key, iv);
        // 同 key+iv 加密结果确定
        byte[] encryptedAgain = AesUtil.encryptCbc(data, key, iv);
        assertTrue(java.util.Arrays.equals(encrypted, encryptedAgain));
        // 解密得到原文
        assertTrue(java.util.Arrays.equals(data, AesUtil.decryptCbc(encrypted, key, iv)));
    }

    /**
     * testCbcDifferentIv方法：不同初始向量产生不同密文。
     */
    @Test
    public void testCbcDifferentIv() {
        byte[] key = AesUtil.generateKey(128);
        byte[] iv1 = new byte[16];
        byte[] iv2 = new byte[16];
        iv2[0] = 1;
        byte[] data = "same-plain".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(java.util.Arrays.equals(
                AesUtil.encryptCbc(data, key, iv1),
                AesUtil.encryptCbc(data, key, iv2)));
    }

    /**
     * testEncryptDecryptCbcBase64方法：Base64 文本形态往返加解密。
     */
    @Test
    public void testEncryptDecryptCbcBase64() {
        byte[] key = AesUtil.generateKey(256);
        byte[] iv = new byte[16];
        String cipherText = AesUtil.encryptCbcToBase64("base64-round-trip", key, iv);
        // Base64 合法字符集
        assertTrue(cipherText.matches("[A-Za-z0-9+/=]+"));
        assertEquals("base64-round-trip", AesUtil.decryptCbcFromBase64(cipherText, key, iv));
    }

    /**
     * testCbcInvalidKeyOrIv方法：非法密钥/初始向量长度抛出 IllegalArgumentException。
     */
    @Test
    public void testCbcInvalidKeyOrIv() {
        byte[] key = AesUtil.generateKey(128);
        byte[] badKey = new byte[10];
        byte[] iv = new byte[16];
        byte[] badIv = new byte[8];
        byte[] data = new byte[]{1, 2, 3};
        try {
            AesUtil.encryptCbc(data, badKey, iv);
            fail("expected IllegalArgumentException for bad key");
        } catch (IllegalArgumentException ignored) {
            // 预期
        }
        try {
            AesUtil.encryptCbc(data, key, badIv);
            fail("expected IllegalArgumentException for bad iv");
        } catch (IllegalArgumentException ignored) {
            // 预期
        }
    }
}
