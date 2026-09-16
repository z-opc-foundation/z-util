package com.zifang.util.core.jwt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * HMAC-SHA256 签名算法。
 */
final class HmacSha256 implements SigningAlgorithm {

    static final HmacSha256 INSTANCE = new HmacSha256();
    private static final String ALG = "HmacSHA256";

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }

    @Override
    public String name() {
        return "HS256";
    }

    @Override
    public byte[] sign(byte[] data, String secret) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(ALG);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALG));
        return mac.doFinal(data);
    }

    @Override
    public boolean verify(byte[] data, byte[] signature, String secret) throws GeneralSecurityException {
        byte[] expected = sign(data, secret);
        return constantTimeEquals(expected, signature);
    }
}
