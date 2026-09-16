package com.zifang.util.source;

import com.zifang.util.source.parser.ByteCodeParserImpl;
import org.junit.Test;

/**
 * ByteCodeParserImpl 测试
 */

/**
 * ByteCodeParserImplTest类。
 */
public class ByteCodeParserImplTest {

    @Test(expected = IllegalArgumentException.class)
    /**
     * testParseNullBytecode方法。
     */
    public void testParseNullBytecode() {
        ByteCodeParserImpl parser = new ByteCodeParserImpl();
        parser.parse((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testParseEmptyBytecode方法。
     */
    public void testParseEmptyBytecode() {
        ByteCodeParserImpl parser = new ByteCodeParserImpl();
        parser.parse(new byte[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testParseNullClass方法。
     */
    public void testParseNullClass() {
        ByteCodeParserImpl parser = new ByteCodeParserImpl();
        parser.parse((Class<?>) null);
    }

    @Test(expected = UnsupportedOperationException.class)
    /**
     * testParseBytecodeThrowsUnsupported方法。
     */
    public void testParseBytecodeThrowsUnsupported() {
        ByteCodeParserImpl parser = new ByteCodeParserImpl();
        parser.parse(new byte[]{0x00});
    }

    @Test(expected = UnsupportedOperationException.class)
    /**
     * testParseClassThrowsUnsupported方法。
     */
    public void testParseClassThrowsUnsupported() {
        ByteCodeParserImpl parser = new ByteCodeParserImpl();
        parser.parse(String.class);
    }
}