package com.zifang.util.core.encrypt;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * AesUtil的GCM认证加密分支单元测试。
 */
public class AesUtilGcmTest {

    private static final byte[] KEY = newKey(32);

    @Test
    public void testRoundTrip() {
        byte[] plain = "z-util 一致性 hash".getBytes(StandardCharsets.UTF_8);
        byte[] cipher = AesUtil.encryptGcm(plain, KEY);
        assertArrayEquals(plain, AesUtil.decryptGcm(cipher, KEY));
    }

    @Test
    public void testCipherLayoutIsIvPlusBodyPlusTag() {
        byte[] plain = new byte[10];
        byte[] cipher = AesUtil.encryptGcm(plain, KEY);
        // [IV 12B][ciphertext 10B][tag 16B]
        assertEquals(12 + 10 + 16, cipher.length);
    }

    @Test
    public void testEveryEncryptionUsesFreshIv() {
        byte[] plain = "same input".getBytes(StandardCharsets.UTF_8);
        byte[] first = AesUtil.encryptGcm(plain, KEY);
        byte[] second = AesUtil.encryptGcm(plain, KEY);
        assertFalse("同一明文两次加密必须不同（IV 随机）", Arrays.equals(first, second));
        assertArrayEquals(plain, AesUtil.decryptGcm(first, KEY));
        assertArrayEquals(plain, AesUtil.decryptGcm(second, KEY));
    }

    @Test
    public void testTamperedCiphertextIsRejected() {
        byte[] cipher = AesUtil.encryptGcm("do-not-touch".getBytes(StandardCharsets.UTF_8), KEY);
        cipher[cipher.length - 1] ^= 0x01;
        try {
            AesUtil.decryptGcm(cipher, KEY);
            fail("GCM 认证标签校验失败应抛异常");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("gcm"));
        }
    }

    @Test
    public void testWrongKeyIsRejected() {
        byte[] cipher = AesUtil.encryptGcm("secret".getBytes(StandardCharsets.UTF_8), KEY);
        try {
            AesUtil.decryptGcm(cipher, newKey(16));
            fail("错误密钥应抛异常");
        } catch (RuntimeException expected) {
            // 预期：mac verify 失败
        }
    }

    @Test
    public void testBase64Helpers() {
        String text = "中文 + emoji 😀 + \n换行\t制表";
        String cipher = AesUtil.encryptGcmToBase64(text, KEY);
        assertEquals(text, AesUtil.decryptGcmFromBase64(cipher, KEY));
        assertNull(AesUtil.encryptGcmToBase64(null, KEY));
        assertEquals("", AesUtil.decryptGcmFromBase64("", KEY));
    }

    @Test
    public void testKeySizesAndEmptyPlain() {
        for (int size : new int[]{16, 24, 32}) {
            byte[] key = newKey(size);
            byte[] cipher = AesUtil.encryptGcm(new byte[0], key);
            assertEquals(12 + 16, cipher.length);
            assertEquals(0, AesUtil.decryptGcm(cipher, key).length);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsBadKeyLength() {
        AesUtil.encryptGcm("x".getBytes(StandardCharsets.UTF_8), newKey(15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsShortCiphertext() {
        AesUtil.decryptGcm(new byte[5], KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNullPlain() {
        AesUtil.encryptGcm(null, KEY);
    }

    @Test
    public void testNotEqualsHexCbcShape() {
        byte[] plain = "0123456789".getBytes(StandardCharsets.UTF_8);
        assertNotEquals(0, AesUtil.encryptGcm(plain, KEY).length);
        assertTrue(AesUtil.encryptGcm(plain, KEY).length > plain.length);
    }

    private static byte[] newKey(int bytes) {
        byte[] key = new byte[bytes];
        new Random(20260928L + bytes).nextBytes(key);
        return key;
    }
}
