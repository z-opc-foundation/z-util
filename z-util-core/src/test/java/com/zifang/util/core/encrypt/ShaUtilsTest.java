package com.zifang.util.core.encrypt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * ShaUtilsTest类。
 */
public class ShaUtilsTest {

    @Test
    /**
     * testSha1方法。SHA-1 已知向量校验（小写十六进制）。
     */
    public void testSha1() {
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", ShaUtils.sha1("abc"));
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", ShaUtils.sha1(""));
        assertEquals(40, ShaUtils.sha1("hello world").length());
    }

    @Test
    /**
     * testSha1Uppercase方法。大写开关生效。
     */
    public void testSha1Uppercase() {
        assertEquals("A9993E364706816ABA3E25717850C26C9CD0D89D", ShaUtils.sha1("abc", true));
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", ShaUtils.sha1("abc", false));
    }

    @Test
    /**
     * testSha256方法。SHA-256 已知向量校验（小写十六进制）。
     */
    public void testSha256() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", ShaUtils.sha256("abc"));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", ShaUtils.sha256(""));
        assertEquals("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD", ShaUtils.sha256("abc", true));
    }

    @Test
    /**
     * testDigest方法。指定算法计算摘要，与便捷方法结果一致。
     */
    public void testDigest() {
        assertEquals(ShaUtils.sha1("abc"), ShaUtils.digest("abc", ShaUtils.SHA_1, false));
        assertEquals(ShaUtils.sha256("abc", true), ShaUtils.digest("abc", ShaUtils.SHA_256, true));
        assertEquals(128, ShaUtils.digest("abc", "SHA-512", false).length());
        assertEquals(32, ShaUtils.digest("abc", "MD5", false).length());
    }

    @Test
    /**
     * testDigest_NullText方法。null 输入返回 null。
     */
    public void testDigest_NullText() {
        assertNull(ShaUtils.sha1(null));
        assertNull(ShaUtils.sha256(null));
        assertNull(ShaUtils.digest(null, ShaUtils.SHA_1, true));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testDigest_UnsupportedAlgorithm方法。不支持的算法抛 IllegalArgumentException。
     */
    public void testDigest_UnsupportedAlgorithm() {
        ShaUtils.digest("abc", "SHA-999", false);
    }

    @Test
    /**
     * testDigest_Utf8方法。UTF-8 编码中文摘要稳定可复现。
     */
    public void testDigest_Utf8() {
        String first = ShaUtils.sha256("中文摘要测试");
        String second = ShaUtils.sha256("中文摘要测试");
        assertEquals(first, second);
        assertEquals(64, first.length());
    }
}
