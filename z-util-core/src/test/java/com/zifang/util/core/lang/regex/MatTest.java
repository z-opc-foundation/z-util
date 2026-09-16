package com.zifang.util.core.lang.regex;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MatTest类。
 */
public class MatTest {

    @Test
    /**
     * testIsPassport方法。
     */
    public void testIsPassport() {
        // 常见格式：字母+数字组合
        assertTrue(Mat.isPassport("G12345678"));
        assertTrue(Mat.isPassport("P1234567"));
        assertTrue(Mat.isPassport("E12345678"));
        assertTrue(Mat.isPassport("SD1234567"));
        // 纯字母或纯数字
        assertTrue(Mat.isPassport("ABCDE"));
        assertTrue(Mat.isPassport("12345"));
        // 17位上限
        assertTrue(Mat.isPassport("A1234567890123456"));
        // 长度不符
        assertFalse(Mat.isPassport("A123"));
        assertFalse(Mat.isPassport("A12345678901234567"));
        // 含特殊字符
        assertFalse(Mat.isPassport("G1234 678"));
        assertFalse(Mat.isPassport("G123-5678"));
        // null与空串
        assertFalse(Mat.isPassport(null));
        assertFalse(Mat.isPassport(""));
    }

    @Test
    /**
     * testIsIdCard方法。
     */
    public void testIsIdCard() {
        assertTrue(Mat.isIdCard("110101199003077516"));
        assertFalse(Mat.isIdCard("123"));
        assertFalse(Mat.isIdCard(null));
    }

    @Test
    /**
     * testIsMobile方法。
     */
    public void testIsMobile() {
        assertTrue(Mat.isMobile("13812345678"));
        assertFalse(Mat.isMobile("12345678901"));
        assertFalse(Mat.isMobile(null));
    }
}
