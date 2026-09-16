package com.zifang.util.media.graph.qrcode;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * MatrixToImageWriterTest类。
 */
public class MatrixToImageWriterTest {

    @Test
    /**
     * testMatrixToImageWriterHasPrivateConstructor方法。
     */
    public void testMatrixToImageWriterHasPrivateConstructor() {
        assertNotNull(MatrixToImageWriter.class);
    }

    @Test
    /**
     * testToBufferedImage方法。
     */
    public void testToBufferedImage() throws Exception {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(0, 0);
        matrix.set(1, 1);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        assertNotNull(image);
        assertEquals(10, image.getWidth());
        assertEquals(10, image.getHeight());
    }

    @Test
    /**
     * testWriteToStream方法。
     */
    public void testWriteToStream() throws Exception {
        BitMatrix matrix = new BitMatrix(10, 10);
        matrix.set(5, 5);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "png", out);
        assertTrue(out.size() > 0);
    }
}
