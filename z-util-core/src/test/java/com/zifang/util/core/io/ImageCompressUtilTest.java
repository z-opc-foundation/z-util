package com.zifang.util.core.io;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * ImageCompressUtilTest类。
 */
public class ImageCompressUtilTest {

    /**
     * 生成指定宽高的随机噪声 PNG 字节
     */
    private byte[] createNoisePngBytes(int width, int height, long seed) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(seed);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, random.nextInt(1 << 24));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", out)) {
            throw new IllegalStateException("no png writer available");
        }
        return out.toByteArray();
    }

    @Test
    /**
     * testCompressJpegRoundTrip方法。
     */
    public void testCompressJpegRoundTrip() throws Exception {
        byte[] pngBytes = createNoisePngBytes(120, 120, 42L);
        byte[] jpegBytes = ImageCompressUtil.compress(pngBytes, "image/jpeg", 0.2f);

        assertNotNull(jpegBytes);
        assertTrue(jpegBytes.length > 0);

        BufferedImage restored = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        assertNotNull(restored);
        assertEquals(120, restored.getWidth());
        assertEquals(120, restored.getHeight());
    }

    @Test
    /**
     * testCompressJpegSmallerThanLosslessPng方法。
     */
    public void testCompressJpegSmallerThanLosslessPng() throws Exception {
        byte[] pngBytes = createNoisePngBytes(160, 160, 7L);
        byte[] jpegBytes = ImageCompressUtil.compress(pngBytes, "image/jpeg", 0.15f);

        // 随机噪声下低质量 JPEG 必然小于无损 PNG
        assertTrue(jpegBytes.length < pngBytes.length);
    }

    @Test
    /**
     * testCompressPngKeepsReadable方法。
     */
    public void testCompressPngKeepsReadable() throws Exception {
        byte[] pngBytes = createNoisePngBytes(60, 60, 99L);
        byte[] result = ImageCompressUtil.compress(pngBytes, "image/png", 0.3f);

        BufferedImage restored = ImageIO.read(new ByteArrayInputStream(result));
        assertNotNull(restored);
        assertEquals(60, restored.getWidth());
        assertEquals(60, restored.getHeight());
    }

    @Test
    /**
     * testCompressNullAndEmpty方法。
     */
    public void testCompressNullAndEmpty() {
        assertNull(ImageCompressUtil.compress(null, "image/png", 0.5f));

        byte[] empty = new byte[0];
        assertSame(empty, ImageCompressUtil.compress(empty, "image/png", 0.5f));
    }

    @Test
    /**
     * testCompressNonImageContentType方法。
     */
    public void testCompressNonImageContentType() {
        byte[] bytes = "not an image".getBytes(StandardCharsets.UTF_8);
        assertSame(bytes, ImageCompressUtil.compress(bytes, "text/plain", 0.5f));
        assertSame(bytes, ImageCompressUtil.compress(bytes, null, 0.5f));
    }

    @Test
    /**
     * testCompressUnparsableBytes方法。
     */
    public void testCompressUnparsableBytes() {
        byte[] bytes = "definitely not image bytes".getBytes(StandardCharsets.UTF_8);
        assertSame(bytes, ImageCompressUtil.compress(bytes, "image/png", 0.5f));
    }

    @Test
    /**
     * testCompressQualityClamp方法。
     */
    public void testCompressQualityClamp() throws Exception {
        byte[] pngBytes = createNoisePngBytes(50, 50, 123L);

        byte[] tooLow = ImageCompressUtil.compress(pngBytes, "image/jpeg", 0.0f);
        byte[] tooHigh = ImageCompressUtil.compress(pngBytes, "image/jpeg", 5.0f);

        assertNotNull(ImageIO.read(new ByteArrayInputStream(tooLow)));
        assertNotNull(ImageIO.read(new ByteArrayInputStream(tooHigh)));
        assertEquals(50, ImageIO.read(new ByteArrayInputStream(tooLow)).getWidth());
        assertEquals(50, ImageIO.read(new ByteArrayInputStream(tooHigh)).getWidth());
    }

    @Test
    /**
     * testCompressTransparentBackgroundFlattened方法。
     */
    public void testCompressTransparentBackgroundFlattened() throws Exception {
        // 构造带透明通道的 ARGB 图片
        BufferedImage argb = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(2026L);
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                argb.setRGB(x, y, random.nextInt());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(argb, "png", out);
        byte[] transparentPng = out.toByteArray();

        byte[] jpegBytes = ImageCompressUtil.compress(transparentPng, "image/jpeg", 0.5f);

        BufferedImage restored = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        assertNotNull(restored);
        // 压平后应为不透明的 RGB 图
        assertFalse(restored.getColorModel().hasAlpha());
        // 全透明像素应落在白底而非黑底
        BufferedImage fullyTransparent = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream transparentOut = new ByteArrayOutputStream();
        ImageIO.write(fullyTransparent, "png", transparentOut);
        byte[] flattened = ImageCompressUtil.compress(transparentOut.toByteArray(), "image/jpeg", 0.9f);
        BufferedImage whiteCheck = ImageIO.read(new ByteArrayInputStream(flattened));
        int rgb = whiteCheck.getRGB(5, 5);
        assertEquals(0xFF, (rgb >> 16) & 0xFF);
        assertEquals(0xFF, (rgb >> 8) & 0xFF);
        assertEquals(0xFF, rgb & 0xFF);
    }
}
