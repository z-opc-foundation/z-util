package com.zifang.util.core.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * HashUtilTest类。
 */
public class HashUtilTest {

    // --- additiveHash ---

    @Test
    /**
     * testAdditiveHash_WithValidString方法。
     */
    public void testAdditiveHash_WithValidString() {
        int result = HashUtil.additiveHash("test", 31);
        assertTrue(result >= 0);
    }

    @Test
    /**
     * testAdditiveHash_WithEmptyString方法。
     */
    public void testAdditiveHash_WithEmptyString() {
        int result = HashUtil.additiveHash("", 31);
        assertEquals(0 % 31, result);
    }

    @Test
    /**
     * testAdditiveHash_DiffersWithDifferentPrime方法。
     */
    public void testAdditiveHash_DiffersWithDifferentPrime() {
        int result1 = HashUtil.additiveHash("test", 31);
        int result2 = HashUtil.additiveHash("test", 37);
        assertTrue(result1 != result2);
    }

    // --- rotatingHash ---

    @Test
    /**
     * testRotatingHash_WithValidString方法。
     */
    public void testRotatingHash_WithValidString() {
        int result = HashUtil.rotatingHash("test", 31);
        assertTrue(result >= 0);
    }

    @Test
    /**
     * testRotatingHash_WithEmptyString方法。
     */
    public void testRotatingHash_WithEmptyString() {
        int result = HashUtil.rotatingHash("", 31);
        assertEquals(0 % 31, result);
    }

    // --- oneByOneHash ---

    @Test
    /**
     * testOneByOneHash_WithValidString方法。
     */
    public void testOneByOneHash_WithValidString() {
        int result = HashUtil.oneByOneHash("test");
        assertTrue(result != 0);
    }

    @Test
    /**
     * testOneByOneHash_WithEmptyString方法。
     */
    public void testOneByOneHash_WithEmptyString() {
        int result = HashUtil.oneByOneHash("");
        assertEquals(0, result);
    }

    @Test
    /**
     * testOneByOneHash_Deterministic方法。
     */
    public void testOneByOneHash_Deterministic() {
        int result1 = HashUtil.oneByOneHash("test");
        int result2 = HashUtil.oneByOneHash("test");
        assertEquals(result1, result2);
    }

    // --- bernstein ---

    @Test
    /**
     * testBernstein_WithValidString方法。
     */
    public void testBernstein_WithValidString() {
        int result = HashUtil.bernstein("test");
        assertTrue(result != 0);
    }

    @Test
    /**
     * testBernstein_WithEmptyString方法。
     */
    public void testBernstein_WithEmptyString() {
        int result = HashUtil.bernstein("");
        assertEquals(0, result);
    }

    // --- universal ---

