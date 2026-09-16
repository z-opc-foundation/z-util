package com.zifang.util.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HtmlUtilTest类。
 */
public class HtmlUtilTest {

    // --- escape ---

    @Test
    /**
     * testEscape_WithNormalString方法。
     */
    public void testEscape_WithNormalString() throws Exception {
        String result = HtmlUtil.escape("hello", "UTF-8");
        assertEquals("hello", result);
    }

    @Test
    /**
     * testEscape_WithNull方法。
     */
    public void testEscape_WithNull() throws Exception {
        String result = HtmlUtil.escape(null, "UTF-8");
        assertEquals("", result);
    }

    @Test
    /**
     * testEscape_WithEmpty方法。
     */
    public void testEscape_WithEmpty() throws Exception {
        String result = HtmlUtil.escape("", "UTF-8");
        assertEquals("", result);
    }

    // --- unescape ---

    @Test
    /**
     * testUnescape_WithEncodedString方法。
     */
    public void testUnescape_WithEncodedString() throws Exception {
        String result = HtmlUtil.unescape("hello%20world", "UTF-8");
        assertEquals("hello world", result);
    }

    @Test
    /**
     * testUnescape_WithNull方法。
     */
    public void testUnescape_WithNull() {
        String result = HtmlUtil.unescape(null, "UTF-8");
        assertEquals("", result);
    }

    @Test
    /**
     * testUnescape_WithEmpty方法。
     */
    public void testUnescape_WithEmpty() {
        String result = HtmlUtil.unescape("", "UTF-8");
        assertEquals("", result);
    }

    // --- unhtml ---

