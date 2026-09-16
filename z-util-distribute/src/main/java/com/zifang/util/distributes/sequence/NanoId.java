package com.zifang.util.distributes.sequence;

import java.security.SecureRandom;

/**
 * NanoId 生成器（自研，对标 ai.nanoid.NanoId）。
 * <p>
 * 原理：生成 URL-safe（无歧义字符）短 ID，默认 21 字符 ≈ 126 bit 熵。
 * <p>
 * 字符集：{@code A-Za-z0-9_-}（64 字符，去掉 iIlL1oO0 这些易混字符，可选）。
 *
 * <h3>与 UUID 相比</h3>
 * UUID 36 字符（含 4 个 -），NanoId 默认 21 字符 — 短得多但仍有 126 bit 熵。
 * 适合 URL 短链、订单号等场景。
 */
public class NanoId {

    /**
     * 默认字符集：URL-safe 64 字符。
     */
    public static final String DEFAULT_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

    /**
     * 易混淆字符过滤（默认开启）。
     */
    public static final String CONFUSING = "iIlL1oO0";

    private final int size;
    private final char[] alphabet;
    private final SecureRandom random = new SecureRandom();

    public NanoId() {
        this(21);
    }

    public NanoId(int size) {
        this(size, DEFAULT_ALPHABET);
    }

    public NanoId(int size, String alphabet) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (alphabet == null || alphabet.length() < 2) throw new IllegalArgumentException("alphabet too small");
        this.size = size;
        // 去掉易混淆字符（如果有的话）
        StringBuilder filtered = new StringBuilder();
        for (int i = 0; i < alphabet.length(); i++) {
            char c = alphabet.charAt(i);
            if (CONFUSING.indexOf(c) < 0) filtered.append(c);
        }
        this.alphabet = filtered.toString().toCharArray();
    }

    public String next() {
        char[] buf = new char[size];
        for (int i = 0; i < size; i++) {
            buf[i] = alphabet[random.nextInt(alphabet.length)];
        }
        return new String(buf);
    }

    public int size() {
        return size;
    }
}
