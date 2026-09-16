package com.zifang.util.core.jwt;

/**
 * 无签名算法（仅用于测试；生产禁止）。
 */
final class NoneAlgorithm implements SigningAlgorithm {
    static final NoneAlgorithm INSTANCE = new NoneAlgorithm();

    @Override
    public String name() {
        return "none";
    }

    @Override
    public byte[] sign(byte[] data, String secret) {
        return new byte[0];
    }

    @Override
    public boolean verify(byte[] data, byte[] signature, String secret) {
        return signature.length == 0;
    }
}
