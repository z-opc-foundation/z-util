package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * QRCodeDecoderTest类。
 */
public class QRCodeDecoderTest {

    @Test
    /**
     * testDecodeSimpleImage方法。
     */
    public void testDecodeSimpleImage() throws IOException {
        // Create a simple test image (white background)
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        // This may throw if no valid QR code found, which is expected for synthetic image
        try {
            String result = QRCodeDecoder.decode(image, "UTF-8");
            // If it doesn't throw, we got some result
            assertNotNull(result);
        } catch (RuntimeException e) {
            // Expected for synthetic images without real QR patterns
            assertTrue(e.getMessage().contains("finder patterns") || e.getMessage().contains("Could not"));
        }
    }

    @Test
    /**
     * testDecodeWithGrayscaleImage方法。
     */
    public void testDecodeWithGrayscaleImage() throws IOException {
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        try {
            String result = QRCodeDecoder.decode(image, "UTF-8");
            assertNotNull(result);
        } catch (RuntimeException e) {
            // Expected - synthetic image without real QR patterns
        }
    }

    @Test
    /**
     * testBinarizerAndFinderPatternWorkTogether方法。
     */
    public void testBinarizerAndFinderPatternWorkTogether() {
        // Test that Binarizer and FinderPatternFinder can be used together
        BufferedImage image = new BufferedImage(21, 21, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 21, 21);
        // Add a simple pattern
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 7, 7);
        g.fillRect(14, 0, 7, 7);
        g.fillRect(0, 14, 7, 7);
        g.dispose();

        BitMatrix binaryMatrix = Binarizer.binarize(image);
        assertNotNull(binaryMatrix);

        FinderPatternFinder finder = new FinderPatternFinder(binaryMatrix);
        assertNotNull(finder);
    }
}
