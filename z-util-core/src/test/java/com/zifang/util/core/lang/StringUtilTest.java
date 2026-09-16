package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * StringUtilTest类。
 */
public class StringUtilTest {
    @Test
    /**
     * isBlank方法。
     */
    public void isBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("Hello"));
        assertFalse(StringUtil.isBlank(" Hello "));
    }

    @Test
    /**
     * isAllLowerCase方法。
     */
    public void isAllLowerCase() {

    }

    @Test
    /**
     * isAllUpperCase方法。
     */
    public void isAllUpperCase() {
    }

    @Test
    /**
     * isEmpty方法。
     */
    public void isEmpty() {
    }

    @Test
    /**
     * isNotEmptyString方法。
     */
    public void isNotEmptyString() {
    }

    @Test
    /**
     * append方法。
     */
    public void append() {
    }

    @Test
    /**
     * collapseWhitespace方法。
     */
    public void collapseWhitespace() {
    }

    @Test
    /**
     * contains方法。
     */
    public void contains() {
    }

    @Test
    /**
     * testContains方法。
     */
    public void testContains() {
    }

    @Test
    /**
     * containsAll方法。
     */
    public void containsAll() {
    }

    @Test
    /**
     * testContainsAll方法。
     */
    public void testContainsAll() {
    }

    @Test
    /**
     * containsAny方法。
     */
    public void containsAny() {
    }

    @Test
    /**
     * testContainsAny方法。
     */
    public void testContainsAny() {
    }

    @Test
    /**
     * countSubstr方法。
     */
    public void countSubstr() {
    }

    @Test
    /**
     * testCountSubstr方法。
     */
    public void testCountSubstr() {
    }

    @Test
    /**
     * endsWith方法。
     */
    public void endsWith() {
    }

    @Test
    /**
     * testEndsWith方法。
     */
    public void testEndsWith() {
    }

    @Test
    /**
     * testEndsWith1方法。
     */
    public void testEndsWith1() {
    }

    @Test
    /**
     * ensureLeft方法。
     */
    public void ensureLeft() {
    }

    @Test
    /**
     * testEnsureLeft方法。
     */
    public void testEnsureLeft() {
    }

    @Test
    /**
     * ensureRight方法。
     */
    public void ensureRight() {
    }

    @Test
    /**
     * testEnsureRight方法。
     */
    public void testEnsureRight() {
    }

    @Test
    /**
     * base64Decode方法。
     */
    public void base64Decode() {
    }

    @Test
    /**
     * base64Encode方法。
     */
    public void base64Encode() {
    }

    @Test
    /**
     * binDecode方法。
     */
    public void binDecode() {
    }

    @Test
    /**
     * binEncode方法。
     */
    public void binEncode() {
    }

    @Test
    /**
     * decDecode方法。
     */
    public void decDecode() {
    }

    @Test
    /**
     * decEncode方法。
     */
    public void decEncode() {
    }

    @Test
    /**
     * decode方法。
     */
    public void decode() {
    }

    @Test
    /**
     * encode方法。
     */
    public void encode() {
    }

    @Test
    /**
     * first方法。
     */
    public void first() {
    }

    @Test
    /**
     * head方法。
     */
    public void head() {
    }

    @Test
    /**
     * format方法。
     */
    public void format() {
    }

    @Test
    /**
     * hexDecode方法。
     */
    public void hexDecode() {
    }

    @Test
    /**
     * hexEncode方法。
     */
    public void hexEncode() {
    }

    @Test
    /**
     * indexOf方法。
     */
    public void indexOf() {
    }

    @Test
    /**
     * unequal方法。
     */
    public void unequal() {
    }

    @Test
    /**
     * inequal方法。
     */
    public void inequal() {
    }

    @Test
    /**
     * insert方法。
     */
    public void insert() {
    }

    @Test
    /**
     * isUpperCase方法。
     */
    public void isUpperCase() {
    }

    @Test
    /**
     * slugify方法。
     */
    public void slugify() {
    }

    @Test
    /**
     * transliterate方法。
     */
    public void transliterate() {
    }

    @Test
    /**
     * surround方法。
     */
    public void surround() {
    }

    @Test
    /**
     * toCamelCase方法。
     */
    public void toCamelCase() {
    }

    @Test
    /**
     * toStudlyCase方法。
     */
    public void toStudlyCase() {
    }

    @Test
    /**
     * tail方法。
     */
    public void tail() {
    }

    @Test
    /**
     * toDecamelize方法。
     */
    public void toDecamelize() {
    }

    @Test
    /**
     * toKebabCase方法。
     */
    public void toKebabCase() {
    }

    @Test
    /**
     * toSnakeCase方法。
     */
    public void toSnakeCase() {
    }

    @Test
    /**
     * capitalize方法。
     */
    public void capitalize() {
    }

    @Test
    /**
     * lowerFirst方法。
     */
    public void lowerFirst() {
    }

    @Test
    /**
     * isEnclosedBetween方法。
     */
    public void isEnclosedBetween() {
    }

    @Test
    /**
     * testIsEnclosedBetween方法。
     */
    public void testIsEnclosedBetween() {
    }

    @Test
    /**
     * upperFirst方法。
     */
    public void upperFirst() {
    }

    @Test
    /**
     * trimStart方法。
     */
    public void trimStart() {
    }

    @Test
    /**
     * charsCount方法。
     */
    public void charsCount() {
    }

    @Test
    /**
     * underscored方法。
     */
    public void underscored() {
    }

    @Test
    /**
     * lines方法。
     */
    public void lines() {
    }

    @Test
    /**
     * dasherize方法。
     */
    public void dasherize() {
    }

    @Test
    /**
     * humanize方法。
     */
    public void humanize() {
    }

    @Test
    /**
     * swapCase方法。
     */
    public void swapCase() {
    }

    @Test
    /**
     * formatNumber方法。
     */
    public void formatNumber() {
    }

    @Test
    /**
     * chop方法。
     */
    public void chop() {
    }

    @Test
    /**
     * startCase方法。
     */
    public void startCase() {
    }

    @Test
    /**
     * escapeRegExp方法。
     */
    public void escapeRegExp() {
    }

    @Test
    /**
     * rightPad方法。
     */
    public void rightPad() {
        assert StringUtil.rightPad("2", "0", 2).equals("20");
        assert StringUtil.rightPad("12", "0", 2).equals("12");
        assert StringUtil.rightPad("012", "0", 2).equals("012");
    }

    @Test
    /**
     * leftPad方法。
     */
    public void leftPad() {
        assert StringUtil.leftPad("2", 2, "0").equals("02");
        assert StringUtil.leftPad("12", 2, "0").equals("12");
        assert StringUtil.leftPad("012", 2, "0").equals("012");
    }

    @Test
    /**
     * repeat方法。
     */
    public void repeat() {
        assert StringUtil.repeat("2", 2).equals("22");
        assert StringUtil.repeat("-2", 2).equals("-2-2");
    }

    @Test
    /**
     * changeToFull方法。
     */
    public void changeToFull() {

    }

    @Test
    /**
     * unicodeEscaped方法。
     */
    public void unicodeEscaped() {
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
    }

    @Test
    /**
     * replaceTest方法。
     */
    public void replaceTest() {

        assert "%s".equals(StringUtil.replace("{}",
                "\\{\\}",
                "%s", false));

    }

    @Test
    /**
     * replaceBlank方法。
     */
    public void replaceBlank() {
    }

    @Test
    /**
     * string2Unicode方法。
     */
    public void string2Unicode() {
    }

    @Test
    /**
     * unicode2String方法。
     */
    public void unicode2String() {
    }

    @Test
    /**
     * hasPrefix方法。
     */
    public void hasPrefix() {
    }

    @Test
    /**
     * hasSuffix方法。
     */
    public void hasSuffix() {
    }

    @Test
    /**
     * testFormatNumber方法。
     */
    public void testFormatNumber() {
    }

    @Test
    /**
     * hasLength方法。
     */
    public void hasLength() {
    }

    @Test
    /**
     * toUnderScoreCase方法。
     */
    public void toUnderScoreCase() {
    }

    @Test
    /**
     * isNotEmpty方法。
     */
    public void isNotEmpty() {
    }

    @Test
    /**
     * formatDouble方法。
     */
    public void formatDouble() {
    }

    @Test
    /**
     * isFormat方法。
     */
    public void isFormat() {
    }

    @Test
    /**
     * testBase64Decode方法。
     */
    public void testBase64Decode() {
        assert StringUtil.base64Decode("aGVsbG8=").equals("hello");
        assert StringUtil.base64Encode("hello").equals("aGVsbG8=");
    }

    @Test
    /**
     * testBinDecode方法。
     */
    public void testBinDecode() {

        assert StringUtil.binEncode("\0\1").equals("00000000000000000000000000000001");
        assert StringUtil.binDecode("00000000000000000000000000000001").equals("\0\1");
        assert StringUtil.decEncode("\0\1").equals("0000000001");
        assert StringUtil.decDecode("0000000001").equals("\0\1");
//      assert StringUtil.hexDecode("0110100001100101011011000110110001101111").equals("hello");

    }

    @Test
    /**
     * aaaa方法。
     */
    public void aaaa() {

        // 11011000  001111011  10111000  1001110
        System.out.println(StringUtil.binEncode("\uD83D\uDC4E"));

    }

    // --- formatPlaceholder ---

    @Test
    /**
     * testFormatPlaceholder_WithOrderedArgs方法。
     */
    public void testFormatPlaceholder_WithOrderedArgs() {
        assertEquals("a-b-c", StringUtil.formatPlaceholder("{}-{}-{}", "a", "b", "c"));
        assertEquals("name=tom,age=18", StringUtil.formatPlaceholder("name={},age={}", "tom", 18));
    }

    @Test
    /**
     * testFormatPlaceholder_WithInsufficientArgs方法。
     */
    public void testFormatPlaceholder_WithInsufficientArgs() {
        // 参数不足时剩余占位符保持原样，多余参数忽略
        assertEquals("a-{}", StringUtil.formatPlaceholder("{}-{}", "a"));
        assertEquals("a", StringUtil.formatPlaceholder("{}", "a", "b"));
    }

    @Test
    /**
     * testFormatPlaceholder_WithNullAndEmpty方法。
     */
    public void testFormatPlaceholder_WithNullAndEmpty() {
        assertNull(StringUtil.formatPlaceholder(null, "a"));
        assertEquals("template", StringUtil.formatPlaceholder("template"));
        assertEquals("template", StringUtil.formatPlaceholder("template", (Object[]) null));
        assertEquals("null", StringUtil.formatPlaceholder("{}", (Object) null));
    }

    // --- replacePlaceholder ---

    @Test
    /**
     * testReplacePlaceholder_WithNamedParams方法。
     */
    public void testReplacePlaceholder_WithNamedParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "alice");
        params.put("city", "shanghai");
        assertEquals("user=alice,city=shanghai", StringUtil.replacePlaceholder("user=${name},city=${city}", params));
        // 同名占位符全部替换
        assertEquals("alice-alice", StringUtil.replacePlaceholder("${name}-${name}", params));
    }

    @Test
    /**
     * testReplacePlaceholder_MissingKeyKept方法。
     */
    public void testReplacePlaceholder_MissingKeyKept() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "alice");
        // 映射中不存在的 key 与取值为 null 的 key 均保持原样
        assertEquals("alice-${missing}", StringUtil.replacePlaceholder("${name}-${missing}", params));
        params.put("nil", null);
        assertEquals("${nil}", StringUtil.replacePlaceholder("${nil}", params));
    }

    @Test
    /**
     * testReplacePlaceholder_WithNullAndEmpty方法。
     */
    public void testReplacePlaceholder_WithNullAndEmpty() {
        assertNull(StringUtil.replacePlaceholder(null, null));
        assertEquals("template", StringUtil.replacePlaceholder("template", null));
        Map<String, Object> params = new HashMap<>();
        assertEquals("template", StringUtil.replacePlaceholder("template", params));
        params.put("name", "alice");
        // 替换值中的特殊字符不作为正则解释
        assertEquals("a$b.c", StringUtil.replacePlaceholder("${v}", Collections.singletonMap("v", "a$b.c")));
    }

    @Test
    /**
     * testReplacePlaceholder_CustomPrefixSuffix方法。自定义前后缀占位符替换。
     */
    public void testReplacePlaceholder_CustomPrefixSuffix() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "alice");
        params.put("city", "shanghai");
        // 默认风格 ${} 可由自定义前后缀表达
        assertEquals("user=alice,city=shanghai",
                StringUtil.replacePlaceholder("user=${name},city=${city}", params, "${", "}"));
        // 单字符前后缀 $
        assertEquals("a=bob", StringUtil.replacePlaceholder("a=$name$", Collections.singletonMap("name", "bob"), "$", "$"));
        // 同一模板多次替换与缺 key 保留原样
        assertEquals("alice-alice-${missing}",
                StringUtil.replacePlaceholder("${name}-${name}-${missing}", params, "${", "}"));
        // null 值占位符保留原样
        Map<String, Object> withNull = new HashMap<>();
        withNull.put("nil", null);
        assertEquals("keep ${nil}", StringUtil.replacePlaceholder("keep ${nil}", withNull, "${", "}"));
        // 后缀缺失时剩余内容原样保留
        assertEquals("alice ${unclosed", StringUtil.replacePlaceholder("${name} ${unclosed", params, "${", "}"));
        // 前后缀为 null 或空串时返回模板原样
        assertEquals("${name}", StringUtil.replacePlaceholder("${name}", params, null, "}"));
        assertEquals("${name}", StringUtil.replacePlaceholder("${name}", params, "", "}"));
        assertEquals("${name}", StringUtil.replacePlaceholder("${name}", params, "${", ""));
        // template 为 null 或 params 为空返回原样
        assertNull(StringUtil.replacePlaceholder(null, params, "${", "}"));
        assertEquals("${name}", StringUtil.replacePlaceholder("${name}", new HashMap<String, Object>(), "${", "}"));
        // 替换值中包含前后缀字符时不被二次替换
        assertEquals("x$y", StringUtil.replacePlaceholder("$v$", Collections.singletonMap("v", "x$y"), "$", "$"));
    }

    @Test
    /**
     * testIsContainChinese方法。包含中文字符即返回 true，纯 ASCII/空白/null 返回 false。
     */
    public void testIsContainChinese() {
        assertTrue(StringUtil.isContainChinese("中文"));
        assertTrue(StringUtil.isContainChinese("abc中123"));
        assertTrue(StringUtil.isContainChinese("。标点含汉字“测”"));
        assertFalse(StringUtil.isContainChinese("abc 123"));
        assertFalse(StringUtil.isContainChinese(""));
        assertFalse(StringUtil.isContainChinese(null));
        // 中文标点不在 CJK 统一表意符号区间，不视为汉字
        assertFalse(StringUtil.isContainChinese("。，！"));
    }
}