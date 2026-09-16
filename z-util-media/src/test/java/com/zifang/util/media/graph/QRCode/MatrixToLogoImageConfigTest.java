package com.zifang.util.media.graph.qrcode;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * MatrixToLogoImageConfigTest类。
 */
public class MatrixToLogoImageConfigTest {

    @Test
    /**
     * testMatrixToLogoImageConfigExists方法。
     */
    public void testMatrixToLogoImageConfigExists() {
        MatrixToLogoImageConfig config = new MatrixToLogoImageConfig();
        assertNotNull(config);
    }
}
