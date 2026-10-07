package com.zifang.util.pandas.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * NumberGennerator 素数筛测试
 */
public class NumberGenneratorTest {

    @Test
    public void testPrimesBelow20() {
        boolean[] isPrime = NumberGennerator.primeNumber(20);

        // 20 以内的素数：2 3 5 7 11 13 17 19
        int[] expected = {2, 3, 5, 7, 11, 13, 17, 19};
        for (int i = 0; i < isPrime.length; i++) {
            boolean shouldBePrime = false;
            for (int p : expected) {
                if (p == i) {
                    shouldBePrime = true;
                    break;
                }
            }
            assertEquals("i=" + i, shouldBePrime, isPrime[i]);
        }
    }

    @Test
    public void testZeroAndOneAreNotPrime() {
        boolean[] isPrime = NumberGennerator.primeNumber(5);
        assertFalse(isPrime[0]);
        assertFalse(isPrime[1]);
        assertTrue(isPrime[2]);
        assertTrue(isPrime[3]);
    }

    @Test
    public void testArrayLengthMatchesLimit() {
        assertEquals(0, NumberGennerator.primeNumber(0).length);
        assertEquals(1, NumberGennerator.primeNumber(1).length);
        assertEquals(50, NumberGennerator.primeNumber(50).length);
    }

    @Test
    public void testLargerRangeCount() {
        // 1000 以内素数共 168 个
        boolean[] isPrime = NumberGennerator.primeNumber(1000);
        int count = 0;
        for (boolean b : isPrime) {
            if (b) {
                count++;
            }
        }
        assertEquals(168, count);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeLimitRejected() {
        NumberGennerator.primeNumber(-1);
    }
}