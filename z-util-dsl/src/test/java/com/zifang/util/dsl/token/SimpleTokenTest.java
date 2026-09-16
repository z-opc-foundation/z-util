package com.zifang.util.dsl.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleTokenTest类。
 */
public class SimpleTokenTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        SimpleToken token = new SimpleToken();
        assertNotNull(token);
        assertEquals(0, token.getType());
        assertNull(token.getText());
    }

    @Test
    /**
     * testParameterizedConstructor方法。
     */
    public void testParameterizedConstructor() {
        SimpleToken token = new SimpleToken(1, "test", 10, 20, "ID");

        assertEquals(1, token.getType());
        assertEquals("test", token.getText());
        assertEquals(10, token.getLine());
        assertEquals(20, token.getColumn());
        assertEquals("ID", token.getTokenName());
    }

    @Test
    /**
     * testGetSetType方法。
     */
    public void testGetSetType() {
        SimpleToken token = new SimpleToken();
        token.setType(5);
        assertEquals(5, token.getType());
    }

    @Test
    /**
     * testGetSetText方法。
     */
    public void testGetSetText() {
        SimpleToken token = new SimpleToken();
        token.setText("hello");
        assertEquals("hello", token.getText());
    }

    @Test
    /**
     * testGetSetLine方法。
     */
    public void testGetSetLine() {
        SimpleToken token = new SimpleToken();
        token.setLine(100);
        assertEquals(100, token.getLine());
    }

    @Test
    /**
     * testGetSetColumn方法。
     */
    public void testGetSetColumn() {
        SimpleToken token = new SimpleToken();
        token.setColumn(50);
        assertEquals(50, token.getColumn());
    }

    @Test
    /**
     * testGetSetTokenName方法。
     */
    public void testGetSetTokenName() {
        SimpleToken token = new SimpleToken();
        token.setTokenName("KEYWORD");
        assertEquals("KEYWORD", token.getTokenName());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        SimpleToken token = new SimpleToken(1, "test", 10, 20, "ID");
        String str = token.toString();

        assertTrue(str.contains("type=1"));
        assertTrue(str.contains("text='test'"));
        assertTrue(str.contains("line=10"));
        assertTrue(str.contains("column=20"));
        assertTrue(str.contains("tokenName='ID'"));
    }

    @Test
    /**
     * testToString_WithNullText方法。
     */
    public void testToString_WithNullText() {
        SimpleToken token = new SimpleToken(1, null, 10, 20, "ID");
        String str = token.toString();
        assertNotNull(str);
        assertTrue(str.contains("type=1"));
    }

    @Test
    /**
     * testTokenInterfaceMethods方法。
     */
    public void testTokenInterfaceMethods() {
        SimpleToken token = new SimpleToken(42, "content", 5, 15, "STRING");

        assertEquals(42, token.getType());
        assertEquals("content", token.getText());
        assertEquals(5, token.getLine());
        assertEquals(15, token.getColumn());
        assertEquals("STRING", token.getTokenName());
    }

    @Test
    /**
     * testMultipleSetters方法。
     */
    public void testMultipleSetters() {
        SimpleToken token = new SimpleToken();

        token.setType(10);
        token.setText("newText");
        token.setLine(1);
        token.setColumn(2);
        token.setTokenName("NEW_TOKEN");

        assertEquals(10, token.getType());
        assertEquals("newText", token.getText());
        assertEquals(1, token.getLine());
        assertEquals(2, token.getColumn());
        assertEquals("NEW_TOKEN", token.getTokenName());
    }
}
