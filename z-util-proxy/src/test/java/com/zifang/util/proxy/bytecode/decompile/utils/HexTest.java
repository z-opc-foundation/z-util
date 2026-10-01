package com.zifang.util.proxy.bytecode.decompile.utils;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Hex 工具测试：hex/str/bytes 互转与回环。
 */
public class HexTest {

    @Test
    public void str2HexStrUsesSpacesAndUppercase() {
        assertEquals("61 62 63", Hex.str2HexStr("abc"));
        assertEquals("41", Hex.str2HexStr("A"));
        assertEquals("", Hex.str2HexStr(""));
    }

    @Test
    public void hexStr2StrDecodes() {
        assertEquals("abc", Hex.hexStr2Str("616263"));
        assertEquals("N-", Hex.hexStr2Str("4E2D"));
    }

    @Test
    public void hexStrRoundTrip() {
        String hex = Hex.str2HexStr("round-trip 123");
        assertEquals("round-trip 123", Hex.hexStr2Str(hex.replace(" ", "")));
    }

    @Test
    public void byte2HexStrNoSeparators() {
        assertEquals("0AFF", Hex.byte2HexStr(new byte[]{0x0A, (byte) 0xFF}));
        assertEquals("00", Hex.byte2HexStr(new byte[]{0x00}));
    }

    @Test
    public void hexStr2BytesDecodes() {
        assertArrayEquals(new byte[]{0x0A, (byte) 0xFF}, Hex.hexStr2Bytes("0AFF"));
    }

    @Test
    public void bytesHexRoundTrip() {
        byte[] origin = {0x00, 0x12, 0x7F, (byte) 0x80, (byte) 0xFF};
        assertEquals("00127F80FF", Hex.byte2HexStr(origin));
        assertArrayEquals(origin, Hex.hexStr2Bytes("00127F80FF"));
    }

    @Test
    public void hex2IntegerParsesHex() {
        assertEquals(6699, Hex.hex2Integer("1A2B"));
        assertEquals(255, Hex.hex2Integer("00FF"));
        assertEquals(0, Hex.hex2Integer("0000"));
    }

    @Test
    public void strToUnicodeEmitsBackslashUEscapes() throws Exception {
        assertEquals("\\u0061", Hex.strToUnicode("a"));
        assertEquals("\\u4E2D", Hex.strToUnicode("\u4E2D"));
    }

    @Test
    public void unicodeToStringDecodesEscapedGroups() {
        assertEquals("\u4E2D\u6587", Hex.unicodeToString("\\u4E2D\\u6587"));
        assertEquals("AB", Hex.unicodeToString("00410042"));
    }

    @Test
    public void unicodeRoundTrip() throws Exception {
        String origin = "aA\u4E2D\u65871";
        assertEquals(origin, Hex.unicodeToString(Hex.strToUnicode(origin)));
    }
}
