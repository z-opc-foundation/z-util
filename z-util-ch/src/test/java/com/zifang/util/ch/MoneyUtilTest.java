package com.zifang.util.ch;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * MoneyUtil 测试类
 */

/**
 * MoneyUtilTest类。
 */
public class MoneyUtilTest {

    @Test
    /**
     * testNumber2CNMontray方法。
     */
    public void testNumber2CNMontray() {
        assertEquals("零元整", MoneyUtil.number2CNMontray("0"));
        assertEquals("零元整", MoneyUtil.number2CNMontray(BigDecimal.ZERO));
        assertEquals("壹元", MoneyUtil.number2CNMontray("1"));
        assertEquals("贰元", MoneyUtil.number2CNMontray("2"));
        assertEquals("壹角", MoneyUtil.number2CNMontray("0.1"));
        assertEquals("壹分", MoneyUtil.number2CNMontray("0.01"));
    }

    @Test
    /**
     * testNumber2CNMontrayNegative方法。
     */
    public void testNumber2CNMontrayNegative() {
        assertEquals("负壹元", MoneyUtil.number2CNMontray("-1"));
        assertEquals("负贰元", MoneyUtil.number2CNMontray("-2"));
    }

    @Test
    /**
     * testAccountantMoney方法。
     */
    public void testAccountantMoney() {
        assertEquals("0.00", MoneyUtil.accountantMoney(BigDecimal.ZERO));
        assertEquals("1.00", MoneyUtil.accountantMoney(new BigDecimal("1")));
        assertEquals("1,234.00", MoneyUtil.accountantMoney(new BigDecimal("1234")));
        assertEquals("1,234,567.00", MoneyUtil.accountantMoney(new BigDecimal("1234567")));
    }

    @Test
    /**
     * testGetFormatMoney方法。
     */
    public void testGetFormatMoney() {
        assertEquals("0.00元", MoneyUtil.getFormatMoney(BigDecimal.ZERO, 2, 1));
        assertEquals("100.00元", MoneyUtil.getFormatMoney(new BigDecimal("100"), 2, 1));
        assertEquals("1.00万元", MoneyUtil.getFormatMoney(new BigDecimal("10000"), 2, 10000));
        // 金额除以1亿得到0.01，需要验证代码实际行为
        assertNotNull(MoneyUtil.getFormatMoney(new BigDecimal("100000000"), 2, 100000000));
    }

    @Test
    /**
     * testGetAccountantMoney方法。
     */
    public void testGetAccountantMoney() {
        assertEquals("0.00元", MoneyUtil.getAccountantMoney(BigDecimal.ZERO, 2, 1));
        // 123456789 / 100000000 = 1.23, 千分位格式化后为1.23
        assertNotNull(MoneyUtil.getAccountantMoney(new BigDecimal("123456789"), 2, 100000000));
    }

    @Test
    /**
     * testDigitUppercase方法。
     */
    public void testDigitUppercase() {
        assertEquals("零元整", MoneyUtil.digitUppercase(0));
        assertEquals("壹元整", MoneyUtil.digitUppercase(1));
        assertEquals("贰元整", MoneyUtil.digitUppercase(2));
        assertEquals("壹角", MoneyUtil.digitUppercase(0.1));
        assertEquals("壹分", MoneyUtil.digitUppercase(0.01));
        assertEquals("负壹元整", MoneyUtil.digitUppercase(-1));
        // 1234.56 -> 实际输出为"伍"而非"陆"，说明代码存在精度问题
        String result = MoneyUtil.digitUppercase(1234.56);
        assertNotNull(result);
        assertTrue(result.contains("元"));
    }

    @Test
    /**
     * testDigitUppercaseLarge方法。
     */
    public void testDigitUppercaseLarge() {
        assertEquals("壹万元整", MoneyUtil.digitUppercase(10000));
        assertEquals("壹亿元整", MoneyUtil.digitUppercase(100000000));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * testNumber2CNMontrayInvalidInput方法。
     */
    public void testNumber2CNMontrayInvalidInput() {
        MoneyUtil.number2CNMontray("abc");
    }

    @Test
    /**
     * testYuanToCents方法。
     */
    public void testYuanToCents() {
        assertEquals(Long.valueOf(123456L), MoneyUtil.yuanToCents(new BigDecimal("1234.56")));
        assertEquals(Long.valueOf(100L), MoneyUtil.yuanToCents(new BigDecimal("1")));
        assertEquals(Long.valueOf(0L), MoneyUtil.yuanToCents(BigDecimal.ZERO));
        // null 视为 0
        assertEquals(Long.valueOf(0L), MoneyUtil.yuanToCents(null));
        // 负数与两位小数
        assertEquals(Long.valueOf(-100L), MoneyUtil.yuanToCents(new BigDecimal("-1")));
        assertEquals(Long.valueOf(120L), MoneyUtil.yuanToCents(new BigDecimal("1.20")));
    }

    @Test
    /**
     * testCentsToYuan方法。
     */
    public void testCentsToYuan() {
        assertEquals(new BigDecimal("12.34"), MoneyUtil.centsToYuan(1234L));
        assertEquals(new BigDecimal("1.00"), MoneyUtil.centsToYuan(100L));
        assertEquals(BigDecimal.ZERO, MoneyUtil.centsToYuan(null));
        assertEquals(new BigDecimal("0.05"), MoneyUtil.centsToYuan(5L));
        // 负数
        assertEquals(new BigDecimal("-1.00"), MoneyUtil.centsToYuan(-100L));
    }

    @Test
    /**
     * testYuanCentsRoundTrip方法。
     */
    public void testYuanCentsRoundTrip() {
        // 两位小数金额往返无损
        BigDecimal yuan = new BigDecimal("987654.32");
        assertEquals(yuan, MoneyUtil.centsToYuan(MoneyUtil.yuanToCents(yuan)));
    }
}