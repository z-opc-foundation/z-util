package com.zifang.util.core.lang;

import com.zifang.util.core.time.LocalDateUtil;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * IdCardUtil工具类的单元测试
 */
public class IdCardUtilTest {

    /**
     * 有效 18 位样例（校验码 X）
     */
    private static final String VALID_EIGHTEEN_X = "11010519491231002X";

    /**
     * 有效 18 位样例（校验码 0）
     */
    private static final String VALID_EIGHTEEN_0 = "110105199001010010";

    /**
     * 有效 15 位样例（升 18 位后等于 VALID_EIGHTEEN_0）
     */
    private static final String VALID_FIFTEEN = "110105900101001";

    /**
     * testIsValid方法：格式与校验码均正确的号码有效。
     */
    @Test
    public void testIsValid() {
        assertTrue(IdCardUtil.isValid(VALID_EIGHTEEN_X));
        assertTrue(IdCardUtil.isValid(VALID_EIGHTEEN_X.toLowerCase()));
        assertTrue(IdCardUtil.isValid(VALID_EIGHTEEN_0));
        assertTrue(IdCardUtil.isValid(VALID_FIFTEEN));
    }

    /**
     * testIsValidWithInvalidCheckCode方法：格式正确但校验码不匹配的号码无效。
     */
    @Test
    public void testIsValidWithInvalidCheckCode() {
        assertFalse(IdCardUtil.isValid("110105199001010011"));
        assertFalse(IdCardUtil.isValid("110105194912310021"));
    }

    /**
     * testIsValidWithBadInput方法：null、长度与格式非法的号码无效。
     */
    @Test
    public void testIsValidWithBadInput() {
        assertFalse(IdCardUtil.isValid(null));
        assertFalse(IdCardUtil.isValid(""));
        assertFalse(IdCardUtil.isValid("11010519900101001"));
        assertFalse(IdCardUtil.isValid("1101051990010100100"));
        assertFalse(IdCardUtil.isValid("010105199001010010"));
        assertFalse(IdCardUtil.isValid("110105199013010010"));
        assertFalse(IdCardUtil.isValid("110105199001320010"));
    }

    /**
     * testIsValidFormat方法：仅校验格式不校验校验码。
     */
    @Test
    public void testIsValidFormat() {
        assertTrue(IdCardUtil.isValidFormat(VALID_EIGHTEEN_0));
        assertTrue(IdCardUtil.isValidFormat("110105199001010011"));
        assertTrue(IdCardUtil.isValidFormat(VALID_FIFTEEN));
        assertFalse(IdCardUtil.isValidFormat("1101051990010100"));
        assertFalse(IdCardUtil.isValidFormat(null));
    }

    /**
     * testGetCheckCode方法：按前 17 位加权计算校验码。
     */
    @Test
    public void testGetCheckCode() {
        assertEquals('0', IdCardUtil.getCheckCode(VALID_EIGHTEEN_0));
        assertEquals('0', IdCardUtil.getCheckCode(VALID_EIGHTEEN_0.substring(0, 17)));
        assertEquals('X', IdCardUtil.getCheckCode(VALID_EIGHTEEN_X));
        assertEquals('X', IdCardUtil.getCheckCode(VALID_EIGHTEEN_X.substring(0, 17)));
    }

    /**
     * testGetCheckCodeWithIllegalInput方法：长度非法或前 17 位含非数字时抛出异常。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetCheckCodeWithIllegalLength() {
        IdCardUtil.getCheckCode("110105199001010");
    }

    /**
     * testGetCheckCodeWithIllegalChar方法：前 17 位含非数字字符时抛出异常。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetCheckCodeWithIllegalChar() {
        IdCardUtil.getCheckCode("11010519900101x0010");
    }

    /**
     * testToEighteen方法：15 位号码补世纪与校验码升为 18 位。
     */
    @Test
    public void testToEighteen() {
        assertEquals(VALID_EIGHTEEN_0, IdCardUtil.toEighteen(VALID_FIFTEEN));
        // 1949-12-31 的 15 位号码升位后校验码为 X
        assertEquals("11010519491231002X", IdCardUtil.toEighteen("110105491231002"));
    }

    /**
     * testToEighteenWithIllegalInput方法：非法 15 位号码返回 null。
     */
    @Test
    public void testToEighteenWithIllegalInput() {
        assertNull(IdCardUtil.toEighteen(null));
        assertNull(IdCardUtil.toEighteen(VALID_EIGHTEEN_0));
        assertNull(IdCardUtil.toEighteen("010105900101001"));
    }

    /**
     * testGetBirthday方法：提取出生日期字符串，15 位号码补世纪。
     */
    @Test
    public void testGetBirthday() {
        assertEquals("1990-01-01", IdCardUtil.getBirthday(VALID_EIGHTEEN_0));
        assertEquals("1949-12-31", IdCardUtil.getBirthday(VALID_EIGHTEEN_X));
        assertEquals("1990-01-01", IdCardUtil.getBirthday(VALID_FIFTEEN));
        assertNull(IdCardUtil.getBirthday(null));
        assertNull(IdCardUtil.getBirthday("12345"));
    }

    /**
     * testGetBirthDate方法：提取出生日期，非法日期返回 null。
     */
    @Test
    public void testGetBirthDate() {
        assertEquals(LocalDate.of(1990, 1, 1), IdCardUtil.getBirthDate(VALID_EIGHTEEN_0));
        // 13 月的非法日期返回 null
        assertNull(IdCardUtil.getBirthDate("110105199013010010"));
        assertNull(IdCardUtil.getBirthDate(null));
    }

    /**
     * testGetAreaCode方法：提取行政区划码。
     */
    @Test
    public void testGetAreaCode() {
        assertEquals("110105", IdCardUtil.getAreaCode(VALID_EIGHTEEN_0));
        assertEquals("110105", IdCardUtil.getAreaCode(VALID_FIFTEEN));
        assertNull(IdCardUtil.getAreaCode(null));
        assertNull(IdCardUtil.getAreaCode("110105"));
    }

    /**
     * testGetGenderCode方法：顺序码末位奇数为男、偶数为女。
     */
    @Test
    public void testGetGenderCode() {
        // VALID_EIGHTEEN_0 第 17 位为 1，男
        assertEquals(1, IdCardUtil.getGenderCode(VALID_EIGHTEEN_0));
        // VALID_EIGHTEEN_X 第 17 位为 2，女
        assertEquals(0, IdCardUtil.getGenderCode(VALID_EIGHTEEN_X));
        // 15 位号码取第 15 位
        assertEquals(1, IdCardUtil.getGenderCode(VALID_FIFTEEN));
        assertEquals(-1, IdCardUtil.getGenderCode(null));
        assertEquals(-1, IdCardUtil.getGenderCode("1101051990010100"));
        // 第 17 位为非数字时返回 -1
        assertEquals(-1, IdCardUtil.getGenderCode("1101051990010100X1"));
    }

    /**
     * testGetAge方法：周岁年龄与 LocalDateUtil.age 一致，非法返回 -1。
     */
    @Test
    public void testGetAge() {
        assertEquals(LocalDateUtil.age(LocalDate.of(1990, 1, 1)), IdCardUtil.getAge(VALID_EIGHTEEN_0));
        assertEquals(IdCardUtil.getAge(VALID_EIGHTEEN_0), IdCardUtil.getAge(VALID_FIFTEEN));
        assertEquals(-1, IdCardUtil.getAge(null));
        assertEquals(-1, IdCardUtil.getAge("110105199013010010"));
    }
}
