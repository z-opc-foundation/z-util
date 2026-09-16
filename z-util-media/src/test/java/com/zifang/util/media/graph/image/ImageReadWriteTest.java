package com.zifang.util.media.graph.image;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * ImageReadWriteTest类。
 */
public class ImageReadWriteTest {

    @Test
    /**
     * testReadFromByteArray方法。
     */
    public void testReadFromByteArray() throws IOException {
        // Create a simple test image
        BufferedImage original = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        original.setRGB(5, 5, 0xFF0000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(original, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        // Read from byte array
        BufferedImage read = ImageReadWrite.read(imageBytes);
        assertNotNull(read);
        assertEquals(10, read.getWidth());
        assertEquals(10, read.getHeight());
    }

    @Test
    /**
     * testInferFormat方法。
     */
    public void testInferFormat() {
        assertEquals("png", ImageReadWrite.inferFormat("image.png"));
        assertEquals("jpeg", ImageReadWrite.inferFormat("image.jpg"));
        assertEquals("gif", ImageReadWrite.inferFormat("image.gif"));
        assertEquals("bmp", ImageReadWrite.inferFormat("image.bmp"));
        assertEquals("jpeg", ImageReadWrite.inferFormat("image.JPG"));
    }

    @Test
    /**
     * testInferFormatNoExtension方法。
     */
    public void testInferFormatNoExtension() {
        assertEquals("", ImageReadWrite.inferFormat("imageWithoutExtension"));
    }

    @Test
    /**
     * testIsSupported方法。
     */
    public void testIsSupported() {
        assertTrue(ImageReadWrite.isSupported("image.png"));
        assertTrue(ImageReadWrite.isSupported("image.jpg"));
    }

    @Test
    /**
     * testWriteToOutputStream方法。
     */
    public void testWriteToOutputStream() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageReadWrite.write(image, "png", baos);
        assertTrue(baos.size() > 0);
    }

    @Test
    /**
     * testToBytes方法。
     */
    public void testToBytes() throws IOException {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = ImageReadWrite.toBytes(image);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    /**
     * testToBytesWithFormat方法。
     */
    public void testToBytesWithFormat() throws IOException {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        byte[] pngBytes = ImageReadWrite.toBytes(image, "png");
        assertNotNull(pngBytes);
    }
}
