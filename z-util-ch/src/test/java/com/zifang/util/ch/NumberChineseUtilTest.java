package com.zifang.util.ch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * NumberChineseUtil测试：整数逐位中文念法转换。
 */
public class NumberChineseUtilTest {

    @Test
    public void testDigitToChinese() {
        assertEquals("零", NumberChineseUtil.digitToChinese(0));
        assertEquals("一二三", NumberChineseUtil.digitToChinese(123));
        // 中间与末尾的零同样逐位输出
        assertEquals("一零零五零", NumberChineseUtil.digitToChinese(10050));
        assertEquals("一零", NumberChineseUtil.digitToChinese(10));
        assertEquals("九", NumberChineseUtil.digitToChinese(9));
        // 负号转换为「负」前缀
        assertEquals("负二五", NumberChineseUtil.digitToChinese(-25));
    }

    @Test
    public void testDigitToChineseUpper() {
        assertEquals("零", NumberChineseUtil.digitToChineseUpper(0));
        assertEquals("壹贰叁", NumberChineseUtil.digitToChineseUpper(123));
        assertEquals("壹零零伍零", NumberChineseUtil.digitToChineseUpper(10050));
        assertEquals("负贰伍", NumberChineseUtil.digitToChineseUpper(-25));
    }

    @Test
    public void testLongBoundaries() {
        // Long.MIN_VALUE 通过字符串逐位映射，不受绝对值溢出影响
        assertEquals("负九二二三三七二零三六八五四七七五八零八",
                NumberChineseUtil.digitToChinese(Long.MIN_VALUE));
        String maxValue = NumberChineseUtil.digitToChineseUpper(Long.MAX_VALUE);
        assertEquals(19, maxValue.length());
        // Long.MAX_VALUE = 9223372036854775807，首位玖末位柒
        assertEquals('玖', maxValue.charAt(0));
        assertEquals('柒', maxValue.charAt(maxValue.length() - 1));
    }
}
