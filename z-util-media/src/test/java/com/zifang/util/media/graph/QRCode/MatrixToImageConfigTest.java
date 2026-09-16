package com.zifang.util.media.graph.qrcode;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * MatrixToImageConfigTest类。
 */
public class MatrixToImageConfigTest {

    @Test
    /**
     * testMatrixToImageConfigExists方法。
     */
    public void testMatrixToImageConfigExists() {
        MatrixToImageConfig config = new MatrixToImageConfig();
        assertNotNull(config);
    }
}
