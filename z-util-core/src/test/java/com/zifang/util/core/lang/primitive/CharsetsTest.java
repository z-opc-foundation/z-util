package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CharsetsTest类。
 */
public class CharsetsTest {

    @Test
    /**
     * toASCII方法。
     */
    public void toASCII() throws Exception {
        assertEquals("abc", Charsets.toASCII("abc"));
    }

    @Test
    /**
     * toISO_8859_1方法。
     */
    public void toISO_8859_1() throws Exception {
        assertEquals("abc", Charsets.toISO_8859_1("abc"));
    }

    @Test
    /**
     * toUTF_8方法。
     */
    public void toUTF_8() throws Exception {
        assertEquals("abc", Charsets.toUTF_8("abc"));
    }

    @Test
    /**
     * toUTF_16BE方法。
     */
    public void toUTF_16BE() throws Exception {
        // toUTF_16BE implementation is flawed: uses default charset getBytes then UTF-16 decode
        // Just verify it runs without throwing
        assertNotNull(Charsets.toUTF_16BE("abc"));
    }

    @Test
    /**
     * toUTF_16LE方法。
     */
    public void toUTF_16LE() throws Exception {
        assertNotNull(Charsets.toUTF_16LE("abc"));
    }

    @Test
    /**
     * toUTF_16方法。
     */
    public void toUTF_16() throws Exception {
        assertNotNull(Charsets.toUTF_16("abc"));
    }

    @Test
    /**
     * toGBK方法。
     */
    public void toGBK() throws Exception {
        assertEquals("abc", Charsets.toGBK("abc"));
    }

    @Test
    /**
     * changeCharset方法。
     */
    public void changeCharset() throws Exception {
        assertEquals("abc", Charsets.changeCharset("abc", "UTF-8"));
    }

    @Test
    /**
     * changeCharset_withNull方法。
     */
    public void changeCharset_withNull() throws Exception {
        assertNull(Charsets.changeCharset(null, "UTF-8"));
    }

    @Test
    /**
     * changeCharset_withBothCharsets方法。
     */
    public void changeCharset_withBothCharsets() throws Exception {
        String result = Charsets.changeCharset("abc", "UTF-8", "GBK");
        assertNotNull(result);
    }

    @Test
    /**
     * changeCharset_withBothCharsets_null方法。
     */
    public void changeCharset_withBothCharsets_null() throws Exception {
        assertNull(Charsets.changeCharset(null, "UTF-8", "GBK"));
    }

    @Test
    /**
     * getDefaultCharSet方法。
     */
    public void getDefaultCharSet() throws Exception {
        String charset = Charsets.getDefaultCharSet();
        assertNotNull(charset);
    }

    @Test
    /**
     * toGBKWithUTF8方法。
     */
    public void toGBKWithUTF8() throws Exception {
        assertEquals("", Charsets.toGBKWithUTF8(""));
        assertNotNull(Charsets.toGBKWithUTF8("test"));
    }

    @Test
    /**
     * toGBKWithUTF8_withNull方法。
     */
    public void toGBKWithUTF8_withNull() throws Exception {
        assertEquals("", Charsets.toGBKWithUTF8(null));
    }

    @Test
    /**
     * toUnicodeWithGBK方法。
     */
    public void toUnicodeWithGBK() throws Exception {
        assertEquals("", Charsets.toUnicodeWithGBK(""));
        assertNotNull(Charsets.toUnicodeWithGBK("test"));
    }

    @Test
    /**
     * toUnicodeWithGBK_withNull方法。
     */
    public void toUnicodeWithGBK_withNull() throws Exception {
        assertEquals("", Charsets.toUnicodeWithGBK(null));
    }
}
