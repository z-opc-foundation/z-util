package com.zifang.util.media.graph.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图片比较工具。
 * 支持：相似度百分比 / 像素差异Map / 是否完全相同
 */
public final class ImageCompare {

    private ImageCompare() {
    }

    /**
     * 比较两张图片的相似度。
     *
     * @param image1 图片1
     * @param image2 图片2
     * @return 相似度百分比（0~100），完全相同返回100
     */
    public static float compareImage(File image1, File image2) throws IOException {
        BufferedImage bi1 = ImageIO.read(image1);
        BufferedImage bi2 = ImageIO.read(image2);
        if (bi1 == null || bi2 == null) {
            throw new IOException("Unable to read one or both images");
        }
        return compareImage(bi1, bi2);
    }

    /**
     * 比较两张 BufferedImage 的相似度。
     *
     * @param bi1 图片1
     * @param bi2 图片2
     * @return 相似度百分比（0~100）
     */
    public static float compareImage(BufferedImage bi1, BufferedImage bi2) {
        int w1 = bi1.getWidth();
        int h1 = bi1.getHeight();
        int w2 = bi2.getWidth();
        int h2 = bi2.getHeight();

        // 尺寸不一致时，取共同区域
        int w = Math.min(w1, w2);
        int h = Math.min(h1, h2);

        int total = w * h;
        int similar = 0;
        int threshold = 5;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p1 = bi1.getRGB(x, y);
                int p2 = bi2.getRGB(x, y);
                int r1 = (p1 >> 16) & 0xff;
                int g1 = (p1 >> 8) & 0xff;
                int b1 = p1 & 0xff;
                int r2 = (p2 >> 16) & 0xff;
                int g2 = (p2 >> 8) & 0xff;
                int b2 = p2 & 0xff;
                if (Math.abs(r1 - r2) < threshold
                        && Math.abs(g1 - g2) < threshold
                        && Math.abs(b1 - b2) < threshold) {
                    similar++;
                }
            }
        }
        return (float) similar / total * 100f;
    }

    /**
     * 判断两张图片是否完全相同（逐像素比较）。
     */
    public static boolean isIdentical(File image1, File image2) throws IOException {
        BufferedImage bi1 = ImageIO.read(image1);
        BufferedImage bi2 = ImageIO.read(image2);
        if (bi1 == null || bi2 == null) {
            throw new IOException("Unable to read one or both images");
        }
        return isIdentical(bi1, bi2);
    }

    /**
     * 判断两张 BufferedImage 是否完全相同。
     */
    public static boolean isIdentical(BufferedImage bi1, BufferedImage bi2) {
        int w1 = bi1.getWidth();
        int h1 = bi1.getHeight();
        int w2 = bi2.getWidth();
        int h2 = bi2.getHeight();
        if (w1 != w2 || h1 != h2) {
            return false;
        }
        for (int y = 0; y < h1; y++) {
            for (int x = 0; x < w1; x++) {
                if (bi1.getRGB(x, y) != bi2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取差异图（将两张图片的不同像素标记为红色）。
     *
     * @param image1 图片1
     * @param image2 图片2
     * @return 差异图
     */
    public static BufferedImage diffImage(File image1, File image2) throws IOException {
        BufferedImage bi1 = ImageIO.read(image1);
        BufferedImage bi2 = ImageIO.read(image2);
        if (bi1 == null || bi2 == null) {
            throw new IOException("Unable to read one or both images");
        }
        return diffImage(bi1, bi2);
    }

    /**
     * 获取差异图。
     *
     * @param bi1 图片1
     * @param bi2 图片2
     * @return 差异 BufferedImage，不同像素显示为红色
     */
    public static BufferedImage diffImage(BufferedImage bi1, BufferedImage bi2) {
        int w = Math.min(bi1.getWidth(), bi2.getWidth());
        int h = Math.min(bi1.getHeight(), bi2.getHeight());
        BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p1 = bi1.getRGB(x, y);
                int p2 = bi2.getRGB(x, y);
                if (p1 == p2) {
                    diff.setRGB(x, y, p1);
                } else {
                    // 红色标记差异
                    diff.setRGB(x, y, 0xffff0000);
                }
            }
        }
        return diff;
    }
}
