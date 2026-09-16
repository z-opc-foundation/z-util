package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;

/**
 * Binary bitmap wrapper for QR code decoding.
 * Represents a binarized image.
 */
public class BinaryBitmap {
    private final BitMatrix matrix;

    /**
     * BinaryBitmap方法。
     * * @param matrix BitMatrix类型参数
     */
    public BinaryBitmap(BitMatrix matrix) {
        this.matrix = matrix;
    }

    /**
     * getBlackMatrix方法。
     *
     * @return BitMatrix类型返回值
     */
    public BitMatrix getBlackMatrix() {
        return matrix;
    }

    /**
     * getWidth方法。
     *
     * @return int类型返回值
     */
    public int getWidth() {
        return matrix.getWidth();
    }

    /**
     * getHeight方法。
     *
     * @return int类型返回值
     */
    public int getHeight() {
        return matrix.getHeight();
    }
}
