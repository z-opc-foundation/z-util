package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

/**
 * BinarizerTest类。
 */
public class BinarizerTest {

    @Test
    /**
     * testBinarizeWithThreshold方法。
     */
    public void testBinarizeWithThreshold() {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Fill half bright, half dark
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                int gray = x < 5 ? 200 : 50; // left=bright, right=dark
                int rgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgb);
            }
        }

        BitMatrix matrix = Binarizer.binarize(image, 128);
        assertNotNull(matrix);
        assertEquals(10, matrix.getWidth());
        assertEquals(10, matrix.getHeight());
        // Left side should be white (true), right side black (false)
        assertTrue(matrix.get(0, 5));
        assertFalse(matrix.get(9, 5));
    }

    @Test
    /**
     * testBinarizeAutomatic方法。
     */
    public void testBinarizeAutomatic() {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Create a gradient image
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                int gray = x * 25; // 0 to 225
                int rgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgb);
            }
        }

        BitMatrix matrix = Binarizer.binarize(image);
        assertNotNull(matrix);
        assertEquals(10, matrix.getWidth());
        assertEquals(10, matrix.getHeight());
    }

    @Test
    /**
     * testBinarizeSolidImage方法。
     */
    public void testBinarizeSolidImage() {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        // All white pixels
        int white = 0xFFFFFF;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                image.setRGB(x, y, white);
            }
        }

        BitMatrix matrix = Binarizer.binarize(image, 128);
        // All should be white (true)
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                assertTrue("Expected white at (" + x + "," + y + ")", matrix.get(x, y));
            }
        }
    }

    @Test
    /**
     * testBinarizeBlackImage方法。
     */
    public void testBinarizeBlackImage() {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        // All black pixels
        int black = 0x000000;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                image.setRGB(x, y, black);
            }
        }

        BitMatrix matrix = Binarizer.binarize(image, 128);
        // All should be black (false)
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                assertFalse("Expected black at (" + x + "," + y + ")", matrix.get(x, y));
            }
        }
    }
}
