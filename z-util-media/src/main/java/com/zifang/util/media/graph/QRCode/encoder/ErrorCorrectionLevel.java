package com.zifang.util.media.graph.qrcode.encoder;

/**
 * Error correction level for QR codes.
 * L: 7% recovery capacity
 * M: 15% recovery capacity
 * Q: 25% recovery capacity
 * H: 30% recovery capacity
 */
public enum ErrorCorrectionLevel {
    L(1, 7),
    M(0, 15),
    Q(3, 25),
    H(2, 30);

    private final int bits;
    private final int recoveryPercent;

    ErrorCorrectionLevel(int bits, int recoveryPercent) {
        this.bits = bits;
        this.recoveryPercent = recoveryPercent;
    }

    /**
     * fromBits方法。
     * * @param bits int类型参数
     *
     * @return static ErrorCorrectionLevel类型返回值
     */
    public static ErrorCorrectionLevel fromBits(int bits) {
        for (ErrorCorrectionLevel level : values()) {
            if (level.bits == bits) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid error correction level bits: " + bits);
    }

    /**
     * getBits方法。
     *
     * @return int类型返回值
     */
    public int getBits() {
        return bits;
    }

    /**
     * getRecoveryPercent方法。
     *
     * @return int类型返回值
     */
    public int getRecoveryPercent() {
        return recoveryPercent;
    }
}