    @Test
    /**
     * testUniversal_WithValidInput方法。
     */
    public void testUniversal_WithValidInput() {
        char[] key = {'t', 'e', 's', 't'};
        int mask = 0x7FFFFFFF;
        int[] tab = new int[32];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = i;
        }
        int result = HashUtil.universal(key, mask, tab);
        assertTrue(result >= 0);
    }

    // --- zobrist ---

    @Test
    /**
     * testZobrist_WithValidInput方法。
     */
    public void testZobrist_WithValidInput() {
        char[] key = {'t', 'e', 's', 't'};
        int mask = 0x7FFFFFFF;
        int[][] tab = new int[4][256];
        for (int i = 0; i < tab.length; i++) {
            for (int j = 0; j < tab[i].length; j++) {
                tab[i][j] = j;
            }
        }
        int result = HashUtil.zobrist(key, mask, tab);
        assertTrue(result >= 0);
    }

    // --- FNVHash ---

    @Test
    /**
     * testFNVHash_WithValidByteArray方法。
     */
    public void testFNVHash_WithValidByteArray() {
        byte[] data = {'t', 'e', 's', 't'};
        int result = HashUtil.FNVHash(data);
        assertTrue(result != 0);
    }

    @Test
    /**
     * testFNVHash_Deterministic方法。
     */
    public void testFNVHash_Deterministic() {
        byte[] data = {'t', 'e', 's', 't'};
        int result1 = HashUtil.FNVHash(data);
        int result2 = HashUtil.FNVHash(data);
        assertEquals(result1, result2);
    }

    // --- FNVHash1 with byte array ---

    @Test
    /**
     * testFNVHash1_ByteArray_WithValidInput方法。
     */
    public void testFNVHash1_ByteArray_WithValidInput() {
        byte[] data = {'t', 'e', 's', 't'};
        int result = HashUtil.FNVHash1(data);
        assertTrue(result != 0);
    }

    // --- FNVHash1 with String ---

    @Test
    /**
     * testFNVHash1_String_WithValidInput方法。
     */
    public void testFNVHash1_String_WithValidInput() {
        int result = HashUtil.FNVHash1("test");
        assertTrue(result != 0);
    }

    // --- intHash ---

    @Test
    /**
     * testIntHash_WithValidInt方法。
     */
    public void testIntHash_WithValidInt() {
        int result = HashUtil.intHash(12345);
        assertTrue(result != 12345);
    }

    @Test
    /**
     * testIntHash_Deterministic方法。
     */
    public void testIntHash_Deterministic() {
        int result1 = HashUtil.intHash(12345);
        int result2 = HashUtil.intHash(12345);
        assertEquals(result1, result2);
    }

    // --- RSHash ---

    @Test
    /**
     * testRSHash_WithValidString方法。
     */
    public void testRSHash_WithValidString() {
        int result = HashUtil.RSHash("test");
        assertTrue(result >= 0);
    }

    @Test
    /**
     * testRSHash_Deterministic方法。
     */
    public void testRSHash_Deterministic() {
        int result1 = HashUtil.RSHash("test");
        int result2 = HashUtil.RSHash("test");
        assertEquals(result1, result2);
    }

    // --- JSHash ---

    @Test
    /**
     * testJSHash_WithValidString方法。
     */
    public void testJSHash_WithValidString() {
        int result = HashUtil.JSHash("test");
        assertTrue(result >= 0);
    }

    @Test
    /**
     * testJSHash_Deterministic方法。
     */
    public void testJSHash_Deterministic() {
        int result1 = HashUtil.JSHash("test");
        int result2 = HashUtil.JSHash("test");
        assertEquals(result1, result2);
    }

    // --- PJWHash ---

    @Test
    /**
     * testPJWHash_WithValidString方法。
     */
    public void testPJWHash_WithValidString() {
        int result = HashUtil.PJWHash("test");
        assertTrue(result >= 0);
    }

    // --- ELFHash ---

    @Test
    /**
     * testELFHash_WithValidString方法。
     */
    public void testELFHash_WithValidString() {
        int result = HashUtil.ELFHash("test");
        assertTrue(result >= 0);
    }

    // --- BKDRHash ---

    @Test
    /**
     * testBKDRHash_WithValidString方法。
     */
    public void testBKDRHash_WithValidString() {
        int result = HashUtil.BKDRHash("test");
        assertTrue(result >= 0);
    }

    @Test
    /**
     * testBKDRHash_Deterministic方法。
     */
    public void testBKDRHash_Deterministic() {
        int result1 = HashUtil.BKDRHash("test");
        int result2 = HashUtil.BKDRHash("test");
        assertEquals(result1, result2);
    }

    // --- SDBMHash ---

    @Test
    /**
     * testSDBMHash_WithValidString方法。
     */
    public void testSDBMHash_WithValidString() {
        int result = HashUtil.SDBMHash("test");
        assertTrue(result >= 0);
    }

    // --- DJBHash ---

    @Test
    /**
     * testDJBHash_WithValidString方法。
     */
    public void testDJBHash_WithValidString() {
        int result = HashUtil.DJBHash("test");
        assertTrue(result >= 0);
    }

    // --- DEKHash ---

    @Test
    /**
     * testDEKHash_WithValidString方法。
     */
    public void testDEKHash_WithValidString() {
        int result = HashUtil.DEKHash("test");
        assertTrue(result != 0);
    }

    // --- APHash ---

    @Test
    /**
     * testAPHash_WithValidString方法。
     */
    public void testAPHash_WithValidString() {
        int result = HashUtil.APHash("test");
        assertTrue(result != 0);
    }

    // --- java ---

    @Test
    /**
     * testJavaHash_WithValidString方法。
     */
    public void testJavaHash_WithValidString() {
        int result = HashUtil.java("test");
        assertTrue(result != 0);
    }

    @Test
    /**
     * testJavaHash_EqualsStringHashCode方法。
     */
    public void testJavaHash_EqualsStringHashCode() {
        int result = HashUtil.java("test");
        assertEquals("test".hashCode(), result);
    }

    // --- mixHash ---

    @Test
    /**
     * testMixHash_WithValidString方法。
     */
    public void testMixHash_WithValidString() {
        long result = HashUtil.mixHash("test");
        assertTrue(result != 0);
    }

    @Test
    /**
     * testMixHash_Deterministic方法。
     */
    public void testMixHash_Deterministic() {
        long result1 = HashUtil.mixHash("test");
        long result2 = HashUtil.mixHash("test");
        assertEquals(result1, result2);
    }

    // --- hash ---

    @Test
    /**
     * testHash_WithNonNullObject方法。
     */
    public void testHash_WithNonNullObject() {
        int result = HashUtil.hash("test");
        assertTrue(result != 0);
    }

    @Test
    /**
     * testHash_WithNullObject方法。
     */
    public void testHash_WithNullObject() {
        int result = HashUtil.hash(null);
        assertEquals(0, result);
    }

    @Test
    /**
     * testHash_Deterministic方法。
     */
    public void testHash_Deterministic() {
        int result1 = HashUtil.hash("test");
        int result2 = HashUtil.hash("test");
        assertEquals(result1, result2);
    }
}
