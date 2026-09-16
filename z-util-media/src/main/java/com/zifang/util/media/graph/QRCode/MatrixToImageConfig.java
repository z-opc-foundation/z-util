package com.zifang.util.media.graph.qrcode;

import java.awt.image.BufferedImage;

/**
 * 二维码图片输出配置类。
 * 用于配置 BitMatrix 转换为图片时的颜色选项。
 */
public final class MatrixToImageConfig {

    /**
     * 黑色，像素点上使用的颜色
     */
    public static final int BLACK = 0xFF000000;
    /**
     * 白色，像素点外使用的颜色
     */
    public static final int WHITE = 0xFFFFFFFF;

    private final int onColor;
    private final int offColor;

    /**
     * 创建默认配置，使用黑色作为点上颜色，白色作为点外颜色。
     */
    public MatrixToImageConfig() {
        this(BLACK, WHITE);
    }

    /**
     * 创建自定义颜色的配置。
     *
     * @param onColor  像素点上颜色，ARGB 格式的 int 值
     * @param offColor 像素点外颜色，ARGB 格式的 int 值
     */
    public MatrixToImageConfig(int onColor, int offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
    }

    /**
     * 获取像素点上颜色。
     *
     * @return ARGB 格式的颜色值
     */
    public int getPixelOnColor() {
        return onColor;
    }

    /**
     * 获取像素点外颜色。
     *
     * @return ARGB 格式的颜色值
     */
    public int getPixelOffColor() {
        return offColor;
    }

    int getBufferedImageColorModel() {
        // Use faster BINARY if colors match default
//    return onColor == BLACK && offColor == WHITE ? BufferedImage.TYPE_BYTE_BINARY : BufferedImage.TYPE_INT_RGB;
        return BufferedImage.TYPE_INT_ARGB;
    }

}