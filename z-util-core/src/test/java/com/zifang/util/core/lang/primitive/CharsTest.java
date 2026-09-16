package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CharsTest类。
 */
public class CharsTest {

    @Test
    /**
     * isAlpha方法。
     */
    public void isAlpha() {
        assertTrue(Chars.isAlpha('a'));
        assertTrue(Chars.isAlpha('z'));
        assertTrue(Chars.isAlpha('A'));
        assertTrue(Chars.isAlpha('Z'));
        assertFalse(Chars.isAlpha('0'));
        assertFalse(Chars.isAlpha(' '));
        assertFalse(Chars.isAlpha('\n'));
    }

    @Test
    /**
     * isDigit方法。
     */
    public void isDigit() {
        assertTrue(Chars.isDigit('0'));
        assertTrue(Chars.isDigit('9'));
        assertFalse(Chars.isDigit('a'));
        assertFalse(Chars.isDigit(' '));
    }

    @Test
    /**
     * isBlank方法。
     */
    public void isBlank() {
        assertTrue(Chars.isBlank(' '));
        assertTrue(Chars.isBlank('\t'));
        assertTrue(Chars.isBlank('\n'));
        assertFalse(Chars.isBlank('a'));
        assertFalse(Chars.isBlank('0'));
    }

    @Test
    /**
     * toChar方法。
     */
    public void toChar() {
        assertEquals(255, Chars.toChar((byte) -1));
        assertEquals(0, Chars.toChar((byte) 0));
        assertEquals(1, Chars.toChar((byte) 1));
        assertEquals(127, Chars.toChar((byte) 127));
    }

    @Test
    /**
     * toSimpleByteArray_charArray方法。
     */
    public void toSimpleByteArray_charArray() {
        byte[] result = Chars.toSimpleByteArray(new char[]{'a', 'b', 'c'});
        assertEquals(3, result.length);
        assertEquals((byte) 'a', result[0]);
        assertEquals((byte) 'b', result[1]);
        assertEquals((byte) 'c', result[2]);
    }

    @Test
    /**
     * toSimpleByteArray_charSequence方法。
     */
    public void toSimpleByteArray_charSequence() {
        byte[] result = Chars.toSimpleByteArray("abc");
        assertEquals(3, result.length);
        assertEquals((byte) 'a', result[0]);
        assertEquals((byte) 'b', result[1]);
        assertEquals((byte) 'c', result[2]);
    }

    @Test
    /**
     * toSimpleCharArray方法。
     */
    public void toSimpleCharArray() {
        char[] result = Chars.toSimpleCharArray(new byte[]{'a', 'b', 'c'});
        assertEquals(3, result.length);
        assertEquals('a', result[0]);
        assertEquals('b', result[1]);
        assertEquals('c', result[2]);
    }

    @Test
    /**
     * toAscii方法。
     */
    public void toAscii() {
        assertEquals('a', Chars.toAscii('a'));
        assertEquals(0x3F, Chars.toAscii('\u0100'));
        assertEquals(0, Chars.toAscii((char) 0));
    }

    @Test
    /**
     * toAsciiByteArray_charArray方法。
     */
    public void toAsciiByteArray_charArray() {
        byte[] result = Chars.toAsciiByteArray(new char[]{'a', 'b', '\u0100'});
        assertEquals(3, result.length);
        assertEquals((byte) 'a', result[0]);
        assertEquals((byte) 'b', result[1]);
        assertEquals(0x3F, result[2]);
    }

    @Test
    /**
     * toAsciiByteArray_charSequence方法。
     */
    public void toAsciiByteArray_charSequence() {
        byte[] result = Chars.toAsciiByteArray("abc");
        assertEquals(3, result.length);
        assertEquals((byte) 'a', result[0]);
    }

    @Test
    /**
     * toRawByteArray方法。
     */
    public void toRawByteArray() {
        byte[] result = Chars.toRawByteArray(new char[]{0x0102});
        assertEquals(2, result.length);
        assertEquals(0x01, result[0] & 0xFF);
        assertEquals(0x02, result[1] & 0xFF);
    }

    @Test
    /**
     * toRawCharArray方法。
     */
    public void toRawCharArray() {
        byte[] barr = new byte[]{(byte) 0x01, (byte) 0x02};
        char[] result = Chars.toRawCharArray(barr);
        assertEquals(1, result.length);
        assertEquals(0x0102, result[0]);
    }

    @Test
    /**
     * toRawCharArray_oddLength方法。
     */
    public void toRawCharArray_oddLength() {
        byte[] barr = new byte[]{(byte) 0x01};
        char[] result = Chars.toRawCharArray(barr);
        assertEquals(1, result.length);
    }

