package com.zifang.util.dsl.g4.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G4RuleTest类。
 */
public class G4RuleTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        G4Rule rule = new G4Rule();
        assertNull(rule.getName());
        assertNull(rule.getType());
        assertNull(rule.getBody());
        assertFalse(rule.isFragment());
        assertFalse(rule.isHidden());
    }

    @Test
    /**
     * testFullConstructor方法。
     */
    public void testFullConstructor() {
        G4Rule rule = new G4Rule("ID", G4Rule.RuleType.LEXER, "[a-z]+", true);

        assertEquals("ID", rule.getName());
        assertEquals(G4Rule.RuleType.LEXER, rule.getType());
        assertEquals("[a-z]+", rule.getBody());
        assertTrue(rule.isFragment());
        assertFalse(rule.isHidden());
    }

    @Test
    /**
     * testThreeParamConstructor方法。
     */
    public void testThreeParamConstructor() {
        G4Rule rule = new G4Rule("expr", G4Rule.RuleType.PARSER, "term '+' term");

        assertEquals("expr", rule.getName());
        assertEquals(G4Rule.RuleType.PARSER, rule.getType());
        assertEquals("term '+' term", rule.getBody());
        assertFalse(rule.isFragment());
    }

    @Test
    /**
     * testSetName方法。
     */
    public void testSetName() {
        G4Rule rule = new G4Rule();
        rule.setName("TestRule");
        assertEquals("TestRule", rule.getName());
    }

    @Test
    /**
     * testSetType方法。
     */
    public void testSetType() {
        G4Rule rule = new G4Rule();
        rule.setType(G4Rule.RuleType.LEXER);
        assertEquals(G4Rule.RuleType.LEXER, rule.getType());
    }

    @Test
    /**
     * testSetBody方法。
     */
    public void testSetBody() {
        G4Rule rule = new G4Rule();
        rule.setBody("test body");
        assertEquals("test body", rule.getBody());
    }

    @Test
    /**
     * testSetFragment方法。
     */
    public void testSetFragment() {
        G4Rule rule = new G4Rule();
        assertFalse(rule.isFragment());

        rule.setFragment(true);
        assertTrue(rule.isFragment());

        rule.setFragment(false);
        assertFalse(rule.isFragment());
    }

    @Test
    /**
     * testSetHidden方法。
     */
    public void testSetHidden() {
        G4Rule rule = new G4Rule();
        assertFalse(rule.isHidden());

        rule.setHidden(true);
        assertTrue(rule.isHidden());

        rule.setHidden(false);
        assertFalse(rule.isHidden());
    }

    @Test
    /**
     * testIsFragment_LexerFragment方法。
     */
    public void testIsFragment_LexerFragment() {
        G4Rule rule = new G4Rule("LETTER", G4Rule.RuleType.LEXER, "[a-z]", true);
        assertTrue(rule.isFragment());
    }

    @Test
    /**
     * testIsFragment_NotFragment方法。
     */
    public void testIsFragment_NotFragment() {
        G4Rule rule = new G4Rule("ID", G4Rule.RuleType.LEXER, "[a-z]+", false);
        assertFalse(rule.isFragment());
    }

    @Test
    /**
     * testIsHidden_HiddenChannel方法。
     */
    public void testIsHidden_HiddenChannel() {
        G4Rule rule = new G4Rule("WS", G4Rule.RuleType.LEXER, "[ \\t\\n]+", false);
        rule.setHidden(true);
        assertTrue(rule.isHidden());
    }

    @Test
    /**
     * testIsHidden_NotHidden方法。
     */
    public void testIsHidden_NotHidden() {
        G4Rule rule = new G4Rule("ID", G4Rule.RuleType.LEXER, "[a-z]+", false);
        assertFalse(rule.isHidden());
    }

    @Test
    /**
     * testRuleType_LEXER方法。
     */
    public void testRuleType_LEXER() {
        G4Rule rule = new G4Rule("ID", G4Rule.RuleType.LEXER, "[a-z]+");
        assertEquals(G4Rule.RuleType.LEXER, rule.getType());
    }

    @Test
    /**
     * testRuleType_PARSER方法。
     */
    public void testRuleType_PARSER() {
        G4Rule rule = new G4Rule("expr", G4Rule.RuleType.PARSER, "term");
        assertEquals(G4Rule.RuleType.PARSER, rule.getType());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        G4Rule rule = new G4Rule("ID", G4Rule.RuleType.LEXER, "[a-z]+", false);
        String str = rule.toString();

        assertTrue(str.contains("ID"));
        assertTrue(str.contains("LEXER"));
        assertTrue(str.contains("[a-z]+"));
    }

    @Test
    /**
     * testToString_WithFragmentAndHidden方法。
     */
    public void testToString_WithFragmentAndHidden() {
        G4Rule rule = new G4Rule("LETTER", G4Rule.RuleType.LEXER, "[a-z]", true);
        rule.setHidden(true);
        String str = rule.toString();

        assertTrue(str.contains("LETTER"));
        assertTrue(str.contains("isFragment=true"));
    }

    @Test
    /**
     * testGetBody方法。
     */
    public void testGetBody() {
        G4Rule rule = new G4Rule("expr", G4Rule.RuleType.PARSER, "term '+' term");
        assertEquals("term '+' term", rule.getBody());
    }

    @Test
    /**
     * testGetName方法。
     */
    public void testGetName() {
        G4Rule rule = new G4Rule("MyRule", G4Rule.RuleType.PARSER, "content");
        assertEquals("MyRule", rule.getName());
    }

    @Test
    /**
     * testRuleTypeEnumValues方法。
     */
    public void testRuleTypeEnumValues() {
        G4Rule.RuleType[] types = G4Rule.RuleType.values();
        assertEquals(2, types.length);
        assertEquals(G4Rule.RuleType.LEXER, types[0]);
        assertEquals(G4Rule.RuleType.PARSER, types[1]);
    }

    @Test
    /**
     * testRuleTypeEnumValueOf方法。
     */
    public void testRuleTypeEnumValueOf() {
        assertEquals(G4Rule.RuleType.LEXER, G4Rule.RuleType.valueOf("LEXER"));
        assertEquals(G4Rule.RuleType.PARSER, G4Rule.RuleType.valueOf("PARSER"));
    }
}