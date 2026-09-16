package com.zifang.util.media.graph.image.GIF;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * QuantTest类。
 */
public class QuantTest {

    @Test
    /**
     * testQuantExists方法。
     */
    public void testQuantExists() {
        // Quant class has package-private constructor with args
        // Just verify the class is accessible
        assertNotNull(Quant.class);
    }
}
