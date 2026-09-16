package com.zifang.util.media.graph.qrcode.encoder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * QRCodeEncoderTest类。
 */
public class QRCodeEncoderTest {

    @Test
    /**
     * testEncodeSimpleString方法。
     */
    public void testEncodeSimpleString() {
        String content = "Hello";
        BitMatrix matrix = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.L, "UTF-8");
        assertNotNull(matrix);
        assertTrue(matrix.getWidth() >= 21); // Version 1 minimum size
        assertTrue(matrix.getHeight() >= 21);
    }

    @Test
    /**
     * testEncodeChineseContent方法。
     */
    public void testEncodeChineseContent() {
        String content = "中文测试";
        BitMatrix matrix = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.M, "GB18030");
        assertNotNull(matrix);
    }

    @Test
    /**
     * testEncodeEmptyContentThrows方法。
     */
    public void testEncodeEmptyContentThrows() {
        try {
            QRCodeEncoder.encode("", 100, 100, ErrorCorrectionLevel.L, "UTF-8");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Content"));
        }
    }

    @Test
    /**
     * testEncodeNullContentThrows方法。
     */
    public void testEncodeNullContentThrows() {
        try {
            QRCodeEncoder.encode(null, 100, 100, ErrorCorrectionLevel.L, "UTF-8");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Content"));
        }
    }

    @Test
    /**
     * testEncodeWithAllErrorCorrectionLevels方法。
     */
    public void testEncodeWithAllErrorCorrectionLevels() {
        String content = "Test";
        // L, M, Q, H levels
        BitMatrix matrixL = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.L, "UTF-8");
        BitMatrix matrixM = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.M, "UTF-8");
        BitMatrix matrixQ = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.Q, "UTF-8");
        BitMatrix matrixH = QRCodeEncoder.encode(content, 100, 100, ErrorCorrectionLevel.H, "UTF-8");

        assertNotNull(matrixL);
        assertNotNull(matrixM);
        assertNotNull(matrixQ);
        assertNotNull(matrixH);
    }

    @Test
    /**
     * testEncodeDifferentSizes方法。
     */
    public void testEncodeDifferentSizes() {
        String content = "Test";
        // Different requested sizes
        BitMatrix small = QRCodeEncoder.encode(content, 50, 50, ErrorCorrectionLevel.L, "UTF-8");
        BitMatrix large = QRCodeEncoder.encode(content, 200, 200, ErrorCorrectionLevel.L, "UTF-8");

        assertNotNull(small);
        assertNotNull(large);
        // Sizes should be based on QR version, not requested size
    }

    @Test
    /**
     * testEncodeLongerContent方法。
     */
    public void testEncodeLongerContent() {
        String content = "This is a longer test content that should use more data capacity";
        BitMatrix matrix = QRCodeEncoder.encode(content, 200, 200, ErrorCorrectionLevel.M, "UTF-8");
        assertNotNull(matrix);
    }

    @Test
    /**
     * testEncoderProducesValidBitMatrix方法。
     */
    public void testEncoderProducesValidBitMatrix() {
        BitMatrix matrix = QRCodeEncoder.encode("Test", 100, 100, ErrorCorrectionLevel.L, "UTF-8");
        // Check finder pattern positions (should have bits set in corners)
        assertTrue(matrix.get(0, 0)); // Top-left finder pattern
        assertTrue(matrix.get(6, 0));
        assertTrue(matrix.get(0, 6));
    }
}