    @Test
    /**
     * toByteArray方法。
     */
    public void toByteArray() throws Exception {
        char[] carr = new char[]{'a', 'b'};
        byte[] result = Chars.toByteArray(carr);
        assertEquals("ab", new String(result, "UTF-8"));
    }

    @Test
    /**
     * toByteArray_withCharset方法。
     */
    public void toByteArray_withCharset() throws Exception {
        char[] carr = new char[]{'a', 'b'};
        byte[] result = Chars.toByteArray(carr, "UTF-8");
        assertEquals("ab", new String(result, "UTF-8"));
    }

    @Test
    /**
     * toCharArray_fromBytes方法。
     */
    public void toCharArray_fromBytes() throws Exception {
        byte[] barr = "ab".getBytes("UTF-8");
        char[] result = Chars.toCharArray(barr);
        assertEquals(2, result.length);
        assertEquals('a', result[0]);
        assertEquals('b', result[1]);
    }

    @Test
    /**
     * toCharArray_fromBytes_withCharset方法。
     */
    public void toCharArray_fromBytes_withCharset() throws Exception {
        byte[] barr = "ab".getBytes("UTF-8");
        char[] result = Chars.toCharArray(barr, "UTF-8");
        assertEquals(2, result.length);
    }

    @Test
    /**
     * equalsOne方法。
     */
    public void equalsOne() {
        assertTrue(Chars.equalsOne('a', new char[]{'a', 'b', 'c'}));
        assertTrue(Chars.equalsOne('b', new char[]{'a', 'b', 'c'}));
        assertFalse(Chars.equalsOne('d', new char[]{'a', 'b', 'c'}));
    }

    @Test
    /**
     * findFirstEqual_charArray方法。
     */
    public void findFirstEqual_charArray() {
        char[] source = new char[]{'a', 'b', 'c', 'd'};
        assertEquals(1, Chars.findFirstEqual(source, 0, new char[]{'b', 'c'}));
        assertEquals(-1, Chars.findFirstEqual(source, 0, new char[]{'x', 'y'}));
        assertEquals(2, Chars.findFirstEqual(source, 2, new char[]{'c', 'd'}));
    }

    @Test
    /**
     * findFirstEqual_singleChar方法。
     */
    public void findFirstEqual_singleChar() {
        char[] source = new char[]{'a', 'b', 'c'};
        assertEquals(1, Chars.findFirstEqual(source, 0, 'b'));
        assertEquals(-1, Chars.findFirstEqual(source, 0, 'x'));
    }

    @Test
    /**
     * findFirstDiff_charArray方法。
     */
    public void findFirstDiff_charArray() {
        char[] source = new char[]{'a', 'a', 'a', 'b'};
        assertEquals(3, Chars.findFirstDiff(source, 0, new char[]{'a'}));
        assertEquals(-1, Chars.findFirstDiff(source, 0, new char[]{'a', 'b'}));
    }

    @Test
    /**
     * findFirstDiff_singleChar方法。
     */
    public void findFirstDiff_singleChar() {
        char[] source = new char[]{'a', 'a', 'b'};
        assertEquals(2, Chars.findFirstDiff(source, 0, 'a'));
        assertEquals(0, Chars.findFirstDiff(source, 0, 'b'));
    }

    @Test
    /**
     * isWhitespace方法。
     */
    public void isWhitespace() {
        assertTrue(Chars.isWhitespace(' '));
        assertTrue(Chars.isWhitespace('\t'));
        assertTrue(Chars.isWhitespace('\n'));
        assertTrue(Chars.isWhitespace('\r'));
        assertFalse(Chars.isWhitespace('a'));
        assertFalse(Chars.isWhitespace('0'));
    }

    @Test
    /**
     * isLowercaseAlpha方法。
     */
    public void isLowercaseAlpha() {
        assertTrue(Chars.isLowercaseAlpha('a'));
        assertTrue(Chars.isLowercaseAlpha('z'));
        assertFalse(Chars.isLowercaseAlpha('A'));
        assertFalse(Chars.isLowercaseAlpha('0'));
        assertFalse(Chars.isLowercaseAlpha(' '));
    }

    @Test
    /**
     * isUppercaseAlpha方法。
     */
    public void isUppercaseAlpha() {
        assertTrue(Chars.isUppercaseAlpha('A'));
        assertTrue(Chars.isUppercaseAlpha('Z'));
        assertFalse(Chars.isUppercaseAlpha('a'));
        assertFalse(Chars.isUppercaseAlpha('0'));
    }

