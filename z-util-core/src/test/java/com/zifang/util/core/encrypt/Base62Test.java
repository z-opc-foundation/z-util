package com.zifang.util.core.encrypt;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Base62工具类的单元测试。
 */
public class Base62Test {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Test
    public void testRoundTripRandomBytes() {
        Random random = new Random(1234L);
        for (int size = 1; size <= 48; size++) {
            byte[] data = new byte[size];
            random.nextBytes(data);
            assertArrayEquals("size=" + size, data, Base62.decode(Base62.encode(data)));
        }
    }

    @Test
    public void testLeadingZerosPreserved() {
        byte[] data = new byte[]{0, 0, 0, 5, 9};
        String encoded = Base62.encode(data);
        assertTrue(encoded.startsWith("000"));
        assertArrayEquals(data, Base62.decode(encoded));
        assertArrayEquals(new byte[]{0}, Base62.decode("0"));
        assertArrayEquals(new byte[]{0, 0}, Base62.decode("00"));
    }

    @Test
    public void testNumericVectors() {
        // 字母表下标即数值：1 -> "1"，62 -> "10"
        assertEquals("1", Base62.encode(new byte[]{1}));
        assertEquals("10", Base62.encode(new byte[]{62}));
        assertArrayEquals(new byte[]{62}, Base62.decode("10"));
        assertEquals("", Base62.encode(new byte[0]));
        assertEquals("", Base62.encode(null));
        assertEquals(0, Base62.decode("").length);
    }

    @Test
    public void testOutputUsesOnlyAlphabet() {
        byte[] data = new byte[64];
        new Random(99L).nextBytes(data);
        String encoded = Base62.encode(data);
        for (int i = 0; i < encoded.length(); i++) {
            assertTrue("unexpected char " + encoded.charAt(i), ALPHABET.indexOf(encoded.charAt(i)) >= 0);
        }
    }

    @Test
    public void testDecodeRejectsIllegalChar() {
        String[] bad = {"a+b", "z/", "x=1", "é"};
        for (String value : bad) {
            try {
                Base62.decode(value);
                fail("expected IllegalArgumentException for " + value);
            } catch (IllegalArgumentException ignored) {
                // 预期
            }
        }
    }

    @Test
    public void testRandomString() {
        String code = Base62.random(8);
        assertEquals(8, code.length());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            assertTrue(seen.add(Base62.random(8)));
        }
        assertEquals(8, Base62.random(8, new SecureRandom()).length());
        assertEquals(8, Base62.random(8, null).length());
    }

    @Test
    public void testRandomRejectsNonPositiveLength() {
        int[] bad = {0, -1};
        for (int length : bad) {
            try {
                Base62.random(length);
                fail("expected IllegalArgumentException for length " + length);
            } catch (IllegalArgumentException ignored) {
                // 预期
            }
        }
    }
}
