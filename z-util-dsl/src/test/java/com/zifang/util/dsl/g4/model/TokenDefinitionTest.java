package com.zifang.util.dsl.g4.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenDefinitionTest类。
 */
public class TokenDefinitionTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        TokenDefinition def = new TokenDefinition();
        assertNull(def.getName());
        assertNull(def.getPattern());
        assertEquals(0, def.getPrecedence());
        assertFalse(def.isFragment());
        assertFalse(def.isHidden());
    }

    @Test
    /**
     * testThreeParamConstructor方法。
     */
    public void testThreeParamConstructor() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", 1);

        assertEquals("ID", def.getName());
        assertEquals("[a-z]+", def.getPattern());
        assertEquals(1, def.getPrecedence());
    }

    @Test
    /**
     * testSetName方法。
     */
    public void testSetName() {
        TokenDefinition def = new TokenDefinition();
        def.setName("NUM");
        assertEquals("NUM", def.getName());
    }

    @Test
    /**
     * testSetPattern方法。
     */
    public void testSetPattern() {
        TokenDefinition def = new TokenDefinition();
        def.setPattern("[0-9]+");
        assertEquals("[0-9]+", def.getPattern());
    }

    @Test
    /**
     * testSetPrecedence方法。
     */
    public void testSetPrecedence() {
        TokenDefinition def = new TokenDefinition();
        def.setPrecedence(5);
        assertEquals(5, def.getPrecedence());
    }

    @Test
    /**
     * testSetFragment方法。
     */
    public void testSetFragment() {
        TokenDefinition def = new TokenDefinition();
        assertFalse(def.isFragment());

        def.setFragment(true);
        assertTrue(def.isFragment());

        def.setFragment(false);
        assertFalse(def.isFragment());
    }

    @Test
    /**
     * testSetHidden方法。
     */
    public void testSetHidden() {
        TokenDefinition def = new TokenDefinition();
        assertFalse(def.isHidden());

        def.setHidden(true);
        assertTrue(def.isHidden());

        def.setHidden(false);
        assertFalse(def.isHidden());
    }

    @Test
    /**
     * testIsFragment_True方法。
     */
    public void testIsFragment_True() {
        TokenDefinition def = new TokenDefinition("LETTER", "[a-z]", 0);
        def.setFragment(true);
        assertTrue(def.isFragment());
    }

    @Test
    /**
     * testIsFragment_False方法。
     */
    public void testIsFragment_False() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", 0);
        assertFalse(def.isFragment());
    }

    @Test
    /**
     * testIsHidden_True方法。
     */
    public void testIsHidden_True() {
        TokenDefinition def = new TokenDefinition("WS", "[ \\t\\n]+", 100);
        def.setHidden(true);
        assertTrue(def.isHidden());
    }

    @Test
    /**
     * testIsHidden_False方法。
     */
    public void testIsHidden_False() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", 0);
        assertFalse(def.isHidden());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", 1);
        String str = def.toString();

        assertTrue(str.contains("ID"));
        assertTrue(str.contains("[a-z]+"));
        assertTrue(str.contains("1"));
    }

    @Test
    /**
     * testToString_WithFragmentAndHidden方法。
     */
    public void testToString_WithFragmentAndHidden() {
        TokenDefinition def = new TokenDefinition("LETTER", "[a-z]", 0);
        def.setFragment(true);
        def.setHidden(true);
        String str = def.toString();

        assertTrue(str.contains("isFragment=true"));
        assertTrue(str.contains("hidden=true"));
    }

    @Test
    /**
     * testPrecedence_LowerNumberHigherPriority方法。
     */
    public void testPrecedence_LowerNumberHigherPriority() {
        TokenDefinition def1 = new TokenDefinition("KW", "if", 0);
        TokenDefinition def2 = new TokenDefinition("ID", "[a-z]+", 1);

        assertEquals(0, def1.getPrecedence());
        assertEquals(1, def2.getPrecedence());
    }

    @Test
    /**
     * testMultipleSetters方法。
     */
    public void testMultipleSetters() {
        TokenDefinition def = new TokenDefinition();
        def.setName("TEST");
        def.setPattern("test");
        def.setPrecedence(10);
        def.setFragment(true);
        def.setHidden(true);

        assertEquals("TEST", def.getName());
        assertEquals("test", def.getPattern());
        assertEquals(10, def.getPrecedence());
        assertTrue(def.isFragment());
        assertTrue(def.isHidden());
    }

    @Test
    /**
     * testConstructorWithZeroPrecedence方法。
     */
    public void testConstructorWithZeroPrecedence() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", 0);
        assertEquals(0, def.getPrecedence());
    }

    @Test
    /**
     * testConstructorWithNegativePrecedence方法。
     */
    public void testConstructorWithNegativePrecedence() {
        TokenDefinition def = new TokenDefinition("ID", "[a-z]+", -1);
        assertEquals(-1, def.getPrecedence());
    }

    @Test
    /**
     * testEmptyPattern方法。
     */
    public void testEmptyPattern() {
        TokenDefinition def = new TokenDefinition("EMPTY", "", 0);
        assertEquals("", def.getPattern());
    }

    @Test
    /**
     * testPatternWithSpecialChars方法。
     */
    public void testPatternWithSpecialChars() {
        TokenDefinition def = new TokenDefinition("SPECIAL", "\\[\\]", 0);
        assertEquals("\\[\\]", def.getPattern());
    }
}