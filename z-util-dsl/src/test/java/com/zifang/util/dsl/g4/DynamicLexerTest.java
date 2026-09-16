package com.zifang.util.dsl.g4;

import com.zifang.util.dsl.token.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DynamicLexerTest类。
 */
public class DynamicLexerTest {

    @Test
    /**
     * testLoadG4_BasicLexerRule方法。
     */
    public void testLoadG4_BasicLexerRule() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n" +
                "NUM : [0-9]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("hello");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("hello", tokens.get(0).getText());
        assertEquals("ID", tokens.get(0).getTokenName());
    }

    @Test
    /**
     * testLoadG4_MultipleTokens方法。
     */
    public void testLoadG4_MultipleTokens() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n" +
                "NUM : [0-9]+ ;\n" +
                "WS : [ \\t\\n]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("abc 123");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("abc", tokens.get(0).getText());
        assertEquals("123", tokens.get(1).getText());
    }

    @Test
    /**
     * testLoadG4_StringLiteral方法。
     */
    public void testLoadG4_StringLiteral() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "STRING : '\\\"' .*? '\\\"' ;\n";

        lexer.loadG4(g4);
        lexer.setInput("\"hello world\"");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("\"hello world\"", tokens.get(0).getText());
    }

    @Test
    /**
     * testLoadG4_Keywords方法。
     */
    public void testLoadG4_Keywords() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "IF : 'if' ;\n" +
                "ELSE : 'else' ;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("if else");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("if", tokens.get(0).getText());
        assertEquals("else", tokens.get(1).getText());
    }

    @Test
    /**
     * testSetInput_String方法。
     */
    public void testSetInput_String() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("test");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("test", tokens.get(0).getText());
    }

    @Test
    /**
     * testSetInput_CharArray方法。
     */
    public void testSetInput_CharArray() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput(new char[]{'h', 'e', 'l', 'l', 'o'});
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("hello", tokens.get(0).getText());
    }

    @Test
    /**
     * testTokenize_EmptyInput方法。
     */
    public void testTokenize_EmptyInput() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    /**
     * testTokenize_LineAndColumnTracking方法。
     */
    public void testTokenize_LineAndColumnTracking() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n" +
                "NEWLINE : '\\n' ;\n";

        lexer.loadG4(g4);
        lexer.setInput("hello\nworld");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        // 修复：换行符不再被 isWhitespace 跳过，会作为 NEWLINE token 发射
        assertEquals(3, tokens.size());
        assertEquals("ID", tokens.get(0).getTokenName());
        assertEquals(1, tokens.get(0).getLine());
        assertEquals("NEWLINE", tokens.get(1).getTokenName());
        assertEquals(1, tokens.get(1).getLine());
        assertEquals("ID", tokens.get(2).getTokenName());
        assertEquals(2, tokens.get(2).getLine());
    }

    @Test
    /**
     * testTokenize_NumberMatching方法。
     */
    public void testTokenize_NumberMatching() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "NUM : [0-9]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("42");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("42", tokens.get(0).getText());
    }

    @Test
    /**
     * testTokenize_UnknownCharacter方法。
     */
    public void testTokenize_UnknownCharacter() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("@");
        assertThrows(RuntimeException.class, () -> lexer.tokenize());
    }

    @Test
    /**
     * testLoadG4_HiddenTokens方法。
     */
    public void testLoadG4_HiddenTokens() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n" +
                "WS : [ \\t\\n]+ -> channel(HIDDEN) ;\n";

        lexer.loadG4(g4);
        lexer.setInput("hello world");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
    }

    @Test
    /**
     * testTokenize_FragmentRule方法。
     */
    public void testTokenize_FragmentRule() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "fragment LETTER : [a-z] ;\n" +
                "ID : LETTER+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("hello");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        assertEquals("hello", tokens.get(0).getText());
    }

    @Test
    /**
     * testTokenize_LongestMatchWins方法。
     */
    public void testTokenize_LongestMatchWins() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "IF : 'if' ;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("iffy");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        // Should match as ID, not IF + leftover
        assertEquals(1, tokens.size());
        assertEquals("iffy", tokens.get(0).getText());
    }

    @Test
    /**
     * testGetTokenReader方法。
     */
    public void testGetTokenReader() {
        DynamicLexer lexer = new DynamicLexer();
        String g4 = "lexer grammar Test;\n" +
                "ID : [a-z]+ ;\n";

        lexer.loadG4(g4);
        lexer.setInput("test");
        lexer.tokenize();

        assertNotNull(lexer.getTokenReader());
    }
}
