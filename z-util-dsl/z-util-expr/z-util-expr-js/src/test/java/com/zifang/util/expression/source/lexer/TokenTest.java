package com.zifang.util.expression.source.lexer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Token 接口和 SimpleToken 实现完整测试
 */
public class TokenTest {

    @Test
    public void testSimpleTokenConstruction() {
        SimpleToken token = new SimpleToken();
        token.type = TokenType.IntLiteral;
        token.text = "123";

        assertEquals(TokenType.IntLiteral, token.getType());
        assertEquals("123", token.getText());
    }

    @Test
    public void testTokenTypeValues() {
        // 验证所有 TokenType 枚举值
        TokenType[] types = TokenType.values();
        assertTrue(types.length > 15);

        assertEquals(TokenType.Plus, TokenType.valueOf("Plus"));
        assertEquals(TokenType.Minus, TokenType.valueOf("Minus"));
        assertEquals(TokenType.Star, TokenType.valueOf("Star"));
        assertEquals(TokenType.Slash, TokenType.valueOf("Slash"));
        assertEquals(TokenType.GE, TokenType.valueOf("GE"));
        assertEquals(TokenType.GT, TokenType.valueOf("GT"));
        assertEquals(TokenType.EQ, TokenType.valueOf("EQ"));
        assertEquals(TokenType.LE, TokenType.valueOf("LE"));
        assertEquals(TokenType.LT, TokenType.valueOf("LT"));
        assertEquals(TokenType.SemiColon, TokenType.valueOf("SemiColon"));
        assertEquals(TokenType.LeftParen, TokenType.valueOf("LeftParen"));
        assertEquals(TokenType.RightParen, TokenType.valueOf("RightParen"));
        assertEquals(TokenType.Assignment, TokenType.valueOf("Assignment"));
        assertEquals(TokenType.Int, TokenType.valueOf("Int"));
        assertEquals(TokenType.Identifier, TokenType.valueOf("Identifier"));
        assertEquals(TokenType.IntLiteral, TokenType.valueOf("IntLiteral"));
    }

    @Test
    public void testAllTokenTypesCanBeAssignedToToken() {
        for (TokenType type : TokenType.values()) {
            SimpleToken token = new SimpleToken();
            token.type = type;
            token.text = type.name();
            assertEquals(type, token.getType());
        }
    }

    @Test
    public void testSimpleTokenImmutability() {
        SimpleToken token = new SimpleToken();
        token.type = TokenType.IntLiteral;
        token.text = "42";

        // 字段是 public 的，可以修改
        token.text = "100";
        assertEquals("100", token.getText());

        token.type = TokenType.Identifier;
        assertEquals(TokenType.Identifier, token.getType());
    }
}
