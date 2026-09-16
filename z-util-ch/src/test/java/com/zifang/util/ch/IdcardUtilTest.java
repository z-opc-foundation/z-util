package com.zifang.util.ch;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * IdcardUtil 测试类
 */

/**
 * IdcardUtilTest类。
 */
public class IdcardUtilTest {

    @Test
    /**
     * testIsValidatedAllIdcard方法。
     */
    public void testIsValidatedAllIdcard() {
        // 无效身份证
        assertFalse(IdcardUtil.isValidatedAllIdcard("123456789012345678"));
        assertFalse(IdcardUtil.isValidatedAllIdcard("000000000000000000"));
        // 格式正确但校验位错误的身份证
        assertFalse(IdcardUtil.isValidatedAllIdcard("110105194910010010"));
        // 15位：省份与出生日期均合法
        assertTrue(IdcardUtil.isValidatedAllIdcard("110105490101001"));
        // 15位：日期非法（02月31日）
        assertFalse(IdcardUtil.isValidatedAllIdcard("110105490231001"));
        // 15位：省份非法
        assertFalse(IdcardUtil.isValidatedAllIdcard("990105490101001"));
        // 空与长度错误
        assertFalse(IdcardUtil.isValidatedAllIdcard(null));
        assertFalse(IdcardUtil.isValidatedAllIdcard(""));
        assertFalse(IdcardUtil.isValidatedAllIdcard("1101051949100100"));
    }

    @Test
    /**
     * testValidate15Idcard方法。
     */
    public void testValidate15Idcard() {
        // 合法15位：地址码110105 + 出生日期490101 + 顺序码001
        assertTrue(IdcardUtil.validate15Idcard("110105490101001"));
        // 长度错误
        assertFalse(IdcardUtil.validate15Idcard("1101054901010001"));
        // 含非数字
        assertFalse(IdcardUtil.validate15Idcard("11010549010100a"));
        // 日期非法
        assertFalse(IdcardUtil.validate15Idcard("110105490431001"));
        // 省份非法
        assertFalse(IdcardUtil.validate15Idcard("010105490101001"));
    }

    @Test
    /**
     * testConvert15To18方法。
     */
    public void testConvert15To18() {
        // 15位转18位：年份补19前缀，末尾追加校验码
        assertEquals("110105194901010013", IdcardUtil.convert15To18("110105490101001"));
        // 转换后的号码应通过18位完整校验
        assertTrue(IdcardUtil.isValidate18Idcard(IdcardUtil.convert15To18("110105490101001")));
        // 非法输入返回null
        assertNull(IdcardUtil.convert15To18(null));
        assertNull(IdcardUtil.convert15To18("1101054901010"));
        assertNull(IdcardUtil.convert15To18("110105490231001"));
    }

    @Test
    /**
     * testGetBirthday方法。
     */
    public void testGetBirthday() {
        // 18位：第7~14位为出生日期
        assertEquals("1949-10-01", IdcardUtil.getBirthday("110105194910010015"));
        // 15位：第7~12位为出生日期，年份视为19xx
        assertEquals("1949-01-01", IdcardUtil.getBirthday("110105490101001"));
        // 18位但日期非法
        assertNull(IdcardUtil.getBirthday("110105194902310015"));
        // 长度错误
        assertNull(IdcardUtil.getBirthday("1101051949"));
        assertNull(IdcardUtil.getBirthday(null));
    }

    @Test
    /**
     * testGetProvince方法。
     */
    public void testGetProvince() {
        assertEquals("北京", IdcardUtil.getProvince("110105194910010015"));
        assertEquals("北京", IdcardUtil.getProvince("110105490101001"));
        assertEquals("国外", IdcardUtil.getProvince("910105194910010015"));
        // 地址码无法识别
        assertNull(IdcardUtil.getProvince("010105194910010015"));
        assertNull(IdcardUtil.getProvince(null));
    }

    @Test
    /**
     * testIsValidate18Idcard方法。
     */
    public void testIsValidate18Idcard() {
        // 正确格式但校验位错误
        assertFalse(IdcardUtil.isValidate18Idcard("110105194910010010"));
        // 长度不对
        assertFalse(IdcardUtil.isValidate18Idcard("11010519491001001"));
        assertFalse(IdcardUtil.isValidate18Idcard("1101051949100100111"));
        // 包含字母
        assertFalse(IdcardUtil.isValidate18Idcard("11010519491001001a"));
    }

    @Test
    /**
     * testIs18Idcard方法。
     */
    public void testIs18Idcard() {
        assertTrue(IdcardUtil.is18Idcard("110105194910010011"));
        assertTrue(IdcardUtil.is18Idcard("11010519491001001x"));
        assertFalse(IdcardUtil.is18Idcard("123456789012345678"));
        assertFalse(IdcardUtil.is18Idcard("123"));
    }

    @Test
    /**
     * testGetUserSex方法。
     */
    public void testGetUserSex() {
        // 18位身份证：第17位（索引16）决定性别
        // 110105194910010011 - 第17位是1（奇数）-> 男
        assertEquals(1, IdcardUtil.getUserSex("110105194910010011"));
        // 110105194910010023 - 第17位是2（偶数）-> 女
        assertEquals(0, IdcardUtil.getUserSex("110105194910010023"));
        // 15位身份证：第15位（索引14）决定性别
        assertEquals(1, IdcardUtil.getUserSex("110105490311123"));
        // 15位身份证：第15位为2（偶数）-> 女
        assertEquals(0, IdcardUtil.getUserSex("110105490101002"));
        // null与长度异常时默认返回1
        assertEquals(1, IdcardUtil.getUserSex(null));
        assertEquals(1, IdcardUtil.getUserSex("110105"));
    }
}