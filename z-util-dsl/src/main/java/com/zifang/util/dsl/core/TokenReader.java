package com.zifang.util.dsl.core;

import com.zifang.util.dsl.token.Token;

import java.io.Closeable;

/**
 * Token流读取器
 */
public interface TokenReader extends Closeable {

    /**
     * 读取下一个Token
     */
    Token read();

    /**
     * 预览下一个Token（不推进）
     */
    Token peek();

    /**
     * 推进一个Token
     */
    void advance();

    /**
     * 获取当前Token
     */
    Token get(int offset);

    /**
     * 是否已结束
     */
    boolean hasNext();

    /**
     * 重置到开头
     */
    void reset();
}