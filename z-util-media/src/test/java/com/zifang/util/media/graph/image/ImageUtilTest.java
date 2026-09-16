package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * ImageUtilTest类。
 */
public class ImageUtilTest {

    @Test
    /**
     * testResize方法。
     */
    public void testResize() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage resized = ImageUtil.resize(img, 50, 50);
        assertEquals(50, resized.getWidth());
        assertEquals(50, resized.getHeight());
    }

    @Test
    /**
     * testCrop方法。
     */
    public void testCrop() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage cropped = ImageUtil.crop(img, 10, 10, 50, 50);
        assertEquals(50, cropped.getWidth());
        assertEquals(50, cropped.getHeight());
    }

    @Test
    /**
     * testGrayscale方法。
     */
    public void testGrayscale() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage gray = ImageUtil.grayscale(img);
        assertNotNull(gray);
    }

    @Test
    /**
     * testBrightness方法。
     */
    public void testBrightness() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage bright = ImageUtil.brightness(img, 50);
        assertNotNull(bright);
    }

    @Test
    /**
     * testContrast方法。
     */
    public void testContrast() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage contrasted = ImageUtil.contrast(img, 1.5);
        assertNotNull(contrasted);
    }

    @Test
    /**
     * testBlur方法。
     */
    public void testBlur() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage blurred = ImageUtil.blur(img, 3);
        assertNotNull(blurred);
    }

    @Test
    /**
     * testSharpen方法。
     */
    public void testSharpen() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage sharpened = ImageUtil.sharpen(img);
        assertNotNull(sharpened);
    }

    @Test
    /**
     * testInvert方法。
     */
    public void testInvert() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage inverted = ImageUtil.invert(img);
        assertNotNull(inverted);
    }

    @Test
    /**
     * testThreshold方法。
     */
    public void testThreshold() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage thresholded = ImageUtil.threshold(img, 128);
        assertNotNull(thresholded);
    }

    @Test
    /**
     * testRotate方法。
     */
    public void testRotate() {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage rotated = ImageUtil.rotate(img, 90);
        assertNotNull(rotated);
    }

    @Test
    /**
     * testFlipH方法。
     */
    public void testFlipH() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage flipped = ImageUtil.flipH(img);
        assertNotNull(flipped);
    }

    @Test
    /**
     * testFlipV方法。
     */
    public void testFlipV() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage flipped = ImageUtil.flipV(img);
        assertNotNull(flipped);
    }

    @Test
    /**
     * testWatermark方法。
     */
    public void testWatermark() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage watermarked = ImageUtil.watermark(img, "Test", 10, 10, null, Color.RED, 0.5f);
        assertNotNull(watermarked);
    }

    @Test
    /**
     * testConcatH方法。
     */
    public void testConcatH() {
        BufferedImage left = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage right = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage combined = ImageUtil.concatH(left, right);
        assertEquals(100, combined.getWidth());
        assertEquals(50, combined.getHeight());
    }

    @Test
    /**
     * testConcatV方法。
     */
    public void testConcatV() {
        BufferedImage top = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage bottom = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage combined = ImageUtil.concatV(top, bottom);
        assertEquals(50, combined.getWidth());
        assertEquals(100, combined.getHeight());
    }

    @Test
    /**
     * testToBytes方法。
     */
    public void testToBytes() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = ImageUtil.toBytes(img);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    /**
     * testToBytesWithFormat方法。
     */
    public void testToBytesWithFormat() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = ImageUtil.toBytes(img, "png");
        assertNotNull(bytes);
    }

    @Test
    /**
     * testToBase64方法。
     */
    public void testToBase64() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        String base64 = ImageUtil.toBase64(img, "png");
        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
    }

    @Test
    /**
     * testToPng方法。
     */
    public void testToPng() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage png = ImageUtil.toPng(img);
        assertNotNull(png);
    }

    @Test
    /**
     * testToJpg方法。
     */
    public void testToJpg() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage jpg = ImageUtil.toJpg(img);
        assertNotNull(jpg);
        assertEquals(BufferedImage.TYPE_INT_RGB, jpg.getType());
    }
}
