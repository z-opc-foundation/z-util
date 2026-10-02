package com.zifang.util.expression.source.lexer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SimpleTokenReader Token流读取器完整测试
 */
public class SimpleTokenReaderTest {

    private SimpleTokenReader createReader(TokenType... types) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            SimpleToken token = new SimpleToken();
            token.type = types[i];
            token.text = "token" + i;
            tokens.add(token);
        }
        return new SimpleTokenReader(tokens);
    }

    // ==================== read() 测试 ====================

    @Test
    public void testReadSequential() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral, TokenType.Plus, TokenType.IntLiteral);
        assertEquals("token0", reader.read().getText());
        assertEquals("token1", reader.read().getText());
        assertEquals("token2", reader.read().getText());
    }

    @Test
    public void testReadBeyondEnd() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral);
        assertNotNull(reader.read());
        assertNull(reader.read());
        assertNull(reader.read());
    }

    @Test
    public void testReadEmpty() {
        SimpleTokenReader reader = createReader();
        assertNull(reader.read());
    }

    // ==================== peek() 测试 ====================

    @Test
    public void testPeekDoesNotConsume() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral, TokenType.Plus);
        assertEquals("token0", reader.peek().getText());
        assertEquals("token0", reader.peek().getText());
        assertEquals("token0", reader.peek().getText());
    }

    @Test
    public void testPeekBeyondEnd() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral);
        reader.read();
        assertNull(reader.peek());
    }

    @Test
    public void testPeekEmpty() {
        SimpleTokenReader reader = createReader();
        assertNull(reader.peek());
    }

    // ==================== unread() 测试 ====================

    @Test
    public void testUnreadSingle() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral, TokenType.Plus);
        assertEquals("token0", reader.read().getText());
        reader.unread();
        assertEquals("token0", reader.read().getText());
    }

    @Test
    public void testUnreadMultiple() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral, TokenType.Plus, TokenType.IntLiteral);
        assertEquals("token0", reader.read().getText());
        assertEquals("token1", reader.read().getText());
        reader.unread();
        reader.unread();
        assertEquals("token0", reader.read().getText());
    }

    @Test
    public void testUnreadBeyondStart() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral);
        reader.unread(); // 尝试回退到开始之前
        reader.unread();
        reader.unread();
        // position 已经是0，再unread也不应出错
        assertEquals("token0", reader.read().getText());
    }

    @Test
    public void testUnreadAfterEmpty() {
        SimpleTokenReader reader = createReader();
        reader.unread();
        assertNull(reader.read());
    }

    // ==================== 组合操作测试 ====================

    @Test
    public void testPeekReadUnreadRead() {
        SimpleTokenReader reader = createReader(TokenType.IntLiteral, TokenType.Plus, TokenType.IntLiteral);
        assertEquals("token0", reader.peek().getText());  // 查看
        assertEquals("token0", reader.read().getText());  // 读出
        reader.unread();                        // 回退
        assertEquals("token0", reader.read().getText());  // 再次读出
    }

    @Test
    public void testPositionTracking() {
        SimpleTokenReader reader = createReader(
                TokenType.Identifier, TokenType.Assignment,
                TokenType.IntLiteral, TokenType.SemiColon
        );
        assertEquals(0, reader.getPosition());
        reader.read();
        assertEquals(1, reader.getPosition());
        reader.read();
        assertEquals(2, reader.getPosition());
        reader.read();
        assertEquals(3, reader.getPosition());
    }

    @Test
    public void testSetPosition() {
        SimpleTokenReader reader = createReader(
                TokenType.Identifier, TokenType.Assignment,
                TokenType.IntLiteral, TokenType.SemiColon
        );
        reader.read();
        reader.read();
        assertEquals(2, reader.getPosition());
        reader.setPosition(0);
        assertEquals(0, reader.getPosition());
        assertEquals("token0", reader.read().getText());
    }

    @Test
    public void testSetPositionToMiddle() {
        SimpleTokenReader reader = createReader(
                TokenType.Identifier, TokenType.Assignment,
                TokenType.IntLiteral, TokenType.SemiColon
        );
        reader.setPosition(2);
        assertEquals(2, reader.getPosition());
        assertEquals("token2", reader.read().getText());
    }

    @Test
    public void testSetPositionBeyondEnd() {
        SimpleTokenReader reader = createReader(TokenType.Identifier, TokenType.Assignment);
        reader.setPosition(100); // 无效位置，position不变
        assertEquals(0, reader.getPosition());
    }

    @Test
    public void testSetPositionNegative() {
        SimpleTokenReader reader = createReader(TokenType.Identifier, TokenType.Assignment);
        reader.setPosition(-1); // 无效位置，position不变
        assertEquals(0, reader.getPosition());
    }

    @Test
    public void testBacktrackingPattern() {
        // 模拟解析器的回溯场景
        SimpleTokenReader reader = createReader(TokenType.Identifier, TokenType.Assignment, TokenType.IntLiteral);
        assertEquals(TokenType.Identifier, reader.peek().getType());
        reader.read(); // 读走 Identifier
        if (reader.peek().getType() == TokenType.Assignment) {
            reader.read(); // 读走 Assignment
            reader.read(); // 读走 IntLiteral
        } else {
            reader.unread(); // 回退
        }
        assertEquals(3, reader.getPosition());
    }
}
