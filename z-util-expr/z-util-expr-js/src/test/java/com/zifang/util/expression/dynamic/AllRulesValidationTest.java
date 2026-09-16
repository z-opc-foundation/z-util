package com.zifang.util.expression.dynamic;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;
import org.junit.Test;

import java.util.List;

/**
 * 全面验证所有 G4 词法规则的正确性
 * 覆盖 JSParser.g4 中的每一条 lexer 规则
 */
public class AllRulesValidationTest {

    private DynamicLexer createLexer() throws Exception {
        String g4Path = "src/main/resources/g4/JSParser.g4";
        String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(g4Path)));
        int lexerStart = content.indexOf("lexer grammar");
        int parserStart = content.indexOf("parser grammar");
        String lexerSection = parserStart == -1
                ? content.substring(lexerStart)
                : content.substring(lexerStart, parserStart);
        DynamicLexer lexer = new DynamicLexer();
        lexer.loadG4(lexerSection);
        return lexer;
    }

    // ==================== 空白字符测试 ====================

    @Test
    public void testSpace() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("   \t\t  \r\n\r\n");
        List<Token> tokens = lexer.tokenize();
        // Space is HIDDEN - should produce no tokens
        org.junit.Assert.assertTrue("Space should be hidden", tokens.isEmpty());
    }

    // ==================== 注释测试 ====================

    @Test
    public void testLineComment() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("// this is a line comment\nvar x = 1;");
        List<Token> tokens = lexer.tokenize();
        // LineComment is HIDDEN - should only see var, x, =, 1, ;
        org.junit.Assert.assertFalse("Should have tokens", tokens.isEmpty());
        org.junit.Assert.assertEquals("First token should be Var", "Var", tokens.get(0).getTokenName());
    }

    @Test
    public void testBlockComment() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("/* multi\nline\ncomment */var x = 1;");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertFalse("Should have tokens", tokens.isEmpty());
        org.junit.Assert.assertEquals("First token should be Var", "Var", tokens.get(0).getTokenName());
    }

    // ==================== 关键字测试 ====================

    @Test
    public void testAllKeywords() throws Exception {
        DynamicLexer lexer = createLexer();
        String keywords = "var let const function return if else for while do break continue true false null undefined";
        lexer.setInput(keywords);
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 16 keyword tokens", 16, tokens.size());
    }

    // ==================== 标识符测试 ====================

    @Test
    public void testIdentifier() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("foo _bar baz123 _123 ABC xyz XYZ");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 7 identifiers", 7, tokens.size());
        for (Token t : tokens) {
            org.junit.Assert.assertEquals("Expected Identifier", "Identifier", t.getTokenName());
        }
    }

    @Test
    public void testIdentifierNotStartingWithDigit() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("123abc"); // should tokenize as IntLiteral then Identifier
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 2 tokens", 2, tokens.size());
        org.junit.Assert.assertEquals("First should be IntLiteral", "IntLiteral", tokens.get(0).getTokenName());
        org.junit.Assert.assertEquals("Second should be Identifier", "Identifier", tokens.get(1).getTokenName());
    }

    // ==================== 字符串字面量测试 ====================

    @Test
    public void testStringLiteral() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"hello world\" \"foo\\tbar\\n\" \"escape\\\\quote\\\"\" \"\"");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 4 string tokens", 4, tokens.size());
        for (Token t : tokens) {
            org.junit.Assert.assertEquals("Expected StringLiteral", "StringLiteral", t.getTokenName());
        }
    }

    @Test
    public void testSingleQuoteString() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("'hello world' 'foo\\'bar' '\\\\'");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 3 string tokens", 3, tokens.size());
        for (Token t : tokens) {
            org.junit.Assert.assertEquals("Expected SingleQuoteString", "SingleQuoteString", t.getTokenName());
        }
    }

    @Test
    public void testStringWithEscapeSequences() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"\\t\\r\\n\\\"\\\\\"");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 1 token", 1, tokens.size());
        org.junit.Assert.assertEquals("Expected StringLiteral", "StringLiteral", tokens.get(0).getTokenName());
    }

    @Test
    public void testStringUnclosed() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"unclosed string");
        try {
            lexer.tokenize();
            org.junit.Assert.fail("Should throw for unclosed string");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testSingleQuoteUnclosed() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("'unclosed");
        try {
            lexer.tokenize();
            org.junit.Assert.fail("Should throw for unclosed single quote");
        } catch (RuntimeException e) {
            // expected
        }
    }

    // ==================== 数字字面量测试 ====================

    @Test
    public void testIntLiteral() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("0 123 999 007");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 4 int tokens", 4, tokens.size());
        for (Token t : tokens) {
            org.junit.Assert.assertEquals("Expected IntLiteral", "IntLiteral", t.getTokenName());
        }
    }

    @Test
    public void testDecimalLiteral() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("0.0 1.234 99.99 .5");
        List<Token> tokens = lexer.tokenize();
        // 0.0, 1.234, 99.99 = DecimalLiteral; .5 = Dot + IntLiteral
        org.junit.Assert.assertEquals("Expected 5 tokens", 5, tokens.size());
    }

    @Test
    public void testHexLiteral() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("0x0 0x1F 0xabcdef 0xABCDEF");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 4 hex tokens", 4, tokens.size());
        for (Token t : tokens) {
            org.junit.Assert.assertEquals("Expected HexLiteral", "HexLiteral", t.getTokenName());
        }
    }

    // ==================== 运算符测试 ====================

    @Test
    public void testAllOperators() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("+ - * / % = < > <= >= == != && || ! & | ~ ^");
        List<Token> tokens = lexer.tokenize();
        // 19 operators: + - * / % = < > <= >= == != && || ! & | ~ ^
        // Note: <= and >= are single tokens, not two
        org.junit.Assert.assertEquals("Expected 19 operator tokens", 19, tokens.size());
    }

    @Test
    public void testCompoundOperators() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("<= >= == !=");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 4 tokens", 4, tokens.size());
        org.junit.Assert.assertEquals("LessEqual", tokens.get(0).getTokenName());
        org.junit.Assert.assertEquals("GreaterEqual", tokens.get(1).getTokenName());
        org.junit.Assert.assertEquals("Equal", tokens.get(2).getTokenName());
        org.junit.Assert.assertEquals("NotEqual", tokens.get(3).getTokenName());
    }

    // ==================== 界符测试 ====================

    @Test
    public void testAllDelimiters() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("(){}[];,.:?:");
        List<Token> tokens = lexer.tokenize();
        // 12 tokens: ( ) { } [ ] ; , . : ? :
        // Note: . and : are single tokens
        org.junit.Assert.assertEquals("Expected 12 delimiter tokens", 12, tokens.size());
    }

    // ==================== 复杂代码片段测试 ====================

    @Test
    public void testComplexCode() throws Exception {
        DynamicLexer lexer = createLexer();
        String code = "function fib(n) {\n" +
                "  if (n <= 1) return n;\n" +
                "  return fib(n - 1) + fib(n - 2);\n" +
                "}\n" +
                "var result = fib(10);";
        lexer.setInput(code);
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertTrue("Should have many tokens", tokens.size() > 20);
    }

    @Test
    public void testArrayAndObject() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("var arr = [1, 2, 3]; var obj = {name: \"test\", value: 99};");
        List<Token> tokens = lexer.tokenize();
        boolean hasLeftBracket = tokens.stream().anyMatch(t -> t.getTokenName().equals("LeftBracket"));
        boolean hasRightBracket = tokens.stream().anyMatch(t -> t.getTokenName().equals("RightBracket"));
        boolean hasLeftBrace = tokens.stream().anyMatch(t -> t.getTokenName().equals("LeftBrace"));
        boolean hasRightBrace = tokens.stream().anyMatch(t -> t.getTokenName().equals("RightBrace"));
        org.junit.Assert.assertTrue("Should have LeftBracket", hasLeftBracket);
        org.junit.Assert.assertTrue("Should have RightBracket", hasRightBracket);
        org.junit.Assert.assertTrue("Should have LeftBrace", hasLeftBrace);
        org.junit.Assert.assertTrue("Should have RightBrace", hasRightBrace);
    }

    @Test
    public void testTernaryOperator() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("var x = a > b ? a : b;");
        List<Token> tokens = lexer.tokenize();
        boolean hasQuestion = tokens.stream().anyMatch(t -> t.getTokenName().equals("Question"));
        boolean hasColon = tokens.stream().anyMatch(t -> t.getTokenName().equals("Colon"));
        org.junit.Assert.assertTrue("Should have Question", hasQuestion);
        org.junit.Assert.assertTrue("Should have Colon", hasColon);
    }

    @Test
    public void testStringWithNewline() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"line1\nline2\"");
        try {
            lexer.tokenize();
            org.junit.Assert.fail("Should throw for string with newline");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testEmptyString() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"\" ''");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 2 tokens", 2, tokens.size());
    }

    @Test
    public void testEscapeInCharClass() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"abc def\"");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 1 token", 1, tokens.size());
        org.junit.Assert.assertEquals("StringLiteral", tokens.get(0).getTokenName());
    }

    @Test
    public void testHexEscape() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("\"\\x41\\x42\"");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 1 token", 1, tokens.size());
        org.junit.Assert.assertEquals("StringLiteral", tokens.get(0).getTokenName());
    }

    @Test
    public void testBitwiseOperators() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("x & y | z ^ w ~n");
        List<Token> tokens = lexer.tokenize();
        boolean hasBitAnd = tokens.stream().anyMatch(t -> t.getTokenName().equals("BitAnd"));
        boolean hasBitOr = tokens.stream().anyMatch(t -> t.getTokenName().equals("BitOr"));
        boolean hasXor = tokens.stream().anyMatch(t -> t.getTokenName().equals("Xor"));
        boolean hasBitNot = tokens.stream().anyMatch(t -> t.getTokenName().equals("BitNot"));
        org.junit.Assert.assertTrue("Should have BitAnd", hasBitAnd);
        org.junit.Assert.assertTrue("Should have BitOr", hasBitOr);
        org.junit.Assert.assertTrue("Should have Xor", hasXor);
        org.junit.Assert.assertTrue("Should have BitNot", hasBitNot);
    }

    @Test
    public void testForLoop() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("for (var i = 0; i < 10; i++) { sum += i; }");
        List<Token> tokens = lexer.tokenize();
        boolean hasFor = tokens.stream().anyMatch(t -> t.getTokenName().equals("For"));
        boolean hasSemiColon = tokens.stream().anyMatch(t -> t.getTokenName().equals("SemiColon"));
        org.junit.Assert.assertTrue("Should have For", hasFor);
        org.junit.Assert.assertTrue("Should have SemiColon", hasSemiColon);
    }

    @Test
    public void testWhileAndDoWhile() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("while (x > 0) { x--; } do { x++; } while (x < 10);");
        List<Token> tokens = lexer.tokenize();
        boolean hasWhile = tokens.stream().anyMatch(t -> t.getTokenName().equals("While"));
        boolean hasDo = tokens.stream().anyMatch(t -> t.getTokenName().equals("Do"));
        org.junit.Assert.assertTrue("Should have While", hasWhile);
        org.junit.Assert.assertTrue("Should have Do", hasDo);
    }

    @Test
    public void testBreakContinue() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("break; continue;");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 4 tokens", 4, tokens.size());
        org.junit.Assert.assertEquals("Break", tokens.get(0).getTokenName());
        org.junit.Assert.assertEquals("Continue", tokens.get(2).getTokenName());
    }

    @Test
    public void testDotOperator() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("obj.property method()");
        List<Token> tokens = lexer.tokenize();
        boolean hasDot = tokens.stream().anyMatch(t -> t.getTokenName().equals("Dot"));
        org.junit.Assert.assertTrue("Should have Dot", hasDot);
    }

    @Test
    public void testCommaOperator() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("var x = 1, y = 2, z = 3;");
        List<Token> tokens = lexer.tokenize();
        long commaCount = tokens.stream().filter(t -> t.getTokenName().equals("Comma")).count();
        org.junit.Assert.assertEquals("Expected 2 commas", 2, commaCount);
    }

    // ==================== 边界错误测试 ====================

    @Test
    public void testUnknownCharacter() throws Exception {
        DynamicLexer lexer = createLexer();
        try {
            lexer.setInput("@");
            lexer.tokenize();
            org.junit.Assert.fail("Should throw for unknown char");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testIntLiteralOverflow() throws Exception {
        DynamicLexer lexer = createLexer();
        lexer.setInput("123456789012345678901234567890");
        List<Token> tokens = lexer.tokenize();
        org.junit.Assert.assertEquals("Expected 1 token", 1, tokens.size());
        org.junit.Assert.assertEquals("IntLiteral", tokens.get(0).getTokenName());
    }
}
