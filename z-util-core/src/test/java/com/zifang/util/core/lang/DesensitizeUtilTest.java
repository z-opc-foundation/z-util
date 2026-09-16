package com.zifang.util.core.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * DesensitizeUtilTest类。
 */
public class DesensitizeUtilTest {

    @Test
    /**
     * testMask方法。
     */
    public void testMask() {
        // 中间部分替换为等长星号
        assertEquals("156****6218", DesensitizeUtil.mask("15638296218", 3, 4));
        assertEquals("ab*****hi", DesensitizeUtil.mask("abcdefghi", 2, 2));
        // 长度不足时原样返回
        assertEquals("abc", DesensitizeUtil.mask("abc", 2, 2));
        assertEquals("", DesensitizeUtil.mask("", 2, 2));
        assertNull(DesensitizeUtil.mask(null, 2, 2));
        // 前后保留位为0时全部掩码
        assertEquals("*****", DesensitizeUtil.mask("abcde", 0, 0));
    }

    @Test
    /**
     * testMaskPhone方法。
     */
    public void testMaskPhone() {
        // 11位手机号保留前3后4
        assertEquals("156****6218", DesensitizeUtil.maskPhone("15638296218"));
        // 非11位原样返回
        assertEquals("1563829621", DesensitizeUtil.maskPhone("1563829621"));
        assertNull(DesensitizeUtil.maskPhone(null));
        assertEquals("", DesensitizeUtil.maskPhone(""));
    }

    @Test
    /**
     * testMaskIdCard方法。
     */
    public void testMaskIdCard() {
        // 18位证件号保留前6后4
        assertEquals("110101********7516", DesensitizeUtil.maskIdCard("110101199003077516"));
        // 15位证件号保留前6后3，中间为6个星号
        assertEquals("110101******123", DesensitizeUtil.maskIdCard("110101930712123"));
        // 其他长度原样返回
        assertEquals("110101199003", DesensitizeUtil.maskIdCard("110101199003"));
        assertNull(DesensitizeUtil.maskIdCard(null));
    }
}
