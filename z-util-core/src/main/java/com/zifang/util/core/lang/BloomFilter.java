package com.zifang.util.core.lang;

import java.util.BitSet;

/**
 * 简易布隆过滤器（BloomFilter）。
 * <p>
 * 用于以极小内存快速判断一个元素"绝对不存在"或"可能存在"。
 * 适用场景：缓存穿透防护、爬虫 URL 去重、黑名单等。
 * <p>
 * <b>注意</b>：布隆过滤器存在误判率（false positive），但不会出现 false negative。
 * 误判率与位数组大小 m、哈希函数个数 k、元素数量 n 满足：
 * <pre>p ≈ (1 - e^(-kn/m))^k</pre>
 *
 * @author zifang
 */
public class BloomFilter {

    private final BitSet bits;
    private final int bitSize;
    private final int hashCount;

    /**
     * @param expectedInsertions 期望插入元素数量
     * @param falsePositiveRate  期望误判率（0.0 ~ 1.0），如 0.01 表示 1%
     */
    public BloomFilter(int expectedInsertions, double falsePositiveRate) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException("expectedInsertions must be > 0");
        }
        if (falsePositiveRate <= 0.0 || falsePositiveRate >= 1.0) {
            throw new IllegalArgumentException("falsePositiveRate must be in (0,1)");
        }
        // m = -n ln p / (ln 2)^2
        this.bitSize = (int) Math.ceil(-expectedInsertions * Math.log(falsePositiveRate) / (Math.log(2) * Math.log(2)));
        // k = (m/n) ln 2
        this.hashCount = Math.max(1, (int) Math.round((bitSize / (double) expectedInsertions) * Math.log(2)));
        this.bits = new BitSet(bitSize);
    }

    private static long fnv1a64(String s) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x100000001b3L;
        }
        return h;
    }

    private static long murmur64(String s) {
        long h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31L * h + s.charAt(i);
        }
        return h;
    }

    /**
     * 添加元素。
     */
    public void add(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("BloomFilter does not support null");
        }
        int[] hashes = hash(o);
        for (int i = 0; i < hashCount; i++) {
            bits.set(hashes[i]);
        }
    }

    /**
     * 元素是否"可能存在"。
     */
    public boolean mightContain(Object o) {
        if (o == null) {
            return false;
        }
        int[] hashes = hash(o);
        for (int i = 0; i < hashCount; i++) {
            if (!bits.get(hashes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用双重哈希模拟多个哈希函数：
     * h_i(x) = h1(x) + i * h2(x)
     */
    private int[] hash(Object o) {
        long[] hv = hashInternal(o);
        int[] result = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            result[i] = (int) ((hv[0] + (long) i * hv[1]) % bitSize);
            if (result[i] < 0) {
                result[i] += bitSize;
            }
        }
        return result;
    }

    private long[] hashInternal(Object o) {
        long h1;
        long h2;
        if (o instanceof String) {
            String s = (String) o;
            h1 = murmur64(s);
            h2 = fnv1a64(s);
        } else {
            int h = o.hashCode();
            h1 = h;
            h2 = (long) h * 0x9E3779B97F4A7C15L;
        }
        return new long[]{h1, h2};
    }

    public int bitSize() {
        return bitSize;
    }

    public int hashCount() {
        return hashCount;
    }
}