    @Test
    /**
     * isAlphaOrDigit方法。
     */
    public void isAlphaOrDigit() {
        assertTrue(Chars.isAlphaOrDigit('a'));
        assertTrue(Chars.isAlphaOrDigit('0'));
        assertFalse(Chars.isAlphaOrDigit('_'));
        assertFalse(Chars.isAlphaOrDigit(' '));
    }

    @Test
    /**
     * isWordChar方法。
     */
    public void isWordChar() {
        assertTrue(Chars.isWordChar('a'));
        assertTrue(Chars.isWordChar('0'));
        assertTrue(Chars.isWordChar('_'));
        assertFalse(Chars.isWordChar('-'));
    }

    @Test
    /**
     * isPropertyNameChar方法。
     */
    public void isPropertyNameChar() {
        assertTrue(Chars.isPropertyNameChar('a'));
        assertTrue(Chars.isPropertyNameChar('0'));
        assertTrue(Chars.isPropertyNameChar('['));
        assertTrue(Chars.isPropertyNameChar(']'));
        assertFalse(Chars.isPropertyNameChar('-'));
    }

    @Test
    /**
     * isHexDigit方法。
     */
    public void isHexDigit() {
        assertTrue(Chars.isHexDigit('0'));
        assertTrue(Chars.isHexDigit('9'));
        assertTrue(Chars.isHexDigit('a'));
        assertTrue(Chars.isHexDigit('f'));
        assertTrue(Chars.isHexDigit('A'));
        assertTrue(Chars.isHexDigit('F'));
        assertFalse(Chars.isHexDigit('g'));
        assertFalse(Chars.isHexDigit(' '));
    }

    @Test
    /**
     * isGenericDelimiter方法。
     */
    public void isGenericDelimiter() {
        assertTrue(Chars.isGenericDelimiter(':'));
        assertTrue(Chars.isGenericDelimiter('/'));
        assertTrue(Chars.isGenericDelimiter('?'));
        assertTrue(Chars.isGenericDelimiter('#'));
        assertTrue(Chars.isGenericDelimiter('['));
        assertTrue(Chars.isGenericDelimiter(']'));
        assertTrue(Chars.isGenericDelimiter('@'));
        assertFalse(Chars.isGenericDelimiter('a'));
    }

    @Test
    /**
     * isSubDelimiter方法。
     */
    public void isSubDelimiter() {
        assertTrue(Chars.isSubDelimiter('!'));
        assertTrue(Chars.isSubDelimiter('$'));
        assertTrue(Chars.isSubDelimiter('&'));
        assertTrue(Chars.isSubDelimiter('\''));
        assertTrue(Chars.isSubDelimiter('*'));
        assertTrue(Chars.isSubDelimiter('+'));
        assertTrue(Chars.isSubDelimiter(','));
        assertTrue(Chars.isSubDelimiter(';'));
        assertTrue(Chars.isSubDelimiter('='));
        assertFalse(Chars.isSubDelimiter('a'));
    }

    @Test
    /**
     * isReserved方法。
     */
    public void isReserved() {
        assertTrue(Chars.isReserved(':'));
        assertTrue(Chars.isReserved('!'));
        assertFalse(Chars.isReserved('a'));
    }

    @Test
    /**
     * isUnreserved方法。
     */
    public void isUnreserved() {
        assertTrue(Chars.isUnreserved('a'));
        assertTrue(Chars.isUnreserved('0'));
        assertTrue(Chars.isUnreserved('-'));
        assertTrue(Chars.isUnreserved('.'));
        assertTrue(Chars.isUnreserved('_'));
        assertTrue(Chars.isUnreserved('~'));
        assertFalse(Chars.isUnreserved(':'));
    }

    @Test
    /**
     * isPchar方法。
     */
    public void isPchar() {
        assertTrue(Chars.isPchar('a'));
        assertTrue(Chars.isPchar(':'));
        assertTrue(Chars.isPchar('@'));
        assertFalse(Chars.isPchar('/'));
    }

    @Test
    /**
     * toUpperAscii方法。
     */
    public void toUpperAscii() {
        assertEquals('A', Chars.toUpperAscii('a'));
        assertEquals('Z', Chars.toUpperAscii('z'));
        assertEquals('A', Chars.toUpperAscii('A'));
        assertEquals('0', Chars.toUpperAscii('0'));
    }

    @Test
    /**
     * toLowerAscii方法。
     */
    public void toLowerAscii() {
        assertEquals('a', Chars.toLowerAscii('A'));
        assertEquals('z', Chars.toLowerAscii('Z'));
        assertEquals('a', Chars.toLowerAscii('a'));
        assertEquals('0', Chars.toLowerAscii('0'));
    }
}
