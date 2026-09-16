package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

/**
 * ImageCompareTest类。
 */
public class ImageCompareTest {

    @Test
    /**
     * testCompareIdenticalImages方法。
     */
    public void testCompareIdenticalImages() {
        BufferedImage img1 = createTestImage(100, 100, Color.RED);
        BufferedImage img2 = createTestImage(100, 100, Color.RED);

        float similarity = ImageCompare.compareImage(img1, img2);
        assertEquals(100.0f, similarity, 0.01f);
    }

    @Test
    /**
     * testCompareDifferentImages方法。
     */
    public void testCompareDifferentImages() {
        BufferedImage img1 = createTestImage(100, 100, Color.RED);
        BufferedImage img2 = createTestImage(100, 100, Color.BLUE);

        float similarity = ImageCompare.compareImage(img1, img2);
        assertTrue(similarity < 100.0f);
    }

    @Test
    /**
     * testCompareDifferentSizes方法。
     */
    public void testCompareDifferentSizes() {
        BufferedImage img1 = createTestImage(100, 100, Color.RED);
        BufferedImage img2 = createTestImage(50, 50, Color.RED);

        float similarity = ImageCompare.compareImage(img1, img2);
        assertTrue(similarity > 0);
    }

    @Test
    /**
     * testIsIdenticalSameImage方法。
     */
    public void testIsIdenticalSameImage() {
        BufferedImage img1 = createTestImage(100, 100, Color.GREEN);
        BufferedImage img2 = createTestImage(100, 100, Color.GREEN);

        assertTrue(ImageCompare.isIdentical(img1, img2));
    }

    @Test
    /**
     * testIsIdenticalDifferentImages方法。
     */
    public void testIsIdenticalDifferentImages() {
        BufferedImage img1 = createTestImage(100, 100, Color.RED);
        BufferedImage img2 = createTestImage(100, 100, Color.BLUE);

        assertFalse(ImageCompare.isIdentical(img1, img2));
    }

    @Test
    /**
     * testIsIdenticalDifferentSizes方法。
     */
    public void testIsIdenticalDifferentSizes() {
        BufferedImage img1 = createTestImage(100, 100, Color.RED);
        BufferedImage img2 = createTestImage(50, 50, Color.RED);

        assertFalse(ImageCompare.isIdentical(img1, img2));
    }

    @Test
    /**
     * testDiffImage方法。
     */
    public void testDiffImage() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.BLUE);

        BufferedImage diff = ImageCompare.diffImage(img1, img2);
        assertNotNull(diff);
        assertEquals(10, diff.getWidth());
        assertEquals(10, diff.getHeight());
    }

    @Test
    /**
     * testDiffImageSameImages方法。
     */
    public void testDiffImageSameImages() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);

        BufferedImage diff = ImageCompare.diffImage(img1, img2);
        assertNotNull(diff);
        // All pixels should be the same
        assertEquals(img1.getRGB(0, 0), diff.getRGB(0, 0));
    }

    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
