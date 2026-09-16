package com.zifang.util.core.jwt;

import java.security.GeneralSecurityException;

/**
 * JWT 签名算法接口（自研，对标 JJWT 的 SignatureAlgorithm）。
 * <p>
 * 实现：
 * <ul>
 *   <li>{@link HmacSha256}：HS256（共享密钥）</li>
 *   <li>{@link HmacSha512}：HS512</li>
 * </ul>
 */
public interface SigningAlgorithm {

    static SigningAlgorithm hs256() {
        return HmacSha256.INSTANCE;
    }

    static SigningAlgorithm hs512() {
        return HmacSha512.INSTANCE;
    }

    static SigningAlgorithm of(String name) {
        if ("HS256".equalsIgnoreCase(name)) return hs256();
        if ("HS512".equalsIgnoreCase(name)) return hs512();
        if ("none".equalsIgnoreCase(name)) return NoneAlgorithm.INSTANCE;
        throw new IllegalArgumentException("unsupported alg: " + name);
    }

    // ===== 工厂 =====

    /**
     * JWT header 中声明的算法名（如 HS256/RS256）。
     */
    String name();

    /**
     * 用密钥对 {@code signingInput}（header.payload）签名，返回签名字节。
     */
    byte[] sign(byte[] signingInput, String secret) throws GeneralSecurityException;

    /**
     * 验签。
     */
    boolean verify(byte[] signingInput, byte[] signature, String secret) throws GeneralSecurityException;
}
