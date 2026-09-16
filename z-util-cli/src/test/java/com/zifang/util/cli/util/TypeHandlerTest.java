package com.zifang.util.cli.util;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for TypeHandler.
 * Tests type conversion for numbers, dates, URI, URL, File, Class, and cast methods.
 */

/**
 * TypeHandlerTest类。
 */
public class TypeHandlerTest {

    // ==================== toNumber tests ====================

    @Test
    /**
     * testToNumberInteger方法。
     */
    public void testToNumberInteger() throws ParseException {
        Number result = TypeHandler.toNumber("123", Integer.class);
        assertEquals(123, result.intValue());
    }

    @Test
    /**
     * testToNumberIntegerNegative方法。
     */
    public void testToNumberIntegerNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-456", Integer.class);
        assertEquals(-456, result.intValue());
    }

    @Test
    /**
     * testToNumberIntegerHex方法。
     */
    public void testToNumberIntegerHex() throws ParseException {
        Number result = TypeHandler.toNumber("0xFF", Integer.class);
        assertEquals(255, result.intValue());
    }

    @Test
    /**
     * testToNumberIntPrimitive方法。
     */
    public void testToNumberIntPrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("42", Integer.TYPE);
        assertEquals(42, result.intValue());
    }

    @Test
    /**
     * testToNumberLong方法。
     */
    public void testToNumberLong() throws ParseException {
        Number result = TypeHandler.toNumber("9223372036854775807", Long.class);
        assertEquals(9223372036854775807L, result.longValue());
    }

    @Test
    /**
     * testToNumberLongNegative方法。
     */
    public void testToNumberLongNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-999999999999", Long.class);
        assertEquals(-999999999999L, result.longValue());
    }

    @Test
    /**
     * testToNumberLongPrimitive方法。
     */
    public void testToNumberLongPrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("100", Long.TYPE);
        assertEquals(100L, result.longValue());
    }

    @Test
    /**
     * testToNumberDouble方法。
     */
    public void testToNumberDouble() throws ParseException {
        Number result = TypeHandler.toNumber("3.14159", Double.class);
        assertEquals(3.14159, result.doubleValue(), 0.00001);
    }

    @Test
    /**
     * testToNumberDoubleNegative方法。
     */
    public void testToNumberDoubleNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-2.71828", Double.class);
        assertEquals(-2.71828, result.doubleValue(), 0.00001);
    }

    @Test
    /**
     * testToNumberDoublePrimitive方法。
     */
    public void testToNumberDoublePrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("1.5", Double.TYPE);
        assertEquals(1.5, result.doubleValue(), 0.00001);
    }

    @Test
    /**
     * testToNumberFloat方法。
     */
    public void testToNumberFloat() throws ParseException {
        Number result = TypeHandler.toNumber("2.5", Float.class);
        assertEquals(2.5f, result.floatValue(), 0.0001f);
    }

    @Test
    /**
     * testToNumberFloatNegative方法。
     */
    public void testToNumberFloatNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-1.0", Float.class);
        assertEquals(-1.0f, result.floatValue(), 0.0001f);
    }

    @Test
    /**
     * testToNumberFloatPrimitive方法。
     */
    public void testToNumberFloatPrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("0.5", Float.TYPE);
        assertEquals(0.5f, result.floatValue(), 0.0001f);
    }

    @Test
    /**
     * testToNumberShort方法。
     */
    public void testToNumberShort() throws ParseException {
        Number result = TypeHandler.toNumber("100", Short.class);
        assertEquals((short) 100, result.shortValue());
    }

    @Test
    /**
     * testToNumberShortNegative方法。
     */
    public void testToNumberShortNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-50", Short.class);
        assertEquals((short) -50, result.shortValue());
    }

    @Test
    /**
     * testToNumberShortPrimitive方法。
     */
    public void testToNumberShortPrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("32", Short.TYPE);
        assertEquals((short) 32, result.shortValue());
    }

    @Test
    /**
     * testToNumberByte方法。
     */
    public void testToNumberByte() throws ParseException {
        Number result = TypeHandler.toNumber("127", Byte.class);
        assertEquals((byte) 127, result.byteValue());
    }

    @Test
    /**
     * testToNumberByteNegative方法。
     */
    public void testToNumberByteNegative() throws ParseException {
        Number result = TypeHandler.toNumber("-128", Byte.class);
        assertEquals((byte) -128, result.byteValue());
    }

    @Test
    /**
     * testToNumberBytePrimitive方法。
     */
    public void testToNumberBytePrimitive() throws ParseException {
        Number result = TypeHandler.toNumber("64", Byte.TYPE);
        assertEquals((byte) 64, result.byteValue());
    }

    @Test(expected = ParseException.class)
    /**
     * testToNumberUnsupportedType方法。
     */
    public void testToNumberUnsupportedType() throws ParseException {
        TypeHandler.toNumber("123", String.class);
    }

    @Test
    /**
     * testToNumberNull方法。
     */
    public void testToNumberNull() throws ParseException {
        Number result = TypeHandler.toNumber(null, Integer.class);
        assertNull(result);
    }

    // ==================== toDate tests ====================

    @Test
    /**
     * testToDateWithPattern方法。
     */
    public void testToDateWithPattern() throws ParseException {
        Date result = TypeHandler.toDate("2024-01-15", "yyyy-MM-dd");
        assertNotNull(result);
        assertEquals(2024 - 1900, result.getYear());
        assertEquals(0, result.getMonth());
        assertEquals(15, result.getDate());
    }

    @Test
    /**
     * testToDateWithPatternComplex方法。
     */
    public void testToDateWithPatternComplex() throws ParseException {
        Date result = TypeHandler.toDate("15/01/2024 13:45:30", "dd/MM/yyyy HH:mm:ss");
        assertNotNull(result);
        assertEquals(2024 - 1900, result.getYear());
        assertEquals(0, result.getMonth());
        assertEquals(15, result.getDate());
    }

    @Test
    /**
     * testToDateNull方法。
     */
    public void testToDateNull() throws ParseException {
        Date result = TypeHandler.toDate(null, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test(expected = ParseException.class)
    /**
     * testToDateInvalidFormat方法。
     */
    public void testToDateInvalidFormat() throws ParseException {
        TypeHandler.toDate("not-a-date", "yyyy-MM-dd");
    }

    @Test
    /**
     * testToDateDefault方法。
     */
    public void testToDateDefault() throws ParseException {
        // Default format uses DateFormat.getDateInstance() which typically produces something like "Jan 15, 2024"
        // Using a more standard format for testing
        Date result = TypeHandler.toDate("Jan 15, 2024");
        assertNotNull(result);
    }

    @Test
    /**
     * testToDateDefaultNull方法。
     */
    public void testToDateDefaultNull() throws ParseException {
        Date result = TypeHandler.toDate(null);
        assertNull(result);
    }

    // ==================== toURI tests ====================

    @Test
    /**
     * testToURI方法。
     */
    public void testToURI() {
        URI result = TypeHandler.toURI("http://example.com/path?query=1");
        assertNotNull(result);
        assertEquals("http", result.getScheme());
        assertEquals("example.com", result.getHost());
        assertEquals("/path", result.getPath());
        assertEquals("query=1", result.getQuery());
    }

    @Test
    /**
     * testToURIWithFragment方法。
     */
    public void testToURIWithFragment() {
        URI result = TypeHandler.toURI("https://example.com/page#section");
        assertNotNull(result);
        assertEquals("section", result.getFragment());
    }

    @Test
    /**
     * testToURINull方法。
     */
    public void testToURINull() {
        URI result = TypeHandler.toURI(null);
        assertNull(result);
    }

    // ==================== toURL tests ====================

    @Test
    /**
     * testToURL方法。
     */
    public void testToURL() throws MalformedURLException {
        URL result = TypeHandler.toURL("http://example.com:8080/path");
        assertNotNull(result);
        assertEquals("http", result.getProtocol());
        assertEquals("example.com", result.getHost());
        assertEquals(8080, result.getPort());
        assertEquals("/path", result.getPath());
    }

    @Test
    /**
     * testToURLHttps方法。
     */
    public void testToURLHttps() throws MalformedURLException {
        URL result = TypeHandler.toURL("https://secure.example.com/api");
        assertNotNull(result);
        assertEquals("https", result.getProtocol());
    }

    @Test
    /**
     * testToURLNull方法。
     */
    public void testToURLNull() throws MalformedURLException {
        URL result = TypeHandler.toURL(null);
        assertNull(result);
    }

    @Test(expected = MalformedURLException.class)
    /**
     * testToURLInvalid方法。
     */
    public void testToURLInvalid() throws MalformedURLException {
        TypeHandler.toURL("not-a-valid-url");
    }

    // ==================== toFile tests ====================

    @Test
    /**
     * testToFile方法。
     */
    public void testToFile() {
        File result = TypeHandler.toFile("/tmp/test.txt");
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
        assertEquals("/tmp", result.getParent());
    }

    @Test
    /**
     * testToFileRelative方法。
     */
    public void testToFileRelative() {
        File result = TypeHandler.toFile("relative/path/file.txt");
        assertNotNull(result);
        assertEquals("file.txt", result.getName());
    }

    @Test
    /**
     * testToFileNull方法。
     */
    public void testToFileNull() {
        File result = TypeHandler.toFile(null);
        assertNull(result);
    }

    // ==================== toClass tests ====================

    @Test
    /**
     * testToClassString方法。
     */
    public void testToClassString() throws ClassNotFoundException {
        Class<?> result = TypeHandler.toClass("java.lang.String");
        assertEquals(String.class, result);
    }

    @Test
    /**
     * testToClassInteger方法。
     */
    public void testToClassInteger() throws ClassNotFoundException {
        Class<?> result = TypeHandler.toClass("java.lang.Integer");
        assertEquals(Integer.class, result);
    }

    @Test(expected = ClassNotFoundException.class)
    /**
     * testToClassNotFound方法。
     */
    public void testToClassNotFound() throws ClassNotFoundException {
        TypeHandler.toClass("com.nonexistent.ClassName");
    }

    @Test
    /**
     * testToClassNull方法。
     */
    public void testToClassNull() throws ClassNotFoundException {
        Class<?> result = TypeHandler.toClass(null);
        assertNull(result);
    }

    // ==================== isNumber tests ====================

    @Test
    /**
     * testIsNumberPositive方法。
     */
    public void testIsNumberPositive() {
        assertTrue(TypeHandler.isNumber("123"));
    }

    @Test
    /**
     * testIsNumberNegative方法。
     */
    public void testIsNumberNegative() {
        assertTrue(TypeHandler.isNumber("-456"));
    }

    @Test
    /**
     * testIsNumberDecimal方法。
     */
    public void testIsNumberDecimal() {
        assertTrue(TypeHandler.isNumber("3.14159"));
    }

    @Test
    /**
     * testIsNumberNegativeDecimal方法。
     */
    public void testIsNumberNegativeDecimal() {
        assertTrue(TypeHandler.isNumber("-2.718"));
    }

    @Test
    /**
     * testIsNumberFalse方法。
     */
    public void testIsNumberFalse() {
        assertFalse(TypeHandler.isNumber("abc"));
    }

    @Test
    /**
     * testIsNumberFalseMixed方法。
     */
    public void testIsNumberFalseMixed() {
        assertFalse(TypeHandler.isNumber("12abc"));
    }

    @Test
    /**
     * testIsNumberEmpty方法。
     */
    public void testIsNumberEmpty() {
        assertFalse(TypeHandler.isNumber(""));
    }

    @Test
    /**
     * testIsNumberNull方法。
     */
    public void testIsNumberNull() {
        assertFalse(TypeHandler.isNumber(null));
    }

    // ==================== cast tests ====================

    @Test
    /**
     * testCastString方法。
     */
    public void testCastString() throws ParseException, MalformedURLException, ClassNotFoundException {
        String result = TypeHandler.cast("hello", String.class);
        assertEquals("hello", result);
    }

    @Test
    /**
     * testCastBooleanTrue方法。
     */
    public void testCastBooleanTrue() throws ParseException, MalformedURLException, ClassNotFoundException {
        Boolean result = TypeHandler.cast("true", Boolean.class);
        assertTrue(result);
    }

    @Test
    /**
     * testCastBooleanFalse方法。
     */
    public void testCastBooleanFalse() throws ParseException, MalformedURLException, ClassNotFoundException {
        Boolean result = TypeHandler.cast("false", Boolean.class);
        assertFalse(result);
    }

    @Test
    /**
     * testCastBooleanUpperCase方法。
     */
    public void testCastBooleanUpperCase() throws ParseException, MalformedURLException, ClassNotFoundException {
        Boolean result = TypeHandler.cast("TRUE", Boolean.class);
        assertTrue(result);
    }

    @Test
    /**
     * testCastInteger方法。
     */
    public void testCastInteger() throws ParseException, MalformedURLException, ClassNotFoundException {
        Integer result = TypeHandler.cast("42", Integer.class);
        assertEquals(42, result.intValue());
    }

    @Test
    /**
     * testCastLong方法。
     */
    public void testCastLong() throws ParseException, MalformedURLException, ClassNotFoundException {
        Long result = TypeHandler.cast("9223372036854775807", Long.class);
        assertEquals(9223372036854775807L, result.longValue());
    }

    @Test
    /**
     * testCastDouble方法。
     */
    public void testCastDouble() throws ParseException, MalformedURLException, ClassNotFoundException {
        Double result = TypeHandler.cast("3.14", Double.class);
        assertEquals(3.14, result, 0.01);
    }

    @Test
    /**
     * testCastFloat方法。
     */
    public void testCastFloat() throws ParseException, MalformedURLException, ClassNotFoundException {
        Float result = TypeHandler.cast("2.5", Float.class);
        assertEquals(2.5f, result, 0.01f);
    }

    @Test
    /**
     * testCastShort方法。
     */
    public void testCastShort() throws ParseException, MalformedURLException, ClassNotFoundException {
        Short result = TypeHandler.cast("100", Short.class);
        assertEquals((short) 100, result.shortValue());
    }

    @Test
    /**
     * testCastByte方法。
     */
    public void testCastByte() throws ParseException, MalformedURLException, ClassNotFoundException {
        Byte result = TypeHandler.cast("127", Byte.class);
        assertEquals((byte) 127, result.byteValue());
    }

    @Test
    /**
     * testCastDate方法。
     */
    public void testCastDate() throws ParseException, MalformedURLException, ClassNotFoundException {
        Date result = TypeHandler.cast("Jan 15, 2024", Date.class);
        assertNotNull(result);
    }

    @Test
    /**
     * testCastURI方法。
     */
    public void testCastURI() throws ParseException, MalformedURLException, ClassNotFoundException {
        URI result = TypeHandler.cast("http://example.com", URI.class);
        assertNotNull(result);
        assertEquals("http", result.getScheme());
    }

    @Test
    /**
     * testCastURL方法。
     */
    public void testCastURL() throws ParseException, MalformedURLException, ClassNotFoundException {
        URL result = TypeHandler.cast("http://example.com", URL.class);
        assertNotNull(result);
        assertEquals("http", result.getProtocol());
    }

    @Test
    /**
     * testCastFile方法。
     */
    public void testCastFile() throws ParseException, MalformedURLException, ClassNotFoundException {
        File result = TypeHandler.cast("/tmp/test.txt", File.class);
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
    }

    @Test
    /**
     * testCastClass方法。
     */
    public void testCastClass() throws ParseException, MalformedURLException, ClassNotFoundException {
        Class<?> result = TypeHandler.cast("java.lang.String", Class.class);
        assertEquals(String.class, result);
    }

    @Test
    /**
     * testCastNull方法。
     */
    public void testCastNull() throws ParseException, MalformedURLException, ClassNotFoundException {
        String result = TypeHandler.cast(null, String.class);
        assertNull(result);
    }

    @Test(expected = ParseException.class)
    /**
     * testCastUnsupportedType方法。
     */
    public void testCastUnsupportedType() throws ParseException, MalformedURLException, ClassNotFoundException {
        TypeHandler.cast("value", Object.class);
    }
}