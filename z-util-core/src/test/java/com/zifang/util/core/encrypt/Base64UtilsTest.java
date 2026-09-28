package com.zifang.util.core.encrypt;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Base64Utils工具类的单元测试。
 */
public class Base64UtilsTest {

    @Test
    public void testEncodeDecodeRoundTrip() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        String encoded = Base64Utils.encode(data);
        assertArrayEquals(data, Base64Utils.decode(encoded));
    }

    @Test
    public void testMatchesJdkEncoder() {
        Random random = new Random(42L);
        for (int size = 0; size < 40; size++) {
            byte[] data = new byte[size];
            random.nextBytes(data);
            assertEquals(java.util.Base64.getEncoder().encodeToString(data), Base64Utils.encode(data));
        }
    }

    @Test
    public void testNullAndEmpty() {
        assertNull(Base64Utils.encode(null));
        assertNull(Base64Utils.encodeUrlSafe(null));
        assertNull(Base64Utils.encodeString(null));
        assertEquals(0, Base64Utils.decode(null).length);
        assertEquals(0, Base64Utils.decode("").length);
        assertEquals(0, Base64Utils.decodeUrlSafe("").length);
        assertEquals(0, Base64Utils.decodeLenient(null).length);
        assertEquals("", Base64Utils.decodeToString(""));
    }

    @Test
    public void testUrlSafeHasNoStdMarkers() {
        // 0xFB 0xFF 会产生标准字母表里的 +/ 字符
        byte[] data = new byte[]{(byte) 0xFB, (byte) 0xFF, (byte) 0xBF};
        String std = Base64Utils.encode(data);
        String url = Base64Utils.encodeUrlSafe(data);
        assertTrue(std.contains("+") || std.contains("/"));
        assertFalse(url.contains("+"));
        assertFalse(url.contains("/"));
        assertFalse(url.contains("="));
        assertArrayEquals(data, Base64Utils.decodeUrlSafe(url));
    }

    @Test
    public void testDecodeUrlSafeAcceptsUnpadded() {
        byte[] data = "z-util 中文".getBytes(StandardCharsets.UTF_8);
        String unpadded = Base64Utils.encodeUrlSafe(data);
        String padded = java.util.Base64.getUrlEncoder().encodeToString(data);
        assertArrayEquals(data, Base64Utils.decodeUrlSafe(unpadded));
        assertArrayEquals(data, Base64Utils.decodeUrlSafe(padded));
    }

    @Test
    public void testDecodeLenientSkipsWhitespace() {
        byte[] data = new byte[48];
        new Random(7L).nextBytes(data);
        String encoded = Base64Utils.encode(data);
        StringBuilder wrapped = new StringBuilder();
        for (int i = 0; i < encoded.length(); i += 8) {
            wrapped.append(encoded, i, Math.min(i + 8, encoded.length())).append("\n");
        }
        assertArrayEquals(data, Base64Utils.decodeLenient(wrapped.toString()));
        assertArrayEquals(data, Base64Utils.decodeLenient("  " + encoded + "\t"));
        assertArrayEquals(data, Base64Utils.decodeLenient(Base64Utils.encodeUrlSafe(data)));
    }

    @Test
    public void testDecodeLenientRequiresCallerToStripPemArmor() {
        byte[] data = "z-util".getBytes(StandardCharsets.UTF_8);
        String encoded = Base64Utils.encode(data);
        String pem = "-----BEGIN KEY-----\n" + encoded + "\n-----END KEY-----";
        assertArrayEquals(data, Base64Utils.decodeLenient(encoded + "\n"));
        // armor 行本身由合法字母表字符组成，未剥除时不报错，只会静默解出错误字节
        assertFalse(Arrays.equals(data, Base64Utils.decodeLenient(pem)));
    }

    @Test
    public void testStrictDecodeRejectsWhitespace() {
        String encoded = Base64Utils.encode("abc".getBytes(StandardCharsets.UTF_8));
        try {
            Base64Utils.decode(encoded + "\n");
            fail("expected IllegalArgumentException for embedded newline");
        } catch (IllegalArgumentException ignored) {
            // 预期：严格解码不容错
        }
    }

    @Test
    public void testStringHelpersUseUtf8() {
        String text = "一致性哈希 consistent-hash ✓";
        String encoded = Base64Utils.encodeString(text);
        assertEquals(text, Base64Utils.decodeToString(encoded));
        assertEquals(Base64Utils.encode(text.getBytes(StandardCharsets.UTF_8)), encoded);
    }

    @Test
    public void testLenientHandlesUrlSafeInput() {
        byte[] data = new byte[]{(byte) 0xFB, (byte) 0xFF, (byte) 0xBF, 0x00, 0x01};
        assertArrayEquals(data, Base64Utils.decodeLenient(Base64Utils.encodeUrlSafe(data)));
    }

    @Test
    public void testPaddingIsAddedWhenMissing() {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        String unpadded = java.util.Base64.getEncoder().withoutPadding().encodeToString(data);
        assertEquals("aGVsbG8=", Base64Utils.encode(data));
        assertArrayEquals(data, Base64Utils.decodeLenient(unpadded));
        assertTrue(Base64Utils.decode(Base64Utils.encode(data)).length == data.length);
    }
}
