package com.zifang.util.core.jwt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * HMAC-SHA512 签名算法。
 */
final class HmacSha512 implements SigningAlgorithm {

    static final HmacSha512 INSTANCE = new HmacSha512();
    private static final String ALG = "HmacSHA512";

    @Override
    public String name() {
        return "HS512";
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
        if (expected.length != signature.length) return false;
        int diff = 0;
        for (int i = 0; i < expected.length; i++) diff |= expected[i] ^ signature[i];
        return diff == 0;
    }
}
