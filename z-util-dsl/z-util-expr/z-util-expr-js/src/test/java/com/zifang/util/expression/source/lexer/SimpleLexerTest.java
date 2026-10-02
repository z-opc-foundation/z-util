package com.zifang.util.expression.source.lexer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SimpleLexer 词法分析器完整测试
 */
public class SimpleLexerTest {

    // ==================== 辅助方法 ====================

    private List<Token> tokenize(String script) {
        SimpleLexer lexer = new SimpleLexer();
        SimpleTokenReader reader = lexer.tokenize(script);
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = reader.read()) != null) {
            tokens.add(token);
        }
        return tokens;
    }

    private void assertToken(List<Token> tokens, int index, TokenType expectedType, String expectedText) {
        assertTrue("Token index " + index + " out of bounds (got " + tokens.size() + " tokens)", index < tokens.size());
        Token token = tokens.get(index);
        assertEquals("Token " + index + " type", expectedType, token.getType());
        assertEquals("Token " + index + " text", expectedText, token.getText());
    }

    private int countTokens(List<Token> tokens) {
        return tokens.size();
    }

    // ==================== 整型字面量测试 ====================

    @Test
    public void testTokenizeSingleDigit() {
        List<Token> tokens = tokenize("5");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.IntLiteral, "5");
    }

    @Test
    public void testTokenizeMultiDigit() {
        List<Token> tokens = tokenize("123");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.IntLiteral, "123");
    }

    @Test
    public void testTokenizeLargeNumber() {
        List<Token> tokens = tokenize("9876543210");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.IntLiteral, "9876543210");
    }

    @Test
    public void testTokenizeZero() {
        List<Token> tokens = tokenize("0");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.IntLiteral, "0");
    }

    @Test
    public void testTokenizeMultipleNumbers() {
        List<Token> tokens = tokenize("10 20 30");
        assertEquals(3, countTokens(tokens));
        assertToken(tokens, 0, TokenType.IntLiteral, "10");
        assertToken(tokens, 1, TokenType.IntLiteral, "20");
        assertToken(tokens, 2, TokenType.IntLiteral, "30");
    }

    // ==================== 标识符测试 ====================

    @Test
    public void testTokenizeSimpleIdentifier() {
        List<Token> tokens = tokenize("abc");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "abc");
    }

    @Test
    public void testTokenizeIdentifierWithDigits() {
        List<Token> tokens = tokenize("var1");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "var1");
    }

    @Test
    public void testTokenizeIdentifierStartingWithUpperCase() {
        List<Token> tokens = tokenize("Age");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "Age");
    }

    @Test
    public void testTokenizeIdentifierAfterKeyword() {
        // "inta" 是标识符，不是 int 关键字
        List<Token> tokens = tokenize("inta");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "inta");
    }

    @Test
    public void testTokenizeMultipleIdentifiers() {
        List<Token> tokens = tokenize("a b c");
        assertEquals(3, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "a");
        assertToken(tokens, 1, TokenType.Identifier, "b");
        assertToken(tokens, 2, TokenType.Identifier, "c");
    }

    // ==================== 关键字测试 ====================

    @Test
    public void testTokenizeIntKeywordWithTrailingSpace() {
        // "int " (带空格) 识别为关键字
        List<Token> tokens = tokenize("int ");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Int, "int");
    }

    @Test
    public void testTokenizeIntKeywordWithFollowingIdentifier() {
        // "int age" 识别为: int关键字 + age标识符
        List<Token> tokens = tokenize("int age");
        assertEquals(2, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Int, "int");
        assertToken(tokens, 1, TokenType.Identifier, "age");
    }

    @Test
    public void testTokenizeIntAloneBecomesIdentifier() {
        // "int" 单独出现时，因为后面没有空格，被识别为标识符
        List<Token> tokens = tokenize("int");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "int");
    }

    @Test
    public void testTokenizeInFollowedByNonKeyword() {
        // "in" 后面不是 "t"，所以是标识符
        List<Token> tokens = tokenize("in");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "in");
    }

    // ==================== 运算符测试 ====================

    @Test
    public void testTokenizePlus() {
        List<Token> tokens = tokenize("+");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Plus, "+");
    }

    @Test
    public void testTokenizeMinus() {
        List<Token> tokens = tokenize("-");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Minus, "-");
    }

    @Test
    public void testTokenizeStar() {
        List<Token> tokens = tokenize("*");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Star, "*");
    }

    @Test
    public void testTokenizeSlash() {
        List<Token> tokens = tokenize("/");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Slash, "/");
    }

    @Test
    public void testTokenizeArithmeticExpression() {
        List<Token> tokens = tokenize("a + b - c * d / e");
        assertEquals(9, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "a");
        assertToken(tokens, 1, TokenType.Plus, "+");
        assertToken(tokens, 2, TokenType.Identifier, "b");
        assertToken(tokens, 3, TokenType.Minus, "-");
        assertToken(tokens, 4, TokenType.Identifier, "c");
        assertToken(tokens, 5, TokenType.Star, "*");
        assertToken(tokens, 6, TokenType.Identifier, "d");
        assertToken(tokens, 7, TokenType.Slash, "/");
        assertToken(tokens, 8, TokenType.Identifier, "e");
    }

    // ==================== 比较运算符测试 ====================

    @Test
    public void testTokenizeGT() {
        List<Token> tokens = tokenize("age > 45");
        assertEquals(3, countTokens(tokens));
        assertToken(tokens, 1, TokenType.GT, ">");
    }

    @Test
    public void testTokenizeGE() {
        List<Token> tokens = tokenize("age >= 45");
        assertEquals(3, countTokens(tokens));
        assertToken(tokens, 1, TokenType.GE, ">=");
    }

    @Test
    public void testTokenizeGTWithoutEquals() {
        // ">" 后紧跟空格，不会被误认为 ">="
        List<Token> tokens = tokenize("> ");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.GT, ">");
    }

    // ==================== 分隔符测试 ====================

    @Test
    public void testTokenizeSemicolon() {
        List<Token> tokens = tokenize(";");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.SemiColon, ";");
    }

    @Test
    public void testTokenizeLeftParen() {
        List<Token> tokens = tokenize("(");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.LeftParen, "(");
    }

    @Test
    public void testTokenizeRightParen() {
        List<Token> tokens = tokenize(")");
        assertEquals(1, countTokens(tokens));
        assertToken(tokens, 0, TokenType.RightParen, ")");
    }

    @Test
    public void testTokenizeParenthesesExpression() {
        List<Token> tokens = tokenize("(a + b)");
        assertEquals(5, countTokens(tokens));
        assertToken(tokens, 0, TokenType.LeftParen, "(");
        assertToken(tokens, 1, TokenType.Identifier, "a");
        assertToken(tokens, 2, TokenType.Plus, "+");
        assertToken(tokens, 3, TokenType.Identifier, "b");
        assertToken(tokens, 4, TokenType.RightParen, ")");
    }

    // ==================== 赋值运算符测试 ====================

    @Test
    public void testTokenizeAssignment() {
        List<Token> tokens = tokenize("age = 20");
        assertEquals(3, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "age");
        assertToken(tokens, 1, TokenType.Assignment, "=");
        assertToken(tokens, 2, TokenType.IntLiteral, "20");
    }

    // ==================== 空白字符测试 ====================

    @Test
    public void testTokenizeWithSpaces() {
        List<Token> tokens = tokenize("a + b");
        assertEquals(3, countTokens(tokens));
    }

    @Test
    public void testTokenizeWithTabs() {
        List<Token> tokens = tokenize("a\t+\tb");
        assertEquals(3, countTokens(tokens));
    }

    @Test
    public void testTokenizeWithNewlines() {
        List<Token> tokens = tokenize("a\n+\nb");
        assertEquals(3, countTokens(tokens));
    }

    @Test
    public void testTokenizeWithMultipleSpaces() {
        List<Token> tokens = tokenize("a    +    b");
        assertEquals(3, countTokens(tokens));
    }

    // ==================== 完整语句测试 ====================

    @Test
    public void testTokenizeVariableDeclaration() {
        List<Token> tokens = tokenize("int age = 45;");
        assertEquals(5, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Int, "int");
        assertToken(tokens, 1, TokenType.Identifier, "age");
        assertToken(tokens, 2, TokenType.Assignment, "=");
        assertToken(tokens, 3, TokenType.IntLiteral, "45");
        assertToken(tokens, 4, TokenType.SemiColon, ";");
    }

    @Test
    public void testTokenizeAssignmentStatement() {
        List<Token> tokens = tokenize("age = 20;");
        assertEquals(4, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "age");
        assertToken(tokens, 1, TokenType.Assignment, "=");
        assertToken(tokens, 2, TokenType.IntLiteral, "20");
        assertToken(tokens, 3, TokenType.SemiColon, ";");
    }

    @Test
    public void testTokenizeExpressionStatement() {
        // "age+10*2;" -> age + 10 * 2 ;
        List<Token> tokens = tokenize("age+10*2;");
        assertEquals(6, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Identifier, "age");
        assertToken(tokens, 1, TokenType.Plus, "+");
        assertToken(tokens, 2, TokenType.IntLiteral, "10");
        assertToken(tokens, 3, TokenType.Star, "*");
        assertToken(tokens, 4, TokenType.IntLiteral, "2");
        assertToken(tokens, 5, TokenType.SemiColon, ";");
    }

    @Test
    public void testTokenizeComplexStatement() {
        // int result = a + b * c - d / e;
        // 0:int 1:result 2:= 3:a 4:+ 5:b 6:* 7:c 8:- 9:d 10:/ 11:e 12:;
        List<Token> tokens = tokenize("int result = a + b * c - d / e;");
        assertEquals(13, countTokens(tokens));
        assertToken(tokens, 0, TokenType.Int, "int");
        assertToken(tokens, 1, TokenType.Identifier, "result");
        assertToken(tokens, 2, TokenType.Assignment, "=");
        assertToken(tokens, 3, TokenType.Identifier, "a");
        assertToken(tokens, 4, TokenType.Plus, "+");
        assertToken(tokens, 5, TokenType.Identifier, "b");
        assertToken(tokens, 6, TokenType.Star, "*");
        assertToken(tokens, 7, TokenType.Identifier, "c");
        assertToken(tokens, 8, TokenType.Minus, "-");
        assertToken(tokens, 9, TokenType.Identifier, "d");
        assertToken(tokens, 10, TokenType.Slash, "/");
        assertToken(tokens, 11, TokenType.Identifier, "e");
        assertToken(tokens, 12, TokenType.SemiColon, ";");
    }

    // ==================== 边界测试 ====================

    @Test
    public void testTokenizeEmptyString() {
        List<Token> tokens = tokenize("");
        assertEquals(0, countTokens(tokens));
    }

    @Test
    public void testTokenizeOnlyWhitespace() {
        List<Token> tokens = tokenize("   \t\n  ");
        assertEquals(0, countTokens(tokens));
    }

    @Test
    public void testTokenizeUnknownCharactersSkipped() {
        // 未知字符被跳过
        List<Token> tokens = tokenize("@#$");
        assertEquals(0, countTokens(tokens));
    }

    @Test
    public void testTokenizeSingleCharOperators() {
        // 验证每个单字符运算符都能被识别
        assertEquals(1, countTokens(tokenize("+")));
        assertEquals(1, countTokens(tokenize("-")));
        assertEquals(1, countTokens(tokenize("*")));
        assertEquals(1, countTokens(tokenize("/")));
    }

    // ==================== dump 方法测试 ====================

    @Test
    public void testDumpDoesNotThrow() {
        SimpleLexer lexer = new SimpleLexer();
        SimpleTokenReader reader = lexer.tokenize("int age = 45;");
        // dump 方法只打印，不抛异常
        SimpleLexer.dump(reader);
    }
}
