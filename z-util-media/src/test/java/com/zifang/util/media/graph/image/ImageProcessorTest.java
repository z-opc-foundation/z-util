package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * ImageProcessorTest类。
 */
public class ImageProcessorTest {

    @Test
    /**
     * testConstructor方法。
     */
    public void testConstructor() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        assertNotNull(processor);
        assertEquals(100, processor.getWidth());
        assertEquals(100, processor.getHeight());
    }

    @Test
    /**
     * testResize方法。
     */
    public void testResize() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.resize(50, 50);
        assertEquals(50, processor.getWidth());
        assertEquals(50, processor.getHeight());
    }

    @Test
    /**
     * testResizeInvalidWidth方法。
     */
    public void testResizeInvalidWidth() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.resize(0, 50);
        assertEquals(1, processor.getWidth());
    }

    @Test
    /**
     * testScale方法。
     */
    public void testScale() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.scale(0.5);
        assertEquals(50, processor.getWidth());
        assertEquals(50, processor.getHeight());
    }

    @Test
    /**
     * testScaleInvalidScale方法。
     */
    public void testScaleInvalidScale() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.scale(0); // Should be set to 0.01
        assertEquals(1, processor.getWidth());
    }

    @Test
    /**
     * testCrop方法。
     */
    public void testCrop() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.crop(10, 10, 50, 50);
        assertEquals(50, processor.getWidth());
        assertEquals(50, processor.getHeight());
    }

    @Test
    /**
     * testCropBeyondBounds方法。
     */
    public void testCropBeyondBounds() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.crop(90, 90, 50, 50); // Should be adjusted
        assertTrue(processor.getWidth() <= 50);
        assertTrue(processor.getHeight() <= 50);
    }

    @Test
    /**
     * testRotate方法。
     */
    public void testRotate() {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.rotate(90);
        // After 90 degree rotation, dimensions swap
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testFlipHorizontal方法。
     */
    public void testFlipHorizontal() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.flipHorizontal();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testFlipVertical方法。
     */
    public void testFlipVertical() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.flipVertical();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testGrayscale方法。
     */
    public void testGrayscale() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.grayscale();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testBrightness方法。
     */
    public void testBrightness() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.brightness(50);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testContrast方法。
     */
    public void testContrast() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.contrast(1.5);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testInvert方法。
     */
    public void testInvert() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.invert();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testThreshold方法。
     */
    public void testThreshold() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.threshold(128);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testBlur方法。
     */
    public void testBlur() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.blur(3);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testSharpen方法。
     */
    public void testSharpen() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.sharpen();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testEdgeDetect方法。
     */
    public void testEdgeDetect() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.edgeDetect();
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testWatermarkText方法。
     */
    public void testWatermarkText() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.watermarkText("Test", 10, 10, null, Color.RED, 0.5f);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testOverlay方法。
     */
    public void testOverlay() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage overlay = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        processor.overlay(overlay, 25, 25, 0.5f);
        assertNotNull(processor.getImage());
    }

    @Test
    /**
     * testConcatRight方法。
     */
    public void testConcatRight() {
        BufferedImage img1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img1);
        processor.concatRight(img2);
        assertEquals(100, processor.getWidth());
        assertEquals(50, processor.getHeight());
    }

    @Test
    /**
     * testConcatBottom方法。
     */
    public void testConcatBottom() {
        BufferedImage img1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img1);
        processor.concatBottom(img2);
        assertEquals(50, processor.getWidth());
        assertEquals(100, processor.getHeight());
    }

    @Test
    /**
     * testGetImage方法。
     */
    public void testGetImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageProcessor processor = new ImageProcessor(img);
        assertEquals(img, processor.getImage());
    }

    @Test
    /**
     * testLoadFromBytes方法。
     */
    public void testLoadFromBytes() throws Exception {
        BufferedImage original = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(original, "png", baos);
        byte[] bytes = baos.toByteArray();

        ImageProcessor processor = ImageProcessor.load(bytes);
        assertNotNull(processor);
        assertEquals(10, processor.getWidth());
        assertEquals(10, processor.getHeight());
    }
}
