package com.zifang.util.ch;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * PinyinGeneratorUtil 测试类
 */

/**
 * PinyinGeneratorUtilTest类。
 */
public class PinyinGeneratorUtilTest {

    @Test
    /**
     * testTransformToFullPinyin方法。
     */
    public void testTransformToFullPinyin() {
        assertEquals("zifang", PinyinGeneratorUtil.transformToFullPinyin("子方"));
        assertEquals("zhongguo", PinyinGeneratorUtil.transformToFullPinyin("中国"));
        assertEquals("hello", PinyinGeneratorUtil.transformToFullPinyin("hello"));
        assertEquals("abc", PinyinGeneratorUtil.transformToFullPinyin("abc"));
    }

    @Test
    /**
     * testTransformToHeadPinyin方法。
     */
    public void testTransformToHeadPinyin() {
        assertEquals("zf", PinyinGeneratorUtil.transformToHeadPinyin("子方"));
        assertEquals("zg", PinyinGeneratorUtil.transformToHeadPinyin("中国"));
        assertEquals("hello", PinyinGeneratorUtil.transformToHeadPinyin("hello"));
    }

    @Test
    /**
     * testGetPingYin方法。
     */
    public void testGetPingYin() {
        assertEquals("zhongguo", PinyinGeneratorUtil.getPingYin("中国"));
        assertEquals("hello", PinyinGeneratorUtil.getPingYin("hello"));
    }

    @Test
    /**
     * testGetFirstSpell方法。
     */
    public void testGetFirstSpell() {
        assertEquals("zf", PinyinGeneratorUtil.getFirstSpell("子方"));
        assertEquals("zg", PinyinGeneratorUtil.getFirstSpell("中国"));
    }

    @Test
    /**
     * testGetFullSpell方法。
     */
    public void testGetFullSpell() {
        assertEquals("zifang", PinyinGeneratorUtil.getFullSpell("子方"));
        assertEquals("zhongguo", PinyinGeneratorUtil.getFullSpell("中国"));
    }

    @Test
    /**
     * testIsChineseByREG方法。
     */
    public void testIsChineseByREG() {
        assertTrue(PinyinGeneratorUtil.isChineseByREG("中国"));
        assertTrue(PinyinGeneratorUtil.isChineseByREG("hello中国"));
        assertFalse(PinyinGeneratorUtil.isChineseByREG("hello"));
        assertFalse(PinyinGeneratorUtil.isChineseByREG(null));
        assertFalse(PinyinGeneratorUtil.isChineseByREG(""));
    }

    @Test
    /**
     * testIsChinese方法。
     */
    public void testIsChinese() {
        assertTrue(PinyinGeneratorUtil.isChinese("中国"));
        assertFalse(PinyinGeneratorUtil.isChinese("hello"));
    }

    @Test
    /**
     * testIsChineseChar方法。
     */
    public void testIsChineseChar() {
        assertTrue(PinyinGeneratorUtil.isChinese('中'));
        assertFalse(PinyinGeneratorUtil.isChinese('a'));
        assertFalse(PinyinGeneratorUtil.isChinese('1'));
    }

    @Test
    /**
     * testChineseLength方法。
     */
    public void testChineseLength() {
        assertEquals(2, PinyinGeneratorUtil.ChineseLength("中国"));
        assertEquals(3, PinyinGeneratorUtil.ChineseLength("中国人"));
        assertEquals(0, PinyinGeneratorUtil.ChineseLength("hello"));
    }

    @Test
    /**
     * testTransIDCard15to18方法。
     */
    public void testTransIDCard15to18() {
        // 15位身份证：110105490311123 -> 18位
        String idcard15 = "110105490311123";
        String idcard18 = PinyinGeneratorUtil.transIDCard15to18(idcard15);
        assertNotNull(idcard18);
        assertEquals(18, idcard18.length());
        assertTrue(idcard18.startsWith("1101051949"));

        // 无效输入返回null
        assertNull(PinyinGeneratorUtil.transIDCard15to18(null));
        assertNull(PinyinGeneratorUtil.transIDCard15to18("123")); // 长度不足
    }
}