    @Test
    /**
     * testUnhtml_WithNormalHtml方法。
     */
    public void testUnhtml_WithNormalHtml() {
        String result = HtmlUtil.unhtml("<div>test</div>");
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    /**
     * testUnhtml_WithQuotes方法。
     */
    public void testUnhtml_WithQuotes() {
        String result = HtmlUtil.unhtml("\"test\"");
        assertTrue(result.contains("&quot;"));
    }

    @Test
    /**
     * testUnhtml_WithSingleQuote方法。
     */
    public void testUnhtml_WithSingleQuote() {
        String result = HtmlUtil.unhtml("'test'");
        assertTrue(result.contains("&apos;"));
    }

    @Test
    /**
     * testUnhtml_WithNull方法。
     */
    public void testUnhtml_WithNull() {
        String result = HtmlUtil.unhtml(null);
        assertEquals("", result);
    }

    @Test
    /**
     * testUnhtml_WithEmpty方法。
     */
    public void testUnhtml_WithEmpty() {
        String result = HtmlUtil.unhtml("");
        assertEquals("", result);
    }

    // --- html ---

    @Test
    /**
     * testHtml_WithEscapedHtml方法。
     */
    public void testHtml_WithEscapedHtml() {
        String result = HtmlUtil.html("&lt;div&gt;");
        assertTrue(result.contains("<"));
        assertTrue(result.contains(">"));
    }

    @Test
    /**
     * testHtml_WithNull方法。
     */
    public void testHtml_WithNull() {
        String result = HtmlUtil.html(null);
        assertEquals("", result);
    }

    @Test
    /**
     * testHtml_WithEmpty方法。
     */
    public void testHtml_WithEmpty() {
        String result = HtmlUtil.html("");
        assertEquals("", result);
    }

    // --- replaceXSS ---

    @Test
    /**
     * testReplaceXSS_WithScriptTag方法。
     */
    public void testReplaceXSS_WithScriptTag() {
        String result = HtmlUtil.replaceXSS("<script>alert('xss')</script>");
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert"));
    }

    @Test
    /**
     * testReplaceXSS_WithJavascriptProtocol方法。
     */
    public void testReplaceXSS_WithJavascriptProtocol() {
        String result = HtmlUtil.replaceXSS("javascript:alert('xss')");
        assertFalse(result.contains("javascript:"));
    }

    @Test
    /**
     * testReplaceXSS_WithOnloadEvent方法。
     */
    public void testReplaceXSS_WithOnloadEvent() {
        String result = HtmlUtil.replaceXSS("<img src=x onload=alert('xss')>");
        assertFalse(result.contains("onload"));
    }

    @Test
    /**
     * testReplaceXSS_WithNull方法。
     */
    public void testReplaceXSS_WithNull() {
        String result = HtmlUtil.replaceXSS(null);
        assertNull(result);
    }

    @Test
    /**
     * testReplaceXSS_WithEvalExpression方法。
     */
    public void testReplaceXSS_WithEvalExpression() {
        String result = HtmlUtil.replaceXSS("eval(alert('xss'))");
        assertFalse(result.contains("eval"));
    }

    @Test
    /**
     * testReplaceXSS_WithExpression方法。
     */
    public void testReplaceXSS_WithExpression() {
        String result = HtmlUtil.replaceXSS("expression(alert('xss'))");
        assertFalse(result.contains("expression"));
    }

    @Test
    /**
     * testReplaceXSS_PreservesNormalText方法。
     */
    public void testReplaceXSS_PreservesNormalText() {
        String result = HtmlUtil.replaceXSS("hello world");
        assertEquals("hello world", result);
    }

    // --- filter ---

    @Test
    /**
     * testFilter_WithNormalString方法。
     */
    public void testFilter_WithNormalString() {
        String result = HtmlUtil.filter("hello world");
        assertEquals("hello world", result);
    }

    @Test
    /**
     * testFilter_WithHtmlChars方法。
     */
    public void testFilter_WithHtmlChars() {
        // filter() encodes < > " ' as HTML entities
        String result = HtmlUtil.filter("<div>test</div>");
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    /**
     * testFilter_WithQuotes方法。
     */
    public void testFilter_WithQuotes() {
        // filter() encodes " as &quot; and ' as &apos;
        String result = HtmlUtil.filter("\"test\"");
        assertTrue(result.contains("&quot;"));
        assertEquals("&quot;test&quot;", result);
    }

    @Test
    /**
     * testFilter_PreservesPercent方法。
     */
    public void testFilter_PreservesPercent() {
        // filter() does NOT encode % - it stays as %
        String result = HtmlUtil.filter("100%");
        assertTrue(result.contains("%"));
    }

    @Test
    /**
     * testFilter_PreservesSemicolon方法。
     */
    public void testFilter_PreservesSemicolon() {
        // filter() does NOT encode semicolon - it stays as ;
        String result = HtmlUtil.filter("a;b");
        assertTrue(result.contains(";"));
    }

    @Test
    /**
     * testFilter_WithNull方法。
     */
    public void testFilter_WithNull() {
        String result = HtmlUtil.filter(null);
        assertNull(result);
    }

    @Test
    /**
     * testFilter_MixedSpecialChars方法。
     */
    public void testFilter_MixedSpecialChars() {
        // filter() removes script tags, encodes < > and quotes
        String result = HtmlUtil.filter("<script>alert('xss')</script>");
        assertFalse(result.contains("<script>"));  // script tag removed
        assertFalse(result.contains("</script>"));  // closing tag removed
        assertFalse(result.contains("'"));  // ' is encoded to &apos;
    }

    @Test
    /**
     * testFilter_PreservesNormalChars方法。
     */
    public void testFilter_PreservesNormalChars() {
        String input = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String result = HtmlUtil.filter(input);
        assertEquals(input, result);
    }

    @Test
    /**
     * testFilter_EncodeRoundTrip方法。
     */
    public void testFilter_EncodeRoundTrip() {
        // unhtml should reverse unhtml
        String original = "<div>test</div>";
        String escaped = HtmlUtil.unhtml(original);
        String restored = HtmlUtil.html(escaped);
        assertEquals(original, restored);
    }
}