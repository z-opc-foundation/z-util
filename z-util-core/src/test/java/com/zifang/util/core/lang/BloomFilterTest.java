package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BloomFilterTest {

    @Test
    public void testContainsAddedItems() {
        BloomFilter bf = new BloomFilter(10_000, 0.01);
        for (int i = 0; i < 1000; i++) {
            bf.add("user_" + i);
        }
        for (int i = 0; i < 1000; i++) {
            assertTrue("should contain user_" + i, bf.mightContain("user_" + i));
        }
    }

    @Test
    public void testFalseNegativesAreZero() {
        BloomFilter bf = new BloomFilter(1000, 0.001);
        for (int i = 0; i < 500; i++) {
            bf.add("item-" + i);
        }
        // 已加入元素不应出现 false negative
        for (int i = 0; i < 500; i++) {
            assertTrue(bf.mightContain("item-" + i));
        }
    }

    @Test
    public void testFalsePositiveRate() {
        BloomFilter bf = new BloomFilter(1000, 0.01);
        Set<String> added = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String s = "a-" + i;
            bf.add(s);
            added.add(s);
        }
        int fp = 0;
        int total = 1000;
        for (int i = 0; i < total; i++) {
            String s = "b-" + i;
            if (!added.contains(s) && bf.mightContain(s)) {
                fp++;
            }
        }
        // 允许 5% 误判（宽松断言，因为理论值是 1%）
        assertTrue("False positive rate too high: " + (fp * 100.0 / total) + "%",
                fp * 100.0 / total < 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgs() {
        new BloomFilter(0, 0.01);
    }

    @Test
    public void testNullSafe() {
        BloomFilter bf = new BloomFilter(100, 0.01);
        assertFalse(bf.mightContain(null));
    }